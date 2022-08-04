package io.github.quiltservertools.blockbotdiscord.extensions.linking

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import eu.pb4.placeholders.api.TextParserUtils
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.config.LinkingSpec
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.formatUnlinkedDisconnectMessage
import io.github.quiltservertools.blockbotdiscord.extensions.unwrap
import io.github.quiltservertools.blockbotdiscord.logInfo
import kotlinx.coroutines.launch
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

typealias Dispatcher = CommandDispatcher<ServerCommandSource>
typealias Context = CommandContext<ServerCommandSource>

class LinkCommand(private val dispatcher: Dispatcher) {
    fun register() {
        dispatcher.register(
            literal("link")
                .executes { linkAccount(it, it.source.playerOrThrow) }
                .then(literal("unlink")
                    .requires { it.playerOrThrow.gameProfile.isLinked() }
                    .executes { unlinkAccount(it, it.source.playerOrThrow) })
                .then(literal("get")
                    .requires { it.hasPermissionLevel(2) }
                    .then(literal("minecraft")
                        .then(argument("player", GameProfileArgumentType.gameProfile())
                            .executes {
                                getLinkedPlayer(
                                    it,
                                    GameProfileArgumentType.getProfileArgument(it, "player")
                                )
                            }
                        )
                    )
                    .then(literal("discord")
                        .then(argument("id", LongArgumentType.longArg(1))
                            .executes {
                                getLinkedPlayer(
                                    it,
                                    Snowflake(LongArgumentType.getLong(it, "id"))
                                )
                            }
                        )
                    )
                )
        )
    }

    private fun unlinkAccount(context: Context, player: ServerPlayerEntity): Int {
        val id = BlockBotDiscord.linkedAccounts.get(player.uuid)

        if (BlockBotDiscord.linkedAccounts.remove(player.uuid)) {
            logInfo("Unlinked ${player.name} from $id")
            context.source.sendFeedback(TextParserUtils.formatText(config[LinkingSpec.MessagesSpec.successfulUnlink]), false)

            if (config[LinkingSpec.requireLinking]) {
                context.source.playerOrThrow.networkHandler.disconnect(
                    config.formatUnlinkedDisconnectMessage(
                        player.gameProfile,
                        context.source.server
                    )
                )
            }
        } else {
            context.source.sendFeedback(TextParserUtils.formatText(config[LinkingSpec.MessagesSpec.failedUnlink]), false)
        }

        return 1
    }

    private fun getLinkedPlayer(context: Context, profiles: Collection<GameProfile>): Int {
        profiles.forEach {
            val id = BlockBotDiscord.linkedAccounts.get(it.id)
            getLinkedPlayer(context, id)
        }

        return 1
    }

    private fun getLinkedPlayer(context: Context, id: Snowflake?): Int {
        val source = context.source

        BlockBotDiscord.launch {
            val kord = BlockBotDiscord.bot.getKoin().get<Kord>()

            if (id != null && BlockBotDiscord.linkedAccounts.get(id) != null) {
                val user = kord.getUser(id)
                source.sendFeedback(Text.literal(user?.tag ?: id.toString()), false)

                for (uuid in BlockBotDiscord.linkedAccounts.get(id)!!) {
                    val account = source.server.userCache.getByUuid(uuid).unwrap()
                    source.sendFeedback(Text.literal("    - ${account?.name ?: uuid.toString()}"), false)
                }
            } else {
                source.sendFeedback(TextParserUtils.formatText(config[LinkingSpec.MessagesSpec.noLinkedAccounts]), false)
            }
        }

        return 1
    }

    private fun linkAccount(context: Context, player: ServerPlayerEntity): Int {
        BlockBotDiscord.launch {
            val user = player.getLinkedAccount()

            if (user != null) {
                context.source.sendFeedback(
                    TextParserUtils.formatText(
                        config[LinkingSpec.MessagesSpec.alreadyLinked].replace(
                            "{user}",
                            user.tag
                        )
                    ), false
                )
            } else {
                context.source.sendFeedback(
                    TextParserUtils.formatText(
                        config[LinkingSpec.MessagesSpec.linkCode].replace(
                            "{code}",
                            player.gameProfile.linkCode
                        )
                    ), false
                )
                player.syncLinkedName(BlockBotDiscord.bot.getKoin().get())
            }
        }
        return 1
    }
}
