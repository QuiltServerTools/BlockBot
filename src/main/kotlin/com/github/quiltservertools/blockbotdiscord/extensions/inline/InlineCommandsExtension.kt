package com.github.quiltservertools.blockbotdiscord.extensions.inline

import com.github.quiltservertools.blockbotdiscord.config.InlineCommandsSpec
import com.github.quiltservertools.blockbotdiscord.config.config
import com.github.quiltservertools.blockbotdiscord.config.getGuild
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.entity.Snowflake
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.koin.core.component.inject

class InlineCommandsExtension : Extension() {
    override val name: String = "extensions"

    private val server: MinecraftServer by inject()

    override suspend fun setup() {
        slashCommand(::InlineCommandsArgs) {
            name = "mc"
            description = "Run a command in game"

            guild(config.getGuild(bot))
            allowByDefault = false
            allowedRoles.addAll(config[InlineCommandsSpec.allowedRoles].map { Snowflake(it) })

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
                    LiteralText(member!!.asMember().tag),
                    server,
                    null
                )

                (server as MinecraftDedicatedServer).commandManager.execute(source, arguments.command)
                output.sendBuffer()
            }
        }
    }

    class InlineCommandsArgs : Arguments() {
        val command by string("command", "The command to run (gamemode creative steve)")
    }
}
