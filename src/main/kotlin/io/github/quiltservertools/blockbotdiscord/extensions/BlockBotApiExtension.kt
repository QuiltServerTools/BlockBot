package io.github.quiltservertools.blockbotdiscord.extensions

import com.kotlindiscord.kord.extensions.checks.inGuild
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.ensureWebhook
import com.kotlindiscord.kord.extensions.utils.getTopRole
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.vdurmont.emoji.EmojiParser
import dev.kord.common.entity.ActivityType
import dev.kord.common.entity.AllowedMentionType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.execute
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import io.github.quiltservertools.blockbotapi.Bot
import io.github.quiltservertools.blockbotapi.Channels
import io.github.quiltservertools.blockbotapi.event.RelayMessageEvent
import io.github.quiltservertools.blockbotapi.sender.MessageSender
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender
import io.github.quiltservertools.blockbotapi.sender.RelayMessageSender
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.MentionToMinecraftRenderer
import io.github.quiltservertools.blockbotdiscord.config.*
import io.github.quiltservertools.blockbotdiscord.utility.Colors
import io.github.quiltservertools.blockbotdiscord.utility.convertEmojiToTranslatable
import io.github.quiltservertools.blockbotdiscord.utility.convertStringToMention
import io.github.quiltservertools.blockbotdiscord.utility.literal
import io.github.quiltservertools.mcdiscordreserializer.minecraft.MinecraftSerializer
import io.github.quiltservertools.mcdiscordreserializer.minecraft.MinecraftSerializerOptions
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.advancement.Advancement
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import org.koin.core.component.inject

class BlockBotApiExtension : Extension(), Bot {
    override val name = "BlockBot Api Impl"
    private lateinit var chatWebhook: Webhook

    private val minecraftSerializer = MinecraftSerializer(
        MinecraftSerializerOptions.defaults()
            .addRenderer(MentionToMinecraftRenderer(bot)),
        MinecraftSerializerOptions.escapeDefaults()
    )
    private val server: MinecraftServer by inject()
    private val mentions = AllowedMentionsBuilder()

    override suspend fun setup() {
        val channel = config.getChannel(Channels.CHAT, bot)

        chatWebhook = ensureWebhook(channel as TopGuildMessageChannel, config[ChatRelaySpec.WebhookSpec.webhookName])
        mentions.add(AllowedMentionType.UserMentions)
        mentions.roles.addAll(config.getGuild(bot).roles.filter { it.mentionable }.map { it.id }
            .toList())

        event<MessageCreateEvent> {
            check { isNotBot() }
            check { inGuild(Snowflake(config[BotSpec.guild])) }
            check { failIfNot(config.getChannelsBi().containsValue(event.message.channelId.value)) }

            action {
                val sender = event.message.getAuthorAsMember()!!
                val configChannel = config.getChannelsBi().inverse()[event.message.channelId.value]!!
                val result = RelayMessageEvent.EVENT.invoker().message(
                    RelayMessageSender(
                        sender.username,
                        sender.nickname,
                        sender.tag,
                        sender.hasPermission(Permission.Administrator)
                    ),
                    configChannel,
                    event.message.content
                )

                if (result == ActionResult.PASS) {
                    if (configChannel == Channels.CHAT) {
                        val message = getChatMessage(sender, event.message)

                        server.submit {
                            server.playerManager.broadcastChatMessage(
                                message,
                                net.minecraft.network.MessageType.CHAT,
                                Util.NIL_UUID
                            )
                        }
                    }
                }
            }
        }

        // Send server started message if the server has already started. isLoading should be named isRunning
        if (server.isLoading) onServerStart(server)
    }

