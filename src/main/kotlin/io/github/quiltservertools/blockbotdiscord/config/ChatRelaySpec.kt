package io.github.quiltservertools.blockbotdiscord.config

import com.mojang.authlib.GameProfile
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import dev.kord.core.entity.Message
import eu.pb4.placeholders.PlaceholderAPI
import eu.pb4.placeholders.TextParser
import io.github.quiltservertools.blockbotapi.sender.MessageSender
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender
import io.github.quiltservertools.blockbotdiscord.utility.getTextures
import io.github.quiltservertools.blockbotdiscord.utility.literal
import io.github.quiltservertools.blockbotdiscord.utility.summary
import net.minecraft.advancement.Advancement
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
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
    }

    object WebhookSpec : ConfigSpec() {
        val useWebhook by required<Boolean>()
        val webhookName by required<String>()
        val playerAvatarUrl by required<String>()
    }
}

fun Config.getMinecraftChatRelayMsg(
    sender: MutableText,
    topRole: MutableText,
    message: Text,
    server: MinecraftServer
): Text = PlaceholderAPI.parseText(
    PlaceholderAPI.parsePredefinedText(
        TextParser.parse(this[ChatRelaySpec.MinecraftFormatSpec.messageFormat]),
        PlaceholderAPI.ALT_PLACEHOLDER_PATTERN_CUSTOM,
        mapOf(
            "sender" to sender.copy().formatted(Formatting.RESET),
            "sender_colored" to sender,
            "top_role" to topRole,
            "message" to message
        )
    ), server
)

fun Config.getReplyMsg(
    sender: String,
    message: Message,
    server: MinecraftServer
): Text = PlaceholderAPI.parseText(
    PlaceholderAPI.parsePredefinedText(
        TextParser.parse(this[ChatRelaySpec.MinecraftFormatSpec.replyFormat]),
        PlaceholderAPI.ALT_PLACEHOLDER_PATTERN_CUSTOM,
        mapOf(
            "sender" to (sender).literal(),
            "summary" to message.summary().literal(),
        )
    ), server
)

fun Config.getWebhookChatRelayAvatar(gameProfile: GameProfile): String =
    PlaceholderAPI.parsePredefinedText(
        this[ChatRelaySpec.WebhookSpec.playerAvatarUrl].literal(),
        PlaceholderAPI.ALT_PLACEHOLDER_PATTERN_CUSTOM,
        mapOf(
            "uuid" to gameProfile.id.toString().literal(),
            "username" to gameProfile.name.literal(),
            "texture" to (gameProfile.getTextures()?.literal() ?: LiteralText(""))
        )
    ).string
