@file:OptIn(PrivilegedIntent::class)

package io.github.quiltservertools.blockbotdiscord

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import io.github.quiltservertools.blockbotapi.BlockBotApi
import io.github.quiltservertools.blockbotdiscord.config.*
import io.github.quiltservertools.blockbotdiscord.extensions.BlockBotApiExtension
import io.github.quiltservertools.blockbotdiscord.extensions.ConsoleExtension
import io.github.quiltservertools.blockbotdiscord.extensions.MemberCommandsExtension
import io.github.quiltservertools.blockbotdiscord.extensions.inline.InlineCommandsExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists

object BlockBotDiscord : ModInitializer, CoroutineScope {
    const val MOD_ID = "blockbot-discord"

    val logger: Logger = LogManager.getLogger()
    val CONFIG_FOLDER: Path = FabricLoader.getInstance().configDir.resolve("blockbot")
    lateinit var bot: ExtensibleBot

    @OptIn(ExperimentalSerializationApi::class)
    override fun onInitialize() {
        logInfo("Initializing")

        if (CONFIG_FOLDER.notExists()) {
            CONFIG_FOLDER.createDirectory()
        }
        if (CONFIG_FOLDER.resolve(CONFIG_PATH).notExists()) {
            logInfo("No config file, creating...")
            Files.copy(
                FabricLoader.getInstance().getModContainer(MOD_ID).get().getPath(CONFIG_PATH),
                CONFIG_FOLDER.resolve(CONFIG_PATH)
            )
        }
        if (CONFIG_FOLDER.resolve(MESSAGES_PATH).notExists()) {
            logInfo("No messages file, creating...")
            CONFIG_FOLDER.resolve(MESSAGES_PATH).toFile().writeText(
                Json { prettyPrint = true }.encodeToString(
                    DEFAULT_MESSAGE_CONFIG
                )
            )
        } else {
            messagesConfig = Json { ignoreUnknownKeys = true; isLenient = true } .decodeFromString(CONFIG_FOLDER.resolve(MESSAGES_PATH).toFile().readText())
        }
        if (!config.isCorrect()) {
            logFatal("Config invalid. Disabling mod...")
            return
        }

        ServerLifecycleEvents.SERVER_STARTING.register(BlockBotDiscord::serverStarting)

        DiscordConsoleAppender().start()
    }

    private fun serverStarting(server: MinecraftServer) {
        launch {
            bot = ExtensibleBot(config[BotSpec.token]) {
                messageCommands {
                    enabled = false
                }

                slashCommands {
                    enabled = true
                }

                extensions {
                    add(::BlockBotApiExtension)
                    if (config.getChannelsBi().containsKey("console")) add(::ConsoleExtension)
                    if (config[InlineCommandsSpec.enabled]) add(::InlineCommandsExtension)
                    add(::MemberCommandsExtension)
                }

                intents {
                    +Intents.nonPrivileged
                    +Intent.GuildMembers
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
