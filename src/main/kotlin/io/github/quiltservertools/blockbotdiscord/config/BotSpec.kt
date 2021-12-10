package io.github.quiltservertools.blockbotdiscord.config

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import io.github.quiltservertools.blockbotdiscord.logWarn

object BotSpec : ConfigSpec() {
    val token by required<String>()
    val guild by required<ULong>()
    val channels by required<Map<String, ULong>>()
}

fun Config.getChannelsBi(): BiMap<String, ULong> = HashBiMap.create(this[BotSpec.channels])

suspend fun Config.getChannel(name: String, bot: ExtensibleBot): GuildMessageChannel {
    val channel: GuildMessageChannel? =
        this[BotSpec.channels][name]?.let { Snowflake(it) }?.let { this.getGuild(bot).getChannelOf(it) }
    if (channel == null) {
        logWarn("Invalid channel '${name}'. Make sure it is defined and correct in your config")
    }

    return channel!!
}

suspend fun Config.getGuild(bot: ExtensibleBot) = bot.getKoin().get<Kord>().getGuild(Snowflake(this[BotSpec.guild]))!!
val Config.guildId: Snowflake
    get() = Snowflake(this[BotSpec.guild])

