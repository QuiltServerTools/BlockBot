package com.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.ConfigSpec

object ChatRelaySpec : ConfigSpec() {
    val discordFormat by required<String>()
    val minecraftFormat by required<String>()
    val allowMentions by required<Boolean>()
    object WebhookSpec : ConfigSpec() {
        val useWebhook by required<Boolean>()
        val webhookName by required<String>()
        val avatarUrl by required<String>()
    }
}
