package io.github.quiltservertools.blockbotdiscord.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import io.github.quiltservertools.blockbotdiscord.config.*
import net.minecraft.server.MinecraftServer
import net.minecraft.server.WhitelistEntry
import org.koin.core.component.inject
import java.util.*

class MemberCommandsExtension : Extension() {
    override val name = "member commands"

    private val server: MinecraftServer by inject()

    override suspend fun setup() {
        if (config[MemberCommandsSpec.PlayerListSpec.enabled]) {
            ephemeralSlashCommand {
                name = config[MemberCommandsSpec.PlayerListSpec.name]
                description = config[MemberCommandsSpec.PlayerListSpec.description]

                guild(config.getGuild(bot))

                action {
                    respond {
                        embed {
                            title = config.formatPlayerListTitle(server)
                            description = config.formatPlayerListContent(server)
                        }
                    }
                }
            }
        }

        if (config[MemberCommandsSpec.WhiteListSpec.enabled]) {
            publicSlashCommand(::WhitelistArgs) {
                name = config[MemberCommandsSpec.WhiteListSpec.name]
                description = config[MemberCommandsSpec.WhiteListSpec.description]

                guild(config.getGuild(bot))

                action {
                    val profile = server.userCache?.findByName(arguments.player)?.unwrap()
                    if (profile == null) {
                        respond {
                            content = config[MemberCommandsSpec.WhiteListSpec.MessagesSpec.unknownPlayer].replace(
                                "{player}",
                                arguments.player
                            )
                        }
                        return@action
                    }

                    if (server.playerManager.isWhitelisted(profile)) {
                        respond {
                            content = config[MemberCommandsSpec.WhiteListSpec.MessagesSpec.alreadyWhiteListed]
                        }
                    } else {
                        server.playerManager.whitelist.add(WhitelistEntry(profile))
                        respond {
                            content = config[MemberCommandsSpec.WhiteListSpec.MessagesSpec.successful].replace(
                                "{player}",
                                arguments.player
                            )
                        }
                    }
                }
            }
        }
    }

    inner class WhitelistArgs : Arguments() {
        val player by string {
            name = config[MemberCommandsSpec.WhiteListSpec.PlayerArgumentSpec.name]
            description = config[MemberCommandsSpec.WhiteListSpec.PlayerArgumentSpec.description]
        }
    }
}

fun <T> Optional<T>.unwrap(): T? = orElse(null)
