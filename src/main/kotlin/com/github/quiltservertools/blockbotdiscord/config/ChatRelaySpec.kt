package com.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.ConfigSpec

object ChatRelaySpec : ConfigSpec() {

    val allowMentions by required<Boolean>()
    val convertMarkdown by required<Boolean>()
    object MinecraftFormatSpec: ConfigSpec() {
        val messageFormat by required<String>()
        val replyFormat by required<String>()
    }
    object DiscordMessageFormatSpec: ConfigSpec() {
        val messageFormat by required<String>()
        val announcementFormat by required<String>()
        val emoteFormat by required<String>()
    }
    object DiscordWebhookFormatSpec: ConfigSpec() {
        val messageFormat by required<String>()
        val announcementFormat by required<String>()
        val emoteFormat by required<String>()
    }
    object WebhookSpec : ConfigSpec() {
        val useWebhook by required<Boolean>()
        val webhookName by required<String>()
        val webhookAvatar by required<String>()
        val playerAvatarUrl by required<String>()
    }
}
