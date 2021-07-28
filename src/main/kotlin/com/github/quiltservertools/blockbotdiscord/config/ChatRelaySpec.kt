package com.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.ConfigSpec

object ChatRelaySpec : ConfigSpec() {

    val allowMentions by required<Boolean>()
    val convertEmoji by required<Boolean>()
    val convertMarkdown by required<Boolean>()
    object MessageFormatSpec: ConfigSpec() {
        val discordFormat by required<String>()

        val minecraftFormat by required<String>()
        val replyFormat by required<String>()
    }
    object WebhookSpec : ConfigSpec() {
        val useWebhook by required<Boolean>()
        val webhookName by required<String>()
        val webhookAvatar by required<String>()
        val playerAvatarUrl by required<String>()
    }
}
