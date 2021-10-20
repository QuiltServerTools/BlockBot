package io.github.quiltservertools.blockbotdiscord.config

import dev.kord.common.Color
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.logInfo
import io.github.quiltservertools.blockbotdiscord.utility.Colors
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.notExists

@Serializable
data class WebhookFormat(
    val example: WebhookMessage,

    val chat: WebhookMessage,
    val announcement: WebhookMessage,
    val emote: WebhookMessage,
    val join: WebhookMessage,
    val leave: WebhookMessage,
    val advancement: WebhookMessage,
    val death: WebhookMessage,
    val serverStart: WebhookMessage,
    val serverStop: WebhookMessage
)

@Serializable
data class RegularFormat(
    val example: RegularMessage,

    val chat: RegularMessage,
    val announcement: RegularMessage,
    val emote: RegularMessage,
    val join: RegularMessage,
    val leave: RegularMessage,
    val advancement: RegularMessage,
    val death: RegularMessage,
    val serverStart: RegularMessage,
    val serverStop: RegularMessage
)

@Serializable
data class MessageConfig(
    @SerialName("DO_NOT_TOUCH")
    val version: Int,
    val help: String,
    val webhookFormat: WebhookFormat,
    val regularFormat: RegularFormat
)

val DEFAULT_MESSAGE_CONFIG = MessageConfig(
    version = 0,
    help = "Check docs for explanation: docs.com",
    webhookFormat = WebhookFormat(
        example = WebhookMessage(
            content = "The regular content part of message",
            username = "The webhooks username for this message",
            avatar = "URL to use for webhook avatar for this message",
            tts = false,
            embeds = listOf(
                MessageEmbed(
                    title = "Title of embed (Can't have discord emojis)",
                    description = "Description of embed",
                    url = "Url of embed",
                    color = Colors.green,
                    footer = EmbedFooter(
                        text = "Text for embed footer",
                        iconUrl = "Url for embed footer icon"
                    ),
                    image = "Embed image url ",
                    thumbnail = "Embed thumbnail image url",
                    author = EmbedAuthor(
                        name = "Embed author",
                        url = "Embed author link url",
                        iconUrl = "Embed author icon url"
                    ),
                    fields = listOf(
                        EmbedField(
                            name = "Embed field name",
                            value = "Embed field content",
                            inline = false
                        ),
                        EmbedField(
                            name = "Another embed field name",
                            value = "Can have more than one"
                        )
                    )
                ),
            )
        ),
        chat = WebhookMessage(
            username = "{sender_display}",
            avatar = "{sender_avatar}",
            content = "{message}"
        ),
        announcement = WebhookMessage(
            username = "{sender_display}",
            avatar = "{sender_avatar}",
            content = "**{message}**"
        ),
        emote = WebhookMessage(
            username = "{sender_display}",
            avatar = "{sender_avatar}",
            content = "*{sender_display} {message}*"
        ),
        advancement = WebhookMessage(
            embeds = listOf(
                MessageEmbed(
                    author = EmbedAuthor(
                        name = "{sender_display} has made the advancement [{advancement}]",
                        iconUrl = "{sender_avatar}"
                    ),
                    color = Colors.blue
                )
            )
        ),
        join = WebhookMessage(
            embeds = listOf(
                MessageEmbed(
                    author = EmbedAuthor(
                        name = "{sender_display} joined the game",
                        iconUrl = "{player_avatar}"
                    ),
                    color = Colors.green
                )
            )
        ),
        leave = WebhookMessage(
            embeds = listOf(
                MessageEmbed(
                    author = EmbedAuthor(
                        name = "{sender_display} left the game",
                        iconUrl = "{player_avatar}"
                    ),
                    color = Colors.red
                )
            )
        ),
        death = WebhookMessage(
            embeds = listOf(
                MessageEmbed(
                    author = EmbedAuthor(
                        name = "{death_message}",
                        iconUrl = "{player_avatar}"
                    ),
                    color = Colors.orange
                )
            )
        ),
        serverStart = WebhookMessage(
            embeds = listOf(
                MessageEmbed(
                    title = ":green_circle: Server Started",
                    color = Colors.green
                )
            )
        ),
        serverStop = WebhookMessage(
            embeds = listOf(
                MessageEmbed(
                    title = ":octagonal_sign: Server Stopped",
                    color = Colors.red
                )
            )
        )
    ),
    regularFormat = RegularFormat(
        example = RegularMessage(
            content = "The regular content part of message",
            embed = MessageEmbed(
                title = "Title of embed (Can't have discord emojis)",
                description = "Description of embed",
                url = "Url of embed",
                color = Colors.green,
                footer = EmbedFooter(
                    text = "Text for embed footer",
                    iconUrl = "Url for embed footer icon"
                ),
                image = "Embed image url ",
                thumbnail = "Embed thumbnail image url",
                author = EmbedAuthor(
                    name = "Embed author",
                    url = "Embed author link url",
                    iconUrl = "Embed author icon url"
                ),
                fields = listOf(
                    EmbedField(
                        name = "Embed field name",
                        value = "Embed field content",
                        inline = false
                    ),
                    EmbedField(
                        name = "Another embed field name",
                        value = "Can have more than one"
                    )
                )
            ),
        ),
        chat = RegularMessage(
            content = "{sender_display} » {message}"
        ),
        announcement = RegularMessage(
            content = "{sender_display} » **{message}**"
        ),
        emote = RegularMessage(
            content = "*{sender_display} {message}*"
        ),
        join = RegularMessage(
            embed = MessageEmbed(
                author = EmbedAuthor(
                    name = "{sender_display} joined the game",
                    iconUrl = "{player_avatar}"
                ),
                color = Colors.green
            )
        ),
        leave = RegularMessage(
            embed = MessageEmbed(
                author = EmbedAuthor(
                    name = "{sender_display} left the game",
                    iconUrl = "{player_avatar}"
                ),
                color = Colors.red
            )
        ),
        advancement = RegularMessage(
            embed = MessageEmbed(
                author = EmbedAuthor(
                    name = "{sender_display} has made the advancement [{advancement}]",
                    iconUrl = "{sender_avatar}"
                ),
                color = Colors.blue
            )
        ),
        death = RegularMessage(
            embed = MessageEmbed(
                author = EmbedAuthor(
                    name = "{death_message}",
                    iconUrl = "{player_avatar}"
                ),
                color = Colors.orange
            )
        ),
        serverStart = RegularMessage(
            embed = MessageEmbed(
                title = ":green_circle: Server Started",
                color = Colors.green
            )
        ),
        serverStop = RegularMessage(
            embed = MessageEmbed(
                title = ":octagonal_sign: Server Stopped",
                color = Colors.red
            )
        )
    )
)

