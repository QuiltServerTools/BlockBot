package io.github.quiltservertools.blockbotdiscord.config

import com.mojang.authlib.GameProfile
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import dev.kord.core.entity.Message
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import io.github.quiltservertools.blockbotapi.sender.MessageSender
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender
import io.github.quiltservertools.blockbotdiscord.utility.getTextures
import io.github.quiltservertools.blockbotdiscord.utility.literal
import io.github.quiltservertools.blockbotdiscord.utility.summary
import net.minecraft.advancement.Advancement
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ChatRelaySpec : ConfigSpec() {
    val allowMentions by required<Boolean>()
    val convertMarkdown by required<Boolean>()
    val escapeIngameMarkdown by required<Boolean>()

    object MinecraftFormatSpec : ConfigSpec() {
        val messageFormat by required<String>()
        val replyFormat by required<String>()
        val appendImages by required<Boolean>()
        val imageInterpolation by required<Boolean>()
    }

    object DiscordMessageFormatSpec : ConfigSpec() {
        val messageFormat by required<String>()
        val announcementFormat by required<String>()
        val emoteFormat by required<String>()
        val playerJoin by required<String>()
        val playerLeave by required<String>()
        val playerDeath by required<String>()
        val playerAdvancement by required<String>()
        val serverStart by required<String>()
        val serverStop by required<String>()
    }

    object DiscordWebhookFormatSpec : ConfigSpec() {
        val messageFormat by required<String>()
        val announcementFormat by required<String>()
        val emoteFormat by required<String>()
        val authorFormat by required<String>()
    }

    object WebhookSpec : ConfigSpec() {
        val useWebhook by required<Boolean>()
        val webhookName by required<String>()
        val webhookAvatar by required<String>()
        val playerAvatarUrl by required<String>()
    }
}

fun Config.formatDiscordMessage(sender: MessageSender, message: String): String =
    formatDiscordRelayMessage(sender, message, config[ChatRelaySpec.DiscordMessageFormatSpec.messageFormat])

fun Config.formatDiscordEmote(sender: MessageSender, message: String): String =
    formatDiscordRelayMessage(sender, message, config[ChatRelaySpec.DiscordMessageFormatSpec.emoteFormat])

fun Config.formatDiscordAnnouncement(sender: MessageSender, message: String): String =
    formatDiscordRelayMessage(sender, message, config[ChatRelaySpec.DiscordMessageFormatSpec.announcementFormat])

fun Config.formatWebhookMessage(sender: MessageSender, message: String): String =
    formatDiscordRelayMessage(sender, message, config[ChatRelaySpec.DiscordWebhookFormatSpec.messageFormat])

fun Config.formatWebhookEmote(sender: MessageSender, message: String): String =
    formatDiscordRelayMessage(sender, message, config[ChatRelaySpec.DiscordWebhookFormatSpec.emoteFormat])

fun Config.formatWebhookAnnouncement(sender: MessageSender, message: String): String =
    formatDiscordRelayMessage(sender, message, config[ChatRelaySpec.DiscordWebhookFormatSpec.announcementFormat])

fun Config.formatWebhookAuthor(sender: MessageSender): String =
    formatDiscordRelayMessage(sender, "", config[ChatRelaySpec.DiscordWebhookFormatSpec.authorFormat], mapOf(
        "sender" to sender.name,
        "sender_display" to sender.displayName
    ))

fun Config.formatPlayerJoinMessage(player: ServerPlayerEntity): String =
    formatDiscordRelayMessage(player, config[ChatRelaySpec.DiscordMessageFormatSpec.playerJoin]).string

fun Config.formatPlayerLeaveMessage(player: ServerPlayerEntity): String =
    formatDiscordRelayMessage(player, config[ChatRelaySpec.DiscordMessageFormatSpec.playerLeave]).string

