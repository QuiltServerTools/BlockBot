package com.github.quiltservertools.blockbotdiscord.config

import com.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import com.github.quiltservertools.blockbotdiscord.logWarn
import com.github.quiltservertools.blockbotdiscord.utility.literal
import com.github.quiltservertools.blockbotdiscord.utility.summary
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.UnsetValueException
import com.uchuhimo.konf.source.toml
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import eu.pb4.placeholders.PlaceholderAPI
import eu.pb4.placeholders.TextParser
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

const val CONFIG_PATH = "blockbot-discord.toml"

val config = Config {
    addSpec(BotSpec)
    addSpec(ChatRelaySpec)
    addSpec(ConsoleRelaySpec)
}.from.toml.resource(CONFIG_PATH)
    .from.toml.watchFile(FabricLoader.getInstance().configDir.resolve(CONFIG_PATH).toFile())
    .from.env()
    .from.systemProperties()

fun Config.getDiscordChatRelayMsg(player: ServerPlayerEntity, message: String): String =
    PlaceholderAPI.parseText(
        PlaceholderAPI.parsePredefinedText(
            this[ChatRelaySpec.MessageFormatSpec.discordFormat].literal(),
            PlaceholderAPI.ALT_PLACEHOLDER_PATTERN_CUSTOM,
            mapOf("player" to player.displayName, "message" to message.literal())
        ),
        player
    ).string

fun Config.isCorrect() = try {
    this.validateRequired()
    true
} catch (ex: UnsetValueException) {
    BlockBotDiscord.logger.error(ex)
    false
}

fun Config.getMinecraftChatRelayMsg(
    sender: MutableText,
    topRole: MutableText,
    message: Text,
    server: MinecraftServer
): Text = PlaceholderAPI.parseText(
    PlaceholderAPI.parsePredefinedText(
        TextParser.parse(this[ChatRelaySpec.MessageFormatSpec.minecraftFormat]),
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
        TextParser.parse(this[ChatRelaySpec.MessageFormatSpec.replyFormat]),
        PlaceholderAPI.ALT_PLACEHOLDER_PATTERN_CUSTOM,
        mapOf(
            "sender" to (sender).literal(),
            "summary" to message.summary().literal(),
        )
    ), server
)

fun Config.getWebhookChatRelayAvatar(uuid: UUID): String =
    PlaceholderAPI.parsePredefinedText(
        this[ChatRelaySpec.WebhookSpec.playerAvatarUrl].literal(),
        PlaceholderAPI.ALT_PLACEHOLDER_PATTERN_CUSTOM,
        mapOf("uuid" to uuid.toString().literal())
    ).string

fun Config.getChannelsBi(): BiMap<String, Long> = HashBiMap.create(this[BotSpec.channels])

suspend fun Config.getChannel(name: String, bot: ExtensibleBot): GuildMessageChannel {
    val channel: GuildMessageChannel? =
        this[BotSpec.channels][name]?.let { Snowflake(it) }?.let { this.getGuild(bot).getChannelOf(it) }
    if (channel == null) {
        logWarn("Invalid channel '${name}'. Make sure it is defined and correct in your config")
    }

    return channel!!
}

suspend fun Config.getGuild(bot: ExtensibleBot) = bot.getKoin().get<Kord>().getGuild(Snowflake(this[BotSpec.guild]))!!
