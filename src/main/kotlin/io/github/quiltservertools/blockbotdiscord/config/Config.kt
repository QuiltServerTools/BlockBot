package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.UnsetValueException
import com.uchuhimo.konf.source.toml
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

const val CONFIG_PATH = "blockbot-discord.toml"
const val MESSAGES_PATH = "messages.json"

val config = Config {
    addSpec(BotSpec)
    addSpec(ChatRelaySpec)
    addSpec(ConsoleRelaySpec)
    addSpec(InlineCommandsSpec)
    addSpec(PresenceSpec)
    addSpec(MemberCommandsSpec)
}.from.toml.resource(CONFIG_PATH)
    .from.toml.watchFile(BlockBotDiscord.CONFIG_FOLDER.resolve(CONFIG_PATH).toFile())
    .from.env()
    .from.systemProperties()

fun Config.isCorrect() = try {
    this.validateRequired()
    true
} catch (ex: UnsetValueException) {
    BlockBotDiscord.logger.error(ex)
    false
}
