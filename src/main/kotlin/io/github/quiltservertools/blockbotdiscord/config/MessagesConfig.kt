package io.github.quiltservertools.blockbotdiscord.config

import dev.kord.common.entity.optional.Optional
import dev.kord.rest.json.request.EmbedRequest
import dev.kord.rest.json.request.MessageCreateRequest
import kotlinx.serialization.Serializable

@Serializable
class MessagesConfig {
    val webhookChatFormat = MessageCreateRequest(
        content = Optional.invoke("**test** ok"),
        embed = Optional.invoke(EmbedRequest(title = Optional.invoke("aaa")))
    )
}
