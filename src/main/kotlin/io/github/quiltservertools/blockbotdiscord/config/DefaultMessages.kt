package io.github.quiltservertools.blockbotdiscord.config

import dev.kord.common.Color
import dev.kord.common.entity.optional.optional
import dev.kord.rest.json.request.EmbedFieldRequest
import dev.kord.rest.json.request.EmbedRequest
import dev.kord.rest.json.request.MessageCreateRequest
import dev.kord.rest.json.request.WebhookExecuteRequest
import kotlinx.datetime.Clock

data class MessageConfig(
    val help: String,
    val example: String,
    val botMessages: Map<String, MessageCreateRequest>,
    val webhookMessages: WebhookExecuteRequest
)

val defaultBotMessages = mapOf(
    "example" to MessageCreateRequest(
        content = "**test** ok".optional(),
        tts = true.optional(),
        embed = EmbedRequest(
            title = "aaa".optional(),
            type = "rich".optional(),
            description = "description".optional(),
            url = "google.com".optional(),
            timestamp = Clock.System.now().optional(),
            color = Color(20, 30, 60).optional(),
            fields = listOf(
                EmbedFieldRequest(
                    name = "Changes",
                    value = "test",
                    inline = true.optional()
                )
            ).optional()
        ).optional(),
    ),
    "chatMessage" to MessageCreateRequest(
        content = "{sender_display} » {message}".optional()
    ),
    "announcementFormat" to MessageCreateRequest(
        content = "{sender_display} » **{message}**".optional()
    ),
    "emoteFormat" to MessageCreateRequest(
        content = "*{sender_display} {message}*".optional()
    ),
    "playerJoin" to MessageCreateRequest(
        content = "%player:displayname% joined the game".optional()
    )
)
