package io.github.quiltservertools.blockbotdiscord.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.ensureWebhook
import com.kotlindiscord.kord.extensions.utils.getTopRole
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.vdurmont.emoji.EmojiParser
import dev.kord.common.entity.ActivityType
import dev.kord.common.entity.AllowedMentionType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.execute
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.Webhook
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import eu.pb4.placeholders.PlaceholderAPI
import io.github.quiltservertools.blockbotapi.Bot
import io.github.quiltservertools.blockbotapi.Channels
import io.github.quiltservertools.blockbotapi.event.RelayMessageEvent
import io.github.quiltservertools.blockbotapi.sender.*
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.MentionToMinecraftRenderer
import io.github.quiltservertools.blockbotdiscord.config.*
import io.github.quiltservertools.blockbotdiscord.utility.convertEmojiToTranslatable
import io.github.quiltservertools.blockbotdiscord.utility.convertStringToMention
import io.github.quiltservertools.blockbotdiscord.utility.literal
import io.github.quiltservertools.mcdiscordreserializer.minecraft.MinecraftSerializer
import io.github.quiltservertools.mcdiscordreserializer.minecraft.MinecraftSerializerOptions
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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

        chatWebhook = ensureWebhook(
            channel,
            config[ChatRelaySpec.WebhookSpec.webhookName]
        ) { this::class.java.getResourceAsStream("/assets/blockbot-discord/server.png").readBytes() }
        mentions.add(AllowedMentionType.UserMentions)
        mentions.roles.addAll(config.getGuild(bot).roles.filter { it.mentionable }.map { it.id }
            .toList())

        event<MessageCreateEvent> {
            check { it.message.getAuthorAsMember() != null }
            check { it.message.author?.isBot == false }
            check { config.getChannelsBi().containsValue(it.message.channelId.value) }

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

        if (message.attachments.isNotEmpty()) {
            for (attachment in message.attachments) {
                content.append("\n[${attachment.filename}]".literal().styled {
                    it.withColor(Formatting.BLUE)
                        .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, attachment.url))
                })
            }
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

    public fun sendDiscordMessage(webhookMessage: WebhookMessage, regularMessage: RegularMessage, sender: MessageSender, additionalPlaceholders: Map<String, Text> = mapOf()) {
        val placeholders = mutableMapOf(
            "sender" to sender.name,
            "sender_display" to sender.displayName,
            "sender_avatar" to sender.getAvatar()?.literal()
        ) + additionalPlaceholders

        fun String?.placeholders(): String? {
            if (this == null) return this

            return if (sender is PlayerMessageSender) {
                PlaceholderAPI.parseText(
                    PlaceholderAPI.parsePredefinedText(
                        this.literal(),
                        PlaceholderAPI.ALT_PLACEHOLDER_PATTERN_CUSTOM,
                        placeholders
                    ),
                    sender.player
                ).string
            } else {
                PlaceholderAPI.parseText(
                    PlaceholderAPI.parsePredefinedText(
                        this.literal(),
                        PlaceholderAPI.ALT_PLACEHOLDER_PATTERN_CUSTOM,
                        placeholders
                    ),
                    server
                ).string
            }
        }

        fun createEmbed(embed: MessageEmbed): EmbedBuilder {
            return EmbedBuilder().apply {
                title = embed.title.placeholders()
                description = embed.description.placeholders()
                url = embed.url.placeholders()
                color = embed.color
                embed.footer?.let {
                    footer {
                        text = it.text.placeholders()!!
                        icon = it.iconUrl.placeholders()
                    }
                }
                image = embed.image.placeholders()
                embed.thumbnail?.let {
                    thumbnail { url = it.placeholders()!! }
                }
                embed.author?.let {
                    author {
                        name = it.name.placeholders()
                        url = it.url.placeholders()
                        icon = it.iconUrl.placeholders()
                    }
                }
                for (field in embed.fields) {
                    field {
                        name = field.name.placeholders()!!
                        value = field.value.placeholders()!!
                        inline = inline
                    }
                }
            }
        }

        BlockBotDiscord.launch {
            //TODO allow disabling
            if (config[ChatRelaySpec.WebhookSpec.useWebhook]) {
                val message = webhookMessage

                chatWebhook.execute(chatWebhook.token!!) {
                    allowedMentions = mentions;

                    content = message.content.placeholders()
                    username = message.username.placeholders()
                    avatarUrl = message.avatar.placeholders()
                    tts = message.tts
                    for (embed in message.embeds) {
                        embeds.add(createEmbed(embed).toRequest())
                    }
                }

            } else {
                val message = regularMessage

                val messageChannel = config.getChannel(Channels.CHAT, bot)
                messageChannel.createMessage {
                    allowedMentions = mentions

                    content = message.content.placeholders()
                    tts = message.tts
                    message.embed?.let { embed = createEmbed(message.embed) }
                }
            }
        }
    }

    override fun onChatMessage(sender: MessageSender, type: MessageType, message: String) {
        BlockBotDiscord.launch {
            var content = message
            if (config[ChatRelaySpec.escapeIngameMarkdown]) {
                content = MinecraftSerializer.INSTANCE.escapeMarkdown(content)
            }
            if (config[ChatRelaySpec.allowMentions]) {
                content = convertStringToMention(content, config.getGuild(bot))
            }

            val regularMessage = when (type) {
                MessageType.REGULAR -> messagesConfig.regularFormat.chat
                MessageType.EMOTE -> messagesConfig.regularFormat.emote
                MessageType.ANNOUNCEMENT -> messagesConfig.regularFormat.announcement
            }
            val webhookMessage = when (type) {
                MessageType.REGULAR -> messagesConfig.webhookFormat.chat
                MessageType.EMOTE -> messagesConfig.webhookFormat.emote
                MessageType.ANNOUNCEMENT -> messagesConfig.webhookFormat.announcement
            }

            sendDiscordMessage(webhookMessage, regularMessage, sender, mapOf("message" to content.literal()))
        }
    }

    override fun onPlayerConnect(handler: ServerPlayNetworkHandler, sender: PacketSender, server: MinecraftServer) {
        sendDiscordMessage(messagesConfig.webhookFormat.join, messagesConfig.regularFormat.join, PlayerMessageSender(handler.player))
    }

    override fun onPlayerDisconnect(handler: ServerPlayNetworkHandler, server: MinecraftServer) {
        sendDiscordMessage(messagesConfig.webhookFormat.leave, messagesConfig.regularFormat.leave, PlayerMessageSender(handler.player))
    }

    override fun onPlayerDeath(player: ServerPlayerEntity, message: Text) {
        sendDiscordMessage(messagesConfig.webhookFormat.death, messagesConfig.regularFormat.death, PlayerMessageSender(player))
    }

    override fun onAdvancementGrant(player: ServerPlayerEntity, advancement: Advancement) {
        sendDiscordMessage(messagesConfig.webhookFormat.advancement, messagesConfig.regularFormat.advancement, PlayerMessageSender(player), mapOf(
            "advancement_title" to advancement.display!!.title,
            "advancement_desc" to advancement.display!!.description
        ))
    }

    override fun onServerStart(server: MinecraftServer) {
        sendDiscordMessage(messagesConfig.webhookFormat.serverStart, messagesConfig.regularFormat.serverStart, ServerMessageSender(server))
    }

    override fun onServerStop(server: MinecraftServer) {
        sendDiscordMessage(messagesConfig.webhookFormat.serverStop, messagesConfig.regularFormat.serverStop, ServerMessageSender(server))

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

fun MessageSender.getAvatar(): String? {
    return if (this is PlayerMessageSender) config.getWebhookChatRelayAvatar(this.profile)
    else null
}

suspend fun Member.getDisplayColor() =
    this.roles.toList().sortedByDescending { it.rawPosition }.firstOrNull { it.color.rgb != 0 }?.color