    public suspend fun getChatMessage(sender: Member, message: Message): Text {
        val emojiString = EmojiParser.parseToAliases(message.content)
        var content: MutableText =
            if (config[ChatRelaySpec.convertMarkdown]) minecraftSerializer.serialize(emojiString) else emojiString.literal()
        content = convertEmojiToTranslatable(content)
        if (message.referencedMessage != null) {
            val reply = config.getReplyMsg(
                message.referencedMessage!!.data.author.username,
                message.referencedMessage!!,
                server
            )
            content = "".literal().append(reply).append("\n").append(content)
        }

        for (sticker in message.stickers) {
            if (content.string.isNotEmpty()) content.append("\n")
            content.append("[Sticker: ${sticker.name}]")
        }

        for (attachment in message.attachments) {
            if (content.string.isNotEmpty()) content.append("\n")
            content.append("[${attachment.filename}]".literal().styled {
                it.withColor(Formatting.BLUE)
                    .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, attachment.url))
            })
        }

        val topRole = sender.getTopRole()
        val topColor = sender.getDisplayColor()
        var topRoleMessage: MutableText =
            topRole?.data?.name?.literal() ?: "".literal()
        if (topColor != null) topRoleMessage = topRoleMessage.styled { it.withColor(topColor.rgb) }
        var username: MutableText = sender.displayName.literal()
        if (topColor != null) {
            username = username.styled {
                it.withColor(topColor.rgb)
            }
        }

        return config.getMinecraftChatRelayMsg(username, topRoleMessage, content, server)
    }

    public suspend fun createDiscordEmbed(builder: EmbedBuilder.() -> Unit) {
        if (config[ChatRelaySpec.WebhookSpec.useWebhook]) {
            chatWebhook.execute(chatWebhook.token!!) {
                avatarUrl = config[ChatRelaySpec.WebhookSpec.webhookAvatar]
                allowedMentions = mentions
                embeds.add(EmbedBuilder().apply(builder))
            }
        } else {
            val messageChannel = config.getChannel(Channels.CHAT, bot)
            messageChannel.createMessage {
                allowedMentions = mentions
                embed(builder)
            }
        }
    }

    public suspend fun createDiscordMessage(content: String) {
        if (config[ChatRelaySpec.WebhookSpec.useWebhook]) {
            chatWebhook.execute(chatWebhook.token!!) {
                allowedMentions = mentions
                this.content = content
            }
        } else {
            val messageChannel = config.getChannel(Channels.CHAT, bot)
            messageChannel.createMessage {
                allowedMentions = mentions
                this.content = content
            }
        }
    }

    override fun onChatMessage(sender: MessageSender, message: String) {
        BlockBotDiscord.launch {
            var content = message
            content = MinecraftSerializer.INSTANCE.escapeMarkdown(content) // TODO config
            if (config[ChatRelaySpec.escapeInGameMarkdown]) {
                content = MinecraftSerializer.INSTANCE.escapeMarkdown(content)
            }
            if (config[ChatRelaySpec.allowMentions]) {
                content = convertStringToMention(content, config.getGuild(bot))
            }

            if (config[ChatRelaySpec.WebhookSpec.useWebhook]) {
                if (sender.formatWebhookContent(content).isEmpty()) return@launch
                chatWebhook.execute(chatWebhook.token!!) {
                    this.allowedMentions = mentions
                    this.username = config.formatWebhookAuthor(sender)
                    this.content = sender.formatWebhookContent(content)
                    this.avatarUrl = sender.getAvatar()
                }
            } else {
                if (sender.formatMessageContent(content).isEmpty()) return@launch

                val messageChannel = config.getChannel(Channels.CHAT, bot)
                messageChannel.createMessage {
                    allowedMentions = mentions
                    this.content = sender.formatMessageContent(content)
                }
            }
        }
    }

    override fun onPlayerConnect(handler: ServerPlayNetworkHandler, sender: PacketSender, server: MinecraftServer) {
        if (config.formatPlayerJoinMessage(handler.player).isEmpty()) return
        BlockBotDiscord.launch {
            createDiscordEmbed {
                author {
                    name = config.formatPlayerJoinMessage(handler.player)
                    icon = config.getWebhookChatRelayAvatar(handler.player.gameProfile)
                }
                color = Colors.green
            }
        }
    }

    override fun onPlayerDisconnect(handler: ServerPlayNetworkHandler, server: MinecraftServer) {
        if (config.formatPlayerLeaveMessage(handler.player).isEmpty()) return
        BlockBotDiscord.launch {
            createDiscordEmbed {
                author {
                    name = config.formatPlayerLeaveMessage(handler.player)
                    icon = config.getWebhookChatRelayAvatar(handler.player.gameProfile)
                }
                color = Colors.red
            }
        }
    }

    override fun onPlayerDeath(player: ServerPlayerEntity, message: Text) {
        BlockBotDiscord.launch {
            createDiscordEmbed {
                author {
                    name = message.string
                    icon = config.getWebhookChatRelayAvatar(player.gameProfile)
                }
                color = Colors.orange
            }
        }
    }

    override fun onAdvancementGrant(player: ServerPlayerEntity, advancement: Advancement) {
        if (config.formatPlayerAdvancementMessage(player, advancement).isEmpty()) return
        BlockBotDiscord.launch {
            createDiscordEmbed {
                author {
                    name = config.formatPlayerAdvancementMessage(player, advancement)
                    icon = config.getWebhookChatRelayAvatar(player.gameProfile)
                }
                footer {
                    text = advancement.display!!.description.string
                }
                color = Colors.blue
            }
        }
    }

    override fun onServerStart(server: MinecraftServer) {
        if (config.formatServerStartMessage(server).isEmpty()) return
        BlockBotDiscord.launch {
            createDiscordEmbed {
                author {
                    name = config.formatServerStartMessage(server)
                }
                color = Colors.green
            }
        }
    }

    override fun onServerStop(server: MinecraftServer) {
        if (config.formatServerStopMessage(server).isEmpty()) return
        runBlocking {
            createDiscordEmbed {
                author {
                    name = config.formatServerStopMessage(server)
                }
                color = Colors.red
            }
            kord.shutdown()
        }
    }

    override fun onServerTick(server: MinecraftServer) {
        BlockBotDiscord.launch {
            if (server.ticks % 400 == 0) {
                kord.editPresence {
                    when (config[PresenceSpec.activityType]) {
                        ActivityType.Game -> playing(config.formatPresenceText(server))
                        ActivityType.Listening -> listening(config.formatPresenceText(server))
                        ActivityType.Watching -> watching(config.formatPresenceText(server))
                        ActivityType.Competing -> competing(config.formatPresenceText(server))
                        else -> Unit
                    }
                }
            }
        }
    }


    override fun sendRelayMessage(content: String, channel: String) {
    }

    override fun onRelayMessage(content: String, channel: String) {
    }
}

fun MessageSender.getAvatar(): String {
    return if (this is PlayerMessageSender) config.getWebhookChatRelayAvatar(this.profile)
    else config[ChatRelaySpec.WebhookSpec.webhookAvatar]
}

fun MessageSender.formatMessageContent(content: String): String {
    return when (this.type) {
        MessageSender.MessageType.REGULAR -> config.formatDiscordMessage(this, content)
        MessageSender.MessageType.EMOTE -> config.formatDiscordEmote(this, content)
        MessageSender.MessageType.ANNOUNCEMENT -> config.formatDiscordAnnouncement(this, content)
    }
}

fun MessageSender.formatWebhookContent(content: String): String {
    return when (this.type) {
        MessageSender.MessageType.REGULAR -> config.formatWebhookMessage(this, content)
        MessageSender.MessageType.EMOTE -> config.formatWebhookEmote(this, content)
        MessageSender.MessageType.ANNOUNCEMENT -> config.formatWebhookAnnouncement(this, content)
    }
}

suspend fun Member.getDisplayColor() =
    this.roles.toList().sortedByDescending { it.rawPosition }.firstOrNull { it.color.rgb != 0 }?.color
