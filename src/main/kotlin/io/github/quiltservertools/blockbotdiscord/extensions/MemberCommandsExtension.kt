package io.github.quiltservertools.blockbotdiscord.extensions

import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.embed
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

    @OptIn(KordPreview::class)
    override suspend fun setup() {
        if (config[MemberCommandsSpec.playerList]) {
            slashCommand {
                name = "playerlist"
                description = "Gets the online players"

                guild(config.getGuild(bot))

                action {
                    ephemeralFollowUp {
                        embed {
                            title = "${server.playerManager.playerList.size}/${server.playerManager.maxPlayerCount}"
                            description = server.playerManager.playerList.joinToString(", ") { it.name.string }
                        }
                    }
                }
            }
        }

        if (config[MemberCommandsSpec.whitelist]) {
            slashCommand(::WhitelistArgs) {
                name = "whitelist"
                description = "whitelists a player"

                guild(config.getGuild(bot))
                autoAck = AutoAckType.PUBLIC

                action {
                    val profile = server.userCache.findByName(arguments.player).unwrap()
                    if (profile == null) {
                        publicFollowUp {
                            content = "Unknown player: ${arguments.player}"
                        }
                        return@action
                    }

                    if (server.playerManager.isWhitelisted(profile)) {
                        publicFollowUp {
                            content = "Player already whitelisted"
                        }
                    } else {
                        server.playerManager.whitelist.add(WhitelistEntry(profile))
                        publicFollowUp {
                            content = "Whitelisted ${arguments.player}"
                        }
                    }
                }
            }
        }
    }

    inner class WhitelistArgs : Arguments() {
        val player by string("player", "The name of the player to whitelist")
    }
}

fun <T> Optional<T>.unwrap(): T? = orElse(null)
