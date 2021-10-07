package io.github.quiltservertools.blockbotdiscord

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.common.Color
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.optional
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.json.request.EmbedFieldRequest
import dev.kord.rest.json.request.EmbedRequest
import dev.kord.rest.json.request.MessageCreateRequest
import io.github.quiltservertools.blockbotapi.BlockBotApi
import io.github.quiltservertools.blockbotdiscord.config.*
import io.github.quiltservertools.blockbotdiscord.extensions.BlockBotApiExtension
import io.github.quiltservertools.blockbotdiscord.extensions.ConsoleExtension
import io.github.quiltservertools.blockbotdiscord.extensions.MemberCommandsExtension
import io.github.quiltservertools.blockbotdiscord.extensions.inline.InlineCommandsExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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

@OptIn(PrivilegedIntent::class)
object BlockBotDiscord : ModInitializer, CoroutineScope {
    const val MOD_ID = "blockbot-discord"

    val logger: Logger = LogManager.getLogger()
    val CONFIG_FOLDER: Path = FabricLoader.getInstance().configDir.resolve("blockbot")
    lateinit var bot: ExtensibleBot

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
        if (!config.isCorrect()) {
            logFatal("Config invalid. Disabling mod...")
            return
        }

        ServerLifecycleEvents.SERVER_STARTING.register(BlockBotDiscord::serverStarting)

        DiscordConsoleAppender().start()

        if (CONFIG_FOLDER.resolve(MESSAGES_PATH).notExists()) {
            logInfo("No messages file, creating...")
            val format = Json { prettyPrint = true }
            val messages = MessagesConfig()
            logInfo(format.encodeToString(DEFAULT_MESSAGES))
        }
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
