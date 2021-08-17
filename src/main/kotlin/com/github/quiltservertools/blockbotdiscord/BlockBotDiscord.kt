package com.github.quiltservertools.blockbotdiscord

import com.github.quiltservertools.blockbotapi.BlockBotApi
import com.github.quiltservertools.blockbotdiscord.config.*
import com.github.quiltservertools.blockbotdiscord.extensions.BlockBotApiExtension
import com.github.quiltservertools.blockbotdiscord.extensions.ConsoleExtension
import com.github.quiltservertools.blockbotdiscord.extensions.inline.InlineCommandsExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files

@OptIn(PrivilegedIntent::class)
object BlockBotDiscord : ModInitializer, CoroutineScope {
    const val MOD_ID = "blockbot-discord"

    val logger: Logger = LogManager.getLogger()
    lateinit var bot: ExtensibleBot

    override fun onInitialize() {
        logInfo("Initializing")

        if (!Files.exists(FabricLoader.getInstance().configDir.resolve(CONFIG_PATH))) {
            logInfo("No config file, creating...")
            Files.copy(
                FabricLoader.getInstance().getModContainer(MOD_ID).get().getPath(CONFIG_PATH),
                FabricLoader.getInstance().configDir.resolve(CONFIG_PATH)
            )
        }
        if (!config.isCorrect()) {
            logFatal("Config invalid. Disabling mod...")
            return
        }

        ServerLifecycleEvents.SERVER_STARTING.register(::serverStarting)

        DiscordConsoleAppender().start()
    }

    private fun serverStarting(server: MinecraftServer) {
        launch {
            bot = ExtensibleBot(config[BotSpec.token]) {
                slashCommands {
                    enabled = true
                }

                extensions {
                    add(::BlockBotApiExtension)
                    if (config.getChannelsBi().containsKey("console")) add(::ConsoleExtension)
                    if (config[InlineCommandsSpec.enabled]) add(::InlineCommandsExtension)
                }

                intents {
                    +Intents.all
                }

                hooks {
                    afterKoinSetup {
                        loadModule {
                            single { server }
                        }
                    }
                }
            }
            BlockBotApi.registerBot(bot.findExtension())

            bot.start()
        }
    }

    override val coroutineContext = Dispatchers.Default
}

fun logDebug(message: String) = BlockBotDiscord.logger.debug(message)
fun logInfo(message: String) = BlockBotDiscord.logger.info(message)
fun logWarn(message: String) = BlockBotDiscord.logger.warn(message)
fun logFatal(message: String) = BlockBotDiscord.logger.fatal(message)
