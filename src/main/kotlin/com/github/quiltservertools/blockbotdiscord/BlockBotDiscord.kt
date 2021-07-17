package com.github.quiltservertools.blockbotdiscord

import com.github.quiltservertools.blockbotapi.BlockBotApi
import com.github.quiltservertools.blockbotdiscord.config.*
import com.github.quiltservertools.blockbotdiscord.extensions.BlockBotApiExtension
import com.github.quiltservertools.blockbotdiscord.extensions.ConsoleExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@OptIn(PrivilegedIntent::class)
object BlockBotDiscord : ModInitializer, CoroutineScope {
    const val MOD_ID = "blockbot-discord"

    val logger: Logger = LogManager.getLogger()
    lateinit var bot: ExtensibleBot

    override fun onInitialize() {
        logInfo("Initializing")

        createConfigFile()
        if (!config.isCorrect()) {
            logFatal("Config invalid. Disabling mod...")
            return
        }

        ServerLifecycleEvents.SERVER_STARTING.register(::serverStarting)

        DiscordConsoleAppender().start()

        // Event testing, none work :/
        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            logInfo("block attacked")
            ActionResult.PASS
        }
        ServerLifecycleEvents.SERVER_STARTING.register { server: MinecraftServer ->
            logInfo(server.name)
        }
        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            logInfo(handler.player.name.asString())
        }
    }

    private fun serverStarting(server: MinecraftServer) {
        logInfo("Staring bot")
        launch {
            logInfo("Staring bot2")
            bot = ExtensibleBot(config[BotSpec.token]) {
                slashCommands {
                    enabled = true
                }

                extensions {
                    add(::BlockBotApiExtension)
                    if (config.getChannelsBi().containsKey("console")) {
                        add(::ConsoleExtension)
                    }
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
