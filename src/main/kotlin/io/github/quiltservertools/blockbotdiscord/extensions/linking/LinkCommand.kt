package io.github.quiltservertools.blockbotdiscord.extensions.linking

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.extensions.unwrap
import kotlinx.coroutines.launch
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

typealias Dispatcher = CommandDispatcher<ServerCommandSource>
typealias Context = CommandContext<ServerCommandSource>

class LinkCommand(private val dispatcher: Dispatcher) {
    fun register() {
        dispatcher.register(
            literal("link")
                .executes { linkAccount(it, it.source.player) }
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
                                    LongArgumentType.getLong(it, "id")?.let { Snowflake(it) }
                                )
                            }
                        )
                    )
                )
        )
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
                source.sendFeedback(LiteralText(user?.tag ?: id.asString), false)

                for (uuid in BlockBotDiscord.linkedAccounts.get(id)!!) {
                    val account = source.server.userCache.getByUuid(uuid).unwrap()
                    source.sendFeedback(LiteralText("    - ${account?.name ?: uuid.toString()}"), false)
                }
            } else {
                source.sendFeedback(LiteralText("No linked accounts"), false)
            }
        }

        return 1
    }

    private fun linkAccount(context: Context, player: ServerPlayerEntity): Int {
        BlockBotDiscord.launch {
            val user = player.getLinkedAccount()

            if (user != null) {
                context.source.sendFeedback(LiteralText("Already linked to ${user.tag}"), false)
            } else {
                val code = "%05d".format(player.random.nextInt(100000))
                LinkingExtension.linkCodes[code] = player.uuid
                context.source.sendFeedback(LiteralText(code), false)
                player.syncLinkedName(BlockBotDiscord.bot.getKoin().get())
            }
        }
        return 1
    }
}
