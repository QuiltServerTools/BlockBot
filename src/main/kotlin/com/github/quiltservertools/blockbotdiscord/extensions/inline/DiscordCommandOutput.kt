package com.github.quiltservertools.blockbotdiscord.extensions.inline

import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import kotlinx.coroutines.runBlocking
import net.minecraft.server.command.CommandOutput
import net.minecraft.text.Text
import java.util.*

class DiscordCommandOutput(private val commandContext: SlashCommandContext<out InlineCommandsExtension.InlineCommandsArgs>) :
    CommandOutput {
    val buffer = StringBuffer()

    override fun sendSystemMessage(message: Text, sender: UUID) {
        val content = message.string

        if (content.isNotEmpty()) {
            if (buffer.isNotEmpty()) {
                buffer.append('\n');
            }
            buffer.append(content);
        }
    }

    override fun shouldReceiveFeedback() = true

    override fun shouldTrackOutput() = true

    override fun shouldBroadcastConsoleToOps() = true

    fun sendBuffer() {
        runBlocking {
            commandContext.ephemeralFollowUp {
                content = if (buffer.isEmpty()) "Done" else buffer.toString().take(2000)
            }
        }
    }
}
