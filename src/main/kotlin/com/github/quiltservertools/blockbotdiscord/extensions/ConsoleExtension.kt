package com.github.quiltservertools.blockbotdiscord.extensions

import com.github.quiltservertools.blockbotapi.Channels
import com.github.quiltservertools.blockbotapi.event.RelayMessageEvent
import com.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import com.github.quiltservertools.blockbotdiscord.config.ConsoleRelaySpec
import com.github.quiltservertools.blockbotdiscord.config.config
import com.github.quiltservertools.blockbotdiscord.config.getChannel
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import kotlinx.coroutines.launch
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.koin.core.component.inject
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ConsoleExtension : Extension() {
    override val name = "console"

    private val server: MinecraftServer by inject()

    override suspend fun setup() {
        BlockBotDiscord.launch {
            val channel = config.getChannel(Channels.CONSOLE, bot)

            while (true) {
                val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10L)
                var message = ""

                while (message.length <= 2000) {
                    val remainingTime = deadline - System.nanoTime()
                    if (remainingTime <= 0) break

                    if (consoleQueue.peek()?.let { (message + it).length <= 2000 } == true) {
                        message += consoleQueue.poll()
                    } else {
                        break
                    }
                }

                if (message.isNotEmpty()) {
                    channel.createMessage {
                        allowedMentions = AllowedMentionsBuilder()
                        content = message
                    }
                }
            }
        }

        RelayMessageEvent.EVENT.register { sender, channel, message ->
            if (channel == Channels.CONSOLE && message.isNotEmpty()) {
                if (config[ConsoleRelaySpec.requireAdmin] && !sender.admin) return@register ActionResult.FAIL

                val serverWorld: ServerWorld? = server.overworld
                val source = ServerCommandSource(
                    server,
                    if (serverWorld == null) Vec3d.ZERO else Vec3d.of(serverWorld.spawnPos),
                    Vec2f.ZERO,
                    serverWorld,
                    4,
                    sender.id,
                    LiteralText(sender.id),
                    server,
                    null
                )

                (server as MinecraftDedicatedServer).enqueueCommand(message, source)
                return@register ActionResult.SUCCESS
            }

            ActionResult.PASS
        }
    }

    companion object {
        val consoleQueue = LinkedBlockingQueue<String>()
    }
}