fun Config.formatPlayerDeathMessage(player: ServerPlayerEntity, message: Text): String =
    formatDiscordRelayMessage(
        player,
        config[ChatRelaySpec.DiscordMessageFormatSpec.playerDeath],
        mapOf("message" to message)
    ).string

fun Config.formatPlayerAdvancementMessage(player: ServerPlayerEntity, advancement: Advancement): String =
    formatDiscordRelayMessage(
        player,
        config[ChatRelaySpec.DiscordMessageFormatSpec.playerAdvancement],
        mapOf("advancement" to advancement.display!!.title)
    ).string

fun Config.formatServerStartMessage(server: MinecraftServer): String =
    formatDiscordRelayMessage(server, config[ChatRelaySpec.DiscordMessageFormatSpec.serverStart])

fun Config.formatServerStopMessage(server: MinecraftServer): String =
    formatDiscordRelayMessage(server, config[ChatRelaySpec.DiscordMessageFormatSpec.serverStop])

private fun formatDiscordRelayMessage(
    sender: MessageSender, message: String, format: String, placeholders: Map<String, Text> = mapOf(
        "sender" to sender.name,
        "sender_display" to sender.displayName,
        "message" to message.literal()
    )
): String {
    var parsedText = Placeholders.parseText(
        format.literal(),
        Placeholders.ALT_PLACEHOLDER_PATTERN_CUSTOM,
        placeholders
    )
    if (sender is PlayerMessageSender) parsedText = Placeholders.parseText(
        parsedText,
        PlaceholderContext.of(sender.player)
    )
    return parsedText.string
}

fun formatDiscordRelayMessage(
    player: ServerPlayerEntity,
    format: String,
    placeholders: Map<String, Text> = mapOf()
): Text =
    Placeholders.parseText(
        Placeholders.parseText(
            format.literal(),
            Placeholders.ALT_PLACEHOLDER_PATTERN_CUSTOM,
            placeholders
        ),
        PlaceholderContext.of(player)
    )

fun formatDiscordRelayMessage(
    server: MinecraftServer,
    format: String,
    placeholders: Map<String, Text> = mapOf()
): String =
    Placeholders.parseText(
        Placeholders.parseText(
            format.literal(),
            Placeholders.ALT_PLACEHOLDER_PATTERN_CUSTOM,
            placeholders
        ),
        PlaceholderContext.of(server)
    ).string

fun Config.getMinecraftChatRelayMsg(
    sender: MutableText,
    topRole: MutableText,
    message: Text,
    server: MinecraftServer
): Text = Placeholders.parseText(
    Placeholders.parseText(
        TextParserUtils.formatText(this[ChatRelaySpec.MinecraftFormatSpec.messageFormat]),
        Placeholders.ALT_PLACEHOLDER_PATTERN_CUSTOM,
        mapOf(
            "sender" to sender.copy().formatted(Formatting.RESET),
            "sender_colored" to sender,
            "top_role" to topRole,
            "message" to message
        )
    ), PlaceholderContext.of(server)
)

fun Config.getReplyMsg(
    sender: String,
    message: Message,
    server: MinecraftServer
): Text = Placeholders.parseText(
    Placeholders.parseText(
        TextParserUtils.formatText(this[ChatRelaySpec.MinecraftFormatSpec.replyFormat]),
        Placeholders.ALT_PLACEHOLDER_PATTERN_CUSTOM,
        mapOf(
            "sender" to (sender).literal(),
            "summary" to message.summary().literal(),
        )
    ), PlaceholderContext.of(server)
)

fun Config.getWebhookChatRelayAvatar(gameProfile: GameProfile): String =
    Placeholders.parseText(
        this[ChatRelaySpec.WebhookSpec.playerAvatarUrl].literal(),
        Placeholders.ALT_PLACEHOLDER_PATTERN_CUSTOM,
        mapOf(
            "uuid" to gameProfile.id.toString().literal(),
            "username" to gameProfile.name.literal(),
            "texture" to (gameProfile.getTextures()?.literal() ?: Text.empty())
        )
    ).string
