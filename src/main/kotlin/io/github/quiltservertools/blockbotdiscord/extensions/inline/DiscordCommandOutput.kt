package io.github.quiltservertools.blockbotdiscord.extensions.inline

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.runBlocking
import net.minecraft.server.command.CommandOutput
import net.minecraft.text.Text

class DiscordCommandOutput(private val commandContext: EphemeralSlashCommandContext<out InlineCommandsExtension.InlineCommandsArgs>) :
    CommandOutput {
    private val buffer = StringBuffer()

    override fun sendMessage(message: Text) {
        val content = message.string

        if (content.isNotEmpty()) {
            if (buffer.isNotEmpty()) {
                buffer.append('\n')
            }
            buffer.append(content)
        }
    }

    override fun shouldReceiveFeedback() = true

    override fun shouldTrackOutput() = true

    override fun shouldBroadcastConsoleToOps() = true

    fun sendBuffer() {
        runBlocking {
            commandContext.respond {
                content = if (buffer.isEmpty()) "Done" else buffer.toString().take(2000)
            }
        }
    }
}
