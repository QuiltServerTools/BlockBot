package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
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
): String = Placeholders.parseText(
    this[MemberCommandsSpec.PlayerListSpec.title].literal(),
    PlaceholderContext.of(server)
).string

fun Config.formatPlayerListContent(
    server: MinecraftServer
): String = server.playerManager.playerList.filter { !it.isVanished() }.joinToString {
    Placeholders.parseText(
        this[MemberCommandsSpec.PlayerListSpec.playerFormat].literal(),
        PlaceholderContext.of(it)
    ).string
}
