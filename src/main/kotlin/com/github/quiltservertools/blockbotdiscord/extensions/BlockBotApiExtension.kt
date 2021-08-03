package com.github.quiltservertools.blockbotdiscord.extensions

import com.github.quiltservertools.blockbotapi.Bot
import com.github.quiltservertools.blockbotapi.Channels
import com.github.quiltservertools.blockbotapi.event.DiscordMessageEvent
import com.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import com.github.quiltservertools.blockbotdiscord.MentionToMinecraftRenderer
import com.github.quiltservertools.blockbotdiscord.config.*
import com.github.quiltservertools.blockbotdiscord.utility.*
import com.github.quiltservertools.mcdiscordreserializer.minecraft.MinecraftSerializer
import com.github.quiltservertools.mcdiscordreserializer.minecraft.MinecraftSerializerOptions
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.ensureWebhook
import com.kotlindiscord.kord.extensions.utils.getTopRole
import com.vdurmont.emoji.EmojiParser
import dev.kord.common.entity.AllowedMentionType
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.execute
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.Webhook
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
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

        chatWebhook = ensureWebhook(channel, config[ChatRelaySpec.WebhookSpec.webhookName])
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
                val result = DiscordMessageEvent.EVENT.invoker().message(
                    sender.displayName,
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
        val topRoleMessage =
            topRole?.data?.name?.literal()?.styled { it.withColor(topRole.color.rgb) } ?: "".literal()
        var username: MutableText = sender.displayName.literal()
        if (topRole != null) {
            username = username.styled {
                it.withColor(topRole.color.rgb)
            }
        }

        return config.getMinecraftChatRelayMsg(username, topRoleMessage, content, server)
    }

    public suspend fun createDiscordEmbed(builder: EmbedBuilder.() -> Unit) {
        if (config[ChatRelaySpec.WebhookSpec.useWebhook]) {
            chatWebhook.execute(chatWebhook.token!!) {
                avatarUrl = config[ChatRelaySpec.WebhookSpec.webhookAvatar]
                allowedMentions = mentions
                embeds.add(EmbedBuilder().apply(builder).toRequest())
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

    override fun onChatMessage(player: ServerPlayerEntity?, message: String) {
        BlockBotDiscord.launch {
            var content = message
            // content = MinecraftSerializer.INSTANCE.escapeMarkdown(content)
            if (config[ChatRelaySpec.allowMentions]) {
                content = convertStringToMention(content, config.getGuild(bot))
            }

            if (config[ChatRelaySpec.WebhookSpec.useWebhook]) {
                chatWebhook.execute(chatWebhook.token!!) {
                    this.allowedMentions = mentions
                    this.username = player?.displayName?.asString() ?: "Server"
                    this.content = content
                    this.avatarUrl = if (player != null)
                        config.getWebhookChatRelayAvatar(player.uuid)
                    else
                        config[ChatRelaySpec.WebhookSpec.webhookAvatar]
                }
            } else {
                val messageChannel = config.getChannel(Channels.CHAT, bot)

                messageChannel.createMessage {
                    allowedMentions = mentions
                    this.content = if (player != null) config.getDiscordChatRelayMsg(player, content) else content
                }
            }
        }
    }

    override fun onPlayerConnect(handler: ServerPlayNetworkHandler, sender: PacketSender, server: MinecraftServer) {
        BlockBotDiscord.launch {
            createDiscordEmbed {
                author {
                    name = "${handler.player.displayName.asString()} joined the game"
                    icon = config.getWebhookChatRelayAvatar(handler.player.uuid)
                }
                color = Colors.green
            }
        }
    }

    override fun onPlayerDisconnect(handler: ServerPlayNetworkHandler, server: MinecraftServer) {
        BlockBotDiscord.launch {
            createDiscordEmbed {
                author {
                    name = "${handler.player.displayName.asString()} left the game"
                    icon = config.getWebhookChatRelayAvatar(handler.player.uuid)
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
                    icon = config.getWebhookChatRelayAvatar(player.uuid)
                }
                color = Colors.orange
            }
        }
    }

    override fun onAdvancementGrant(player: ServerPlayerEntity, advancement: Advancement) {
        BlockBotDiscord.launch {
            createDiscordEmbed {
                author {
                    name =
                        "${player.displayName.asString()} has made the advancement [${advancement.display!!.title.string}]"
                    icon = config.getWebhookChatRelayAvatar(player.uuid)
                }
                footer {
                    text = advancement.display!!.description.string
                }
                color = Colors.blue
            }
        }
    }

    override fun onServerStart(server: MinecraftServer?) {
        BlockBotDiscord.launch {
            createDiscordEmbed {
                author {
                    name = "🟢 Server Started!"
                }
                color = Colors.green
            }
        }
    }

    override fun onServerStop(server: MinecraftServer?) {
        runBlocking {
            createDiscordEmbed {
                author {
                    name = "🛑 Server Stopped"
                }
                color = Colors.red
            }
        }
    }


    override fun sendDiscordMessage(content: String, channel: String) {
    }

    override fun onDiscordMessage(content: String, channel: String) {
    }
}
