package io.github.quiltservertools.blockbotdiscord.extensions

import dev.kordex.core.extensions.Extension
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import io.github.quiltservertools.blockbotapi.Channels
import io.github.quiltservertools.blockbotapi.event.RelayMessageEvent
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.config.ConsoleRelaySpec
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.getChannel
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import net.minecraft.server.permissions.LevelBasedPermissionSet
import net.minecraft.server.MinecraftServer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.koin.core.component.inject
import java.util.concurrent.LinkedBlockingQueue

class ConsoleExtension : Extension() {
    override val name = "console"

    private val server: MinecraftServer by inject()

    override suspend fun setup() {
        BlockBotDiscord.launch {
            val channel = config.getChannel(Channels.CONSOLE, bot)

            while (true) {
                val deadline = System.nanoTime() + (DateTimeUnit.SECOND * 60).nanoseconds
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
                if (config[ConsoleRelaySpec.requireAdmin] && !sender.admin) return@register InteractionResult.FAIL

                val serverWorld: ServerLevel = server.overworld()
                val source = CommandSourceStack(
                    server,
                    Vec3.ZERO,
                    Vec2.ZERO,
                    serverWorld,
                    LevelBasedPermissionSet.OWNER,
                    sender.id,
                    Component.literal(sender.id),
                    server,
                    null
                )

                (server as DedicatedServer).handleConsoleInput(message, source)
                return@register InteractionResult.SUCCESS
            }

            InteractionResult.PASS
        }
    }

    companion object {
        private val consoleQueue = LinkedBlockingQueue<String>()
        fun addToQueue(message: String) {
            consoleQueue.addAll(message.chunked(2000))
        }
    }
}
