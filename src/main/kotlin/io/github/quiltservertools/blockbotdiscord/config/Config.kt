package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.UnsetValueException
import com.uchuhimo.konf.source.toml
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import net.fabricmc.loader.api.FabricLoader

const val CONFIG_PATH = "blockbot-discord.toml"

val config = Config {
    addSpec(BotSpec)
    addSpec(ChatRelaySpec)
    addSpec(ConsoleRelaySpec)
    addSpec(InlineCommandsSpec)
    addSpec(PresenceSpec)
    addSpec(MemberCommandsSpec)
    addSpec(LinkingSpec)
}.from.toml.resource(CONFIG_PATH)
    .from.toml.resource(CONFIG_PATH)
    .from.toml.watchFile(FabricLoader.getInstance().configDir.resolve(CONFIG_PATH).toFile())
    .from.env()
    .from.systemProperties()

fun Config.isCorrect() = try {
    this.validateRequired()
    true
} catch (ex: UnsetValueException) {
    BlockBotDiscord.logger.error(ex)
    false
}
