package io.github.quiltservertools.blockbotdiscord.extensions.inline

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.i18n.Key
import io.github.quiltservertools.blockbotdiscord.config.InlineCommandsSpec
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.guildId
import net.minecraft.server.permissions.LevelBasedPermissionSet
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.server.MinecraftServer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.koin.core.component.inject

class InlineCommandsExtension : Extension() {
    override val name: String = "extensions"

    private val server: MinecraftServer by inject()

    override suspend fun setup() {
        ephemeralSlashCommand(::InlineCommandsArgs) {
            name = Key("mc")
            description = Key("Run a command in game")

            guild(config.guildId)
            allowByDefault = false

            action {
                val serverWorld: ServerLevel = server.overworld()
                val output = DiscordCommandOutput(this)
                val source = CommandSourceStack(
                    output,
                    Vec3.ZERO,
                    Vec2.ZERO,
                    serverWorld,
                    LevelBasedPermissionSet.forLevel(PermissionLevel.byId(config[InlineCommandsSpec.opLevel])),
                    member!!.asMember().tag,
                    Component.literal(member!!.asMember().tag),
                    server,
                    null
                )

                (server as DedicatedServer).commands.performPrefixedCommand(source, arguments.command)
                output.sendBuffer()
            }
        }
    }

    inner class InlineCommandsArgs : Arguments() {
        val command by string {
            name = Key("command")
            description = Key("The command to run")
        }
    }
}
