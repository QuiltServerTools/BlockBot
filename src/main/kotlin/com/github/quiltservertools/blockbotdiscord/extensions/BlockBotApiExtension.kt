package com.github.quiltservertools.blockbotdiscord.extensions

import com.github.quiltservertools.blockbotapi.Bot
import com.github.quiltservertools.blockbotapi.Channels
import com.github.quiltservertools.blockbotapi.event.DiscordMessageEvent
import com.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import com.github.quiltservertools.blockbotdiscord.config.*
import com.github.quiltservertools.blockbotdiscord.utility.Colors
import com.github.quiltservertools.blockbotdiscord.utility.convertStringToMention
import com.github.quiltservertools.blockbotdiscord.utility.literal
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.ensureWebhook
import com.kotlindiscord.kord.extensions.utils.getTopRole
import dev.kord.common.entity.AllowedMentionType
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.execute
import dev.kord.core.entity.Webhook
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.advancement.Advancement
import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Util
import org.koin.core.component.inject

class BlockBotApiExtension : Extension(), Bot {
    override val name = "BlockBot Api Impl"
    private lateinit var chatWebhook: Webhook

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
                        var message = event.message.content
                        val topRole = sender.getTopRole()
                        val topRoleMessage =
                            topRole?.data?.name?.literal()?.styled { it.withColor(topRole.color.rgb) } ?: "".literal()
                        var username: MutableText = sender.displayName.literal()
                        if (topRole != null) {
                            username = username.styled {
                                it.withColor(topRole.color.rgb)
                            }
                        }

                        server.submit {
                            server.playerManager.broadcastChatMessage(
                                config.getMinecraftChatRelayMsg(username, topRoleMessage, message, server),
                                MessageType.CHAT,
                                Util.NIL_UUID
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onChatMessage(player: ServerPlayerEntity?, message: String) {
        BlockBotDiscord.launch {
            var content = message
            if (config[ChatRelaySpec.allowMentions]) {
                content = convertStringToMention(message, config.getGuild(bot))
            }

            if (config[ChatRelaySpec.WebhookSpec.useWebhook]) {
                chatWebhook.execute(chatWebhook.token!!) {
                    this.allowedMentions = mentions
                    this.username = player?.displayName?.asString() ?: "server"
                    this.content = content
                    this.avatarUrl = if (player != null)
                        config.getWebhookChatRelayAvatar(player.uuid)
                    else
                        "https://lh3.googleusercontent.com/proxy/cak6AiMaURxOuANzYm_DkwFwykl7vfWjS_DuMqCmT6zgXTuddtXQ8HXyVT5KRWhiI_td0IOwcw183uNmMPTUHiIy"
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
            val messageChannel = config.getChannel(Channels.CHAT, bot)
            messageChannel.createEmbed {
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
            val messageChannel = config.getChannel(Channels.CHAT, bot)
            messageChannel.createEmbed {
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
            val messageChannel = config.getChannel(Channels.CHAT, bot)
            messageChannel.createEmbed {
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
            val messageChannel = config.getChannel(Channels.CHAT, bot)
            messageChannel.createEmbed {
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
            val messageChannel = config.getChannel(Channels.CHAT, bot)
            messageChannel.createEmbed {
                author {
                    name = "🟢 Server Started!"
                }
                color = Colors.green
            }
        }
    }

    override fun onServerStop(server: MinecraftServer?) {
        runBlocking {
            val messageChannel = config.getChannel(Channels.CHAT, bot)
            messageChannel.createEmbed {
                author {
                    name = "🛑 Server Stopped"
                }
                color = Colors.red
            }
            delay(10000)
        }
    }


    override fun sendDiscordMessage(content: String?, channel: String?) {
    }

    override fun onDiscordMessage(content: String?, channel: String?) {
    }
}
