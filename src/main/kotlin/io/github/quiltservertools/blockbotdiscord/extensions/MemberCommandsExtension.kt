package io.github.quiltservertools.blockbotdiscord.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import io.github.quiltservertools.blockbotdiscord.config.MemberCommandsSpec
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.getGuild
import net.minecraft.server.MinecraftServer
import net.minecraft.server.WhitelistEntry
import org.koin.core.component.inject
import java.util.*

class MemberCommandsExtension : Extension() {
    override val name = "member commands"

    private val server: MinecraftServer by inject()

    override suspend fun setup() {
        if (config[MemberCommandsSpec.playerList]) {
            ephemeralSlashCommand {
                name = "playerlist"
                description = "Gets the online players"

                guild(config.getGuild(bot))

                action {
                    respond {
                        embed {
                            title = "${server.playerManager.playerList.size}/${server.playerManager.maxPlayerCount}"
                            description = server.playerManager.playerList.joinToString(", ") { it.name.string }
                        }
                    }
                }
            }
        }

        if (config[MemberCommandsSpec.whitelist]) {
            publicSlashCommand(::WhitelistArgs) {
                name = "whitelist"
                description = "whitelists a player"

                guild(config.getGuild(bot))

                action {
                    val profile = server.userCache.findByName(arguments.player).unwrap()
                    if (profile == null) {
                        respond {
                            content = "Unknown player: ${arguments.player}"
                        }
                        return@action
                    }

                    if (server.playerManager.isWhitelisted(profile)) {
                        respond {
                            content = "Player already whitelisted"
                        }
                    } else {
                        server.playerManager.whitelist.add(WhitelistEntry(profile))
                        respond {
                            content = "Whitelisted ${arguments.player}"
                        }
                    }
                }
            }
        }
    }

    inner class WhitelistArgs : Arguments() {
        val player by string {
            name = "username"
            description = "The username of the player to whitelist"
        }
    }
}

fun <T> Optional<T>.unwrap(): T? = orElse(null)
