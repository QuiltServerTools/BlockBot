package io.github.quiltservertools.blockbotdiscord.extensions.linking

import com.google.common.collect.HashBiMap
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.mojang.authlib.GameProfile
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Member
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.config.BotSpec
import io.github.quiltservertools.blockbotdiscord.config.LinkingSpec
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.getGuild
import io.github.quiltservertools.blockbotdiscord.extensions.unwrap
import io.github.quiltservertools.blockbotdiscord.logInfo
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import org.koin.core.component.inject
import java.util.*
import kotlin.random.Random

class LinkingExtension : Extension() {
    override val name = "linking"

    private val server: MinecraftServer by inject()

    @OptIn(KordPreview::class)
    override suspend fun setup() {
        ephemeralSlashCommand(::LinkingArgs) {
            name = "link"
            description = "links your discord account to a minecraft account"

            guild(config.getGuild(bot))

            val roles = config[LinkingSpec.allowedRoles]
            if (roles.isNotEmpty()) {
                allowByDefault = false
                allowedRoles.addAll(roles.map { Snowflake(it) })
            }

            action {
                if (!linkCodes.containsKey(arguments.code)) {
                    respond {
                        content = "Invalid linking code"
                    }
                } else {
                    val snowflake = event.interaction.user.id
                    val uuid = linkCodes[arguments.code]!!

                    BlockBotDiscord.linkedAccounts.add(snowflake, uuid)
                    logInfo("Linked $uuid to $snowflake")
                    linkCodes.remove(arguments.code)
                    val profile = server.userCache.getByUuid(uuid).unwrap()

                    respond {
                        content = config[LinkingSpec.MessagesSpec.successfulLink].replace("{player}", profile?.name ?: "Unknown")
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
        val linkCodes: HashBiMap<String, UUID> = HashBiMap.create()
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

fun GameProfile.checkLink(): Boolean {
    return runBlocking {
        if (config[LinkingSpec.enabled] && config[LinkingSpec.requireLinking]) {
            return@runBlocking this@checkLink.linkedAccount() != null;
        } else {
            return@runBlocking true
        }
    }
}

fun GameProfile.checkRoles(): Boolean {
    return runBlocking {
        if (config[LinkingSpec.enabled] && config[LinkingSpec.requireLinking] && config[LinkingSpec.connectableRoles].isNotEmpty()) {
            val memberRoles = linkedAccount()?.asMemberOrNull(Snowflake(config[BotSpec.guild]))?.roles?.toList();
            val rolesIDs = ArrayList<Long>();

            memberRoles?.forEach { role -> rolesIDs.add(role.id.value.toLong())}

            return@runBlocking rolesIDs.containsAll(config[LinkingSpec.connectableRoles])
        } else {
            return@runBlocking true
        }
    }
}

val GameProfile.linkCode: String get() {
    return if (LinkingExtension.linkCodes.containsValue(this.id)) {
        LinkingExtension.linkCodes.inverse()[this.id]!!
    } else {
        val code = "%05d".format(Random.nextInt(100000))
        LinkingExtension.linkCodes[code] = this.id
        code
    }
}
