package io.github.quiltservertools.blockbotdiscord.extensions.linking

import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Member
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.config.BotSpec
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.getGuild
import io.github.quiltservertools.blockbotdiscord.extensions.unwrap
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import org.koin.core.component.inject
import java.util.*

class LinkingExtension : Extension() {
    override val name = "linking"

    private val server: MinecraftServer by inject()

    @OptIn(KordPreview::class)
    override suspend fun setup() {
        slashCommand(::LinkingArgs) {
            name = "link"
            description = "links your discord account to a minecraft account"

            guild(config.getGuild(bot))

            action {
                if (!linkCodes.containsKey(arguments.code)) {
                    ephemeralFollowUp {
                        content = "Invalid linking code"
                    }
                } else {
                    val snowflake = event.interaction.user.id
                    val uuid = linkCodes[arguments.code]!!

                    BlockBotDiscord.linkedAccounts.add(snowflake, uuid)
                    linkCodes.remove(arguments.code)
                    val profile = server.userCache.getByUuid(uuid).unwrap()

                    ephemeralFollowUp {
                        content = "Successfully linked to: ${profile?.name}"
                    }
                }
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            BlockBotDiscord.launch {
                handler.player.syncLinkedName(kord)
            }
        }
    }

    inner class LinkingArgs : Arguments() {
        val code by string("code", "The linking code received in-game")
    }

    companion object {
        val linkCodes = mutableMapOf<String, UUID>()
    }
}

suspend fun Kord.canInteractWith(member: Member): Boolean {
    val self = member.guild.getMember(selfId)
    val highestOther = member.roles.map { it.rawPosition }.toList().maxOrNull() ?: 0
    val highestSelf = self.roles.map { it.rawPosition }.toList().maxOrNull() ?: 0
    return highestSelf > highestOther
}

suspend fun ServerPlayerEntity.syncLinkedName(kord: Kord) {
    val member = getLinkedAccount()?.asMemberOrNull(Snowflake(config[BotSpec.guild]))

    if (member?.let { kord.canInteractWith(it) } == true) {
        member.edit {
            nickname = name.string
        }
    }
}
