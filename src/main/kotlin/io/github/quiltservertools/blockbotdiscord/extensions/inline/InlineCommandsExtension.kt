package io.github.quiltservertools.blockbotdiscord.extensions.inline

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import io.github.quiltservertools.blockbotdiscord.config.InlineCommandsSpec
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.getGuild
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
            name = "mc"
            description = "Run a command in game"

            guild(config.getGuild(bot))
            allowByDefault = false

            action {
                val serverWorld: ServerWorld? = server.overworld
                val output = DiscordCommandOutput(this)
                val source = ServerCommandSource(
                    output,
                    if (serverWorld == null) Vec3d.ZERO else Vec3d.of(serverWorld.spawnPos),
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
            name = "command"
            description = "The command to run"}
    }
}
