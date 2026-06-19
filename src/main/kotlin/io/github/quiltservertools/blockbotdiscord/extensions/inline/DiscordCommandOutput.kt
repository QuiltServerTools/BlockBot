package io.github.quiltservertools.blockbotdiscord.extensions.inline

import dev.kordex.core.commands.application.slash.EphemeralSlashCommandContext
import kotlinx.coroutines.runBlocking
import net.minecraft.commands.CommandSource
import net.minecraft.network.chat.Component

class DiscordCommandOutput(private val commandContext: EphemeralSlashCommandContext<out InlineCommandsExtension.InlineCommandsArgs, *>) :
    CommandSource {
    private val buffer = StringBuffer()

    override fun sendSystemMessage(message: Component) {
        val content = message.string

        if (content.isNotEmpty()) {
            if (buffer.isNotEmpty()) {
                buffer.append('\n')
            }
            buffer.append(content)
        }
    }

    override fun acceptsSuccess() = true

    override fun acceptsFailure() = true

    override fun shouldInformAdmins() = true

    fun sendBuffer() {
        runBlocking {
            commandContext.respond {
                content = if (buffer.isEmpty()) "Done" else buffer.toString().take(2000)
            }
        }
    }
}
