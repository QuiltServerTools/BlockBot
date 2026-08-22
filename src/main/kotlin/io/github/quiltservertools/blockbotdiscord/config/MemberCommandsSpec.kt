package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.ServerPlaceholderContext
import io.github.quiltservertools.blockbotdiscord.utility.isVanished
import io.github.quiltservertools.blockbotdiscord.utility.literal
import net.minecraft.server.MinecraftServer

object MemberCommandsSpec : ConfigSpec() {
    object PlayerListSpec : ConfigSpec() {
        val enabled by required<Boolean>()
        val name by required<String>()
        val description by required<String>()
        val title by required<String>()
        val playerFormat by required<String>()
    }

    object WhiteListSpec : ConfigSpec() {
        val enabled by required<Boolean>()
        val name by required<String>()
        val description by required<String>()
        object MessagesSpec : ConfigSpec() {
            val unknownPlayer by required<String>()
            val alreadyWhiteListed by required<String>()
            val successful by required<String>()
        }
        object PlayerArgumentSpec : ConfigSpec() {
            val name by required<String>()
            val description by required<String>()
        }
    }
}

fun Config.formatPlayerListTitle(
    server: MinecraftServer
): String = Placeholders.SERVER_PLACEHOLDER_PARSER.parseComponent(
    this[MemberCommandsSpec.PlayerListSpec.title],
    ServerPlaceholderContext.of(server).asParserContext()
).string

fun Config.formatPlayerListContent(
    server: MinecraftServer
): String = server.playerList.players.filter { !it.isVanished() }.joinToString {
    Placeholders.SERVER_PLACEHOLDER_PARSER.parseComponent(
        this[MemberCommandsSpec.PlayerListSpec.playerFormat],
        ServerPlaceholderContext.of(it).asParserContext()
    ).string
}
