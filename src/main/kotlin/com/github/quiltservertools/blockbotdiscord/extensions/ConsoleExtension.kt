package com.github.quiltservertools.blockbotdiscord.extensions

import com.github.quiltservertools.blockbotapi.Channels
import com.github.quiltservertools.blockbotapi.event.DiscordMessageEvent
import com.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import com.github.quiltservertools.blockbotdiscord.config.config
import com.github.quiltservertools.blockbotdiscord.config.getChannel
import com.kotlindiscord.kord.extensions.extensions.Extension
import kotlinx.coroutines.launch
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import net.minecraft.util.ActionResult
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
                    channel.createMessage(message)
                }
            }
        }

        DiscordMessageEvent.EVENT.register(DiscordMessageEvent { sender, channel, message ->
            if (channel == Channels.CONSOLE && message.isNotEmpty()) {
                (server as MinecraftDedicatedServer).enqueueCommand(message, server.commandSource)
                return@DiscordMessageEvent ActionResult.SUCCESS
            }

            ActionResult.PASS
        })
    }

    companion object {
        val consoleQueue = LinkedBlockingQueue<String>()
    }
}
