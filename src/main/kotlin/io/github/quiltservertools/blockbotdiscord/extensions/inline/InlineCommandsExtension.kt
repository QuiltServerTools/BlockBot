package io.github.quiltservertools.blockbotdiscord.extensions.inline

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.i18n.types.Key
import io.github.quiltservertools.blockbotdiscord.config.InlineCommandsSpec
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.guildId
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
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
                val serverWorld: ServerWorld? = server.overworld
                val output = DiscordCommandOutput(this)
                val source = ServerCommandSource(
                    output,
                    Vec3d.ZERO,
                    Vec2f.ZERO,
                    serverWorld,
                    config[InlineCommandsSpec.opLevel],
                    member!!.asMember().tag,
                    Text.literal(member!!.asMember().tag),
                    server,
                    null
                )

                (server as MinecraftDedicatedServer).commandManager.executeWithPrefix(source, arguments.command)
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