//TODO maybe make these java records and yeet them into the API

@Serializable
data class WebhookMessage(
    val content: String? = null,
    val username: String? = null,
    val avatar: String? = null,
    val tts: Boolean? = null,
    val embeds: List<MessageEmbed> = listOf()
)

@Serializable
data class RegularMessage(
    val content: String? = null,
    val tts: Boolean? = null,
    val embed: MessageEmbed? = null
)

@Serializable
data class MessageEmbed(
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val color: Color? = null,
    val footer: EmbedFooter? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    val author: EmbedAuthor? = null,
    val fields: List<EmbedField> = listOf(),
)

@Serializable
data class EmbedField(
    val name: String,
    val value: String,
    val inline: Boolean? = null,
)

@Serializable
data class EmbedFooter(
    val text: String,
    val iconUrl: String? = null,
)

@Serializable
data class EmbedAuthor(
    val name: String? = null,
    val url: String? = null,
    val iconUrl: String? = null,
)

private val JSON = Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true; }

fun loadMessages(): MessageConfig {
    return if (BlockBotDiscord.CONFIG_FOLDER.resolve(MESSAGES_PATH).notExists()) {
        logInfo("No messages file, creating...")
        createMessagesFile()
        loadMessages()
    } else {
        JSON.decodeFromString(BlockBotDiscord.CONFIG_FOLDER.resolve(MESSAGES_PATH).toFile().readText())
    }
}

private fun createMessagesFile() {
    BlockBotDiscord.CONFIG_FOLDER.resolve(MESSAGES_PATH).toFile().writeText(
        JSON.encodeToString(
            DEFAULT_MESSAGE_CONFIG
        )
    )
}
