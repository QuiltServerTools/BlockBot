package io.github.quiltservertools.blockbotdiscord.extensions.linking

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import eu.pb4.placeholders.api.ParserContext
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.config.LinkingSpec
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.formatUnlinkedDisconnectMessage
import io.github.quiltservertools.blockbotdiscord.extensions.unwrap
import io.github.quiltservertools.blockbotdiscord.logInfo
import io.github.quiltservertools.blockbotdiscord.utility.formatText
import kotlinx.coroutines.launch
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.server.players.NameAndId
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component

typealias Dispatcher = CommandDispatcher<CommandSourceStack>
typealias Context = CommandContext<CommandSourceStack>

class LinkCommand(private val dispatcher: Dispatcher) {

    fun register() {
        dispatcher.register(
            literal("link")
                .executes { linkAccount(it, it.source.playerOrException) }
                .then(
                    literal("unlink")
                        .requires { it.player?.nameAndId()?.isLinked() ?: false }
                        .executes { unlinkAccount(it, it.source.playerOrException) })
                .then(
                    literal("get")
                        .requires (Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                            literal("minecraft")
                            .then(
                                argument("player", GameProfileArgument.gameProfile())
                                    .executes {
                                        getLinkedPlayer(
                                            it,
                                            GameProfileArgument.getGameProfiles(it, "player")
                                        )
                                    }
                            )
                        )
                        .then(
                            literal("discord")
                            .then(
                                argument("id", LongArgumentType.longArg(1))
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

    private fun unlinkAccount(context: Context, player: ServerPlayer): Int {
        val id = BlockBotDiscord.linkedAccounts.get(player.uuid)

        if (BlockBotDiscord.linkedAccounts.remove(player.uuid)) {
            logInfo("Unlinked ${player.name} from $id")
            context.source.sendSuccess(
                {
                    config[LinkingSpec.MessagesSpec.successfulUnlink].formatText(player)
                },
                false
            )

            if (config[LinkingSpec.requireLinking]) {
                context.source.playerOrException.connection.disconnect(
                    config.formatUnlinkedDisconnectMessage(
                        player.nameAndId(),
                        context.source.server
                    )
                )
            }
        } else {
            context.source.sendSuccess(
                {
                    config[LinkingSpec.MessagesSpec.failedUnlink].formatText(player)
                },
                false
            )
        }

        return 1
    }

    private fun getLinkedPlayer(context: Context, profiles: Collection<NameAndId>): Int {
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
                source.sendSuccess({ Component.literal(user?.tag ?: id.toString()) }, false)

                for (uuid in BlockBotDiscord.linkedAccounts.get(id)!!) {
                    val account = source.server.services().nameToIdCache.get(uuid)?.unwrap()
                    source.sendSuccess({ Component.literal("    - ${account?.name ?: uuid.toString()}") }, false)
                }
            } else {
                source.sendSuccess(
                    { config[LinkingSpec.MessagesSpec.noLinkedAccounts].formatText() },
                    false
                )
            }
        }

        return 1
    }

    private fun linkAccount(context: Context, player: ServerPlayer): Int {
        BlockBotDiscord.launch {
            val user = player.getLinkedAccount()

            if (user != null) {
                context.source.sendSuccess(
                    {
                        config[LinkingSpec.MessagesSpec.alreadyLinked].replace(
                            "{user}",
                            user.tag
                        ).formatText(player)
                    }, false
                )
            } else {
                context.source.sendSuccess(
                    {
                        config[LinkingSpec.MessagesSpec.linkCode].replace(
                            "{code}",
                            player.nameAndId().linkCode
                        ).formatText(player)
                    }, false
                )
                player.syncDiscord()
            }
        }
        return 1
    }
}
