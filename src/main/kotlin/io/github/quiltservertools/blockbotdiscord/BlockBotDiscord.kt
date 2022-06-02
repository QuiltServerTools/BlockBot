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
import io.github.quiltservertools.blockbotdiscord.extensions.linking.JsonLinkedAccounts
import io.github.quiltservertools.blockbotdiscord.extensions.linking.LinkCommand
import io.github.quiltservertools.blockbotdiscord.extensions.linking.LinkingExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files

@OptIn(PrivilegedIntent::class)
object BlockBotDiscord : ModInitializer, CoroutineScope {
    const val MOD_ID = "blockbot-discord"

    val logger: Logger = LogManager.getLogger()
    lateinit var bot: ExtensibleBot

    val linkedAccounts = JsonLinkedAccounts()

    override fun onInitialize() {
        logInfo("Initializing")

        if (!Files.exists(FabricLoader.getInstance().configDir.resolve(CONFIG_PATH))) {
            logInfo("No config file, creating...")
            Files.copy(
                FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath(CONFIG_PATH).get(),
                FabricLoader.getInstance().configDir.resolve(CONFIG_PATH)
            )
        }
        if (!config.isCorrect()) {
            logFatal("Config invalid. Disabling mod...")
            return
        }

        linkedAccounts.load()

        ServerLifecycleEvents.SERVER_STARTING.register(BlockBotDiscord::serverStarting)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            LinkCommand(dispatcher).register()
        }

        DiscordConsoleAppender().start()
    }

    private fun serverStarting(server: MinecraftServer) {
        launch {
            bot = ExtensibleBot(config[BotSpec.token]) {
                applicationCommands {
                    enabled = true
                    syncPermissions = false
                }

                extensions {
                    add(::BlockBotApiExtension)
                    if (config.getChannelsBi().containsKey("console")) add(::ConsoleExtension)
                    if (config[InlineCommandsSpec.enabled]) add(::InlineCommandsExtension)
                    add(::MemberCommandsExtension)
                    if (config[LinkingSpec.enabled]) add(::LinkingExtension)
                }

                intents {
                    +Intents.nonPrivileged
                    +Intent.GuildMembers
                }

                members {
                    fill(config[BotSpec.guild])
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

fun id(path: String) = Identifier(BlockBotDiscord.MOD_ID, path)

fun logDebug(message: String) = BlockBotDiscord.logger.debug(message)
fun logInfo(message: String) = BlockBotDiscord.logger.info(message)
fun logWarn(message: String) = BlockBotDiscord.logger.warn(message)
fun logFatal(message: String) = BlockBotDiscord.logger.fatal(message)
