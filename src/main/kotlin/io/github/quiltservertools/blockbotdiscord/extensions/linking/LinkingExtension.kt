package io.github.quiltservertools.blockbotdiscord.extensions.linking

import com.google.common.collect.HashBiMap
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.getTopRole
import com.mojang.authlib.GameProfile
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Member
import eu.pb4.placeholders.PlaceholderAPI
import eu.pb4.placeholders.PlaceholderResult
import eu.pb4.placeholders.TextParser
import io.github.quiltservertools.blockbotdiscord.BlockBotDiscord
import io.github.quiltservertools.blockbotdiscord.config.*
import io.github.quiltservertools.blockbotdiscord.extensions.getDisplayColor
import io.github.quiltservertools.blockbotdiscord.extensions.unwrap
import io.github.quiltservertools.blockbotdiscord.id
import io.github.quiltservertools.blockbotdiscord.logInfo
import io.github.quiltservertools.blockbotdiscord.utility.asMemberOrNull
import io.github.quiltservertools.blockbotdiscord.utility.literal
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
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

            val roles = config[LinkingSpec.requiredRoles]
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
            if (config[LinkingSpec.nicknameSync]) {
                BlockBotDiscord.launch {
                    handler.player.syncLinkedName(kord)
                }
            }
        }

        registerPlaceholders()
    }

    inner class LinkingArgs : Arguments() {
        val code by string {
            name = "code"
            description = "The linking code received in-game"
        }
    }

    companion object {
        val linkCodes: HashBiMap<String, UUID> = HashBiMap.create()
    }
}

private fun registerPlaceholders() {
    PlaceholderAPI.register(id("linked_username")) { ctx ->
        runBlocking {
            val user = ctx.player.getLinkedAccount()
            val color = if (ctx.argument == "colored") user?.asMemberOrNull()?.getDisplayColor() else null

            PlaceholderResult.value(user?.username?.literal()?.styled { color?.let { _ -> it.withColor(color.rgb) } })
        }
    }

    PlaceholderAPI.register(id("linked_display")) { ctx ->
        runBlocking {
            val user = ctx.player.getLinkedAccount()
            val color = if (ctx.argument == "colored") user?.asMemberOrNull()?.getDisplayColor() else null

            PlaceholderResult.value(
                (user?.asMemberOrNull(config.guildId)?.displayName ?: user?.username)?.literal()
                    ?.styled { color?.let { _ -> it.withColor(color.rgb) } }
            )
        }
    }

    PlaceholderAPI.register(id("linked_discriminator")) { ctx ->
        runBlocking {
            PlaceholderResult.value(ctx.player.getLinkedAccount()?.discriminator?.literal())
        }
    }

    PlaceholderAPI.register(id("linked_role")) { ctx ->
        runBlocking {
            val member = ctx.player.getLinkedAccount()?.asMemberOrNull(config.guildId)
            val color = member?.getTopRole()?.color
            val text = member?.getTopRole()?.data?.name?.literal()

            PlaceholderResult.value(text?.styled { color?.let { _ -> it.withColor(color.rgb) } })
        }
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

fun GameProfile.canJoin(server: MinecraftServer): Text? {
    return runBlocking {
        if (config[LinkingSpec.enabled] && config[LinkingSpec.requireLinking]) {
            val account = this@canJoin.linkedAccount()
            if (account != null) {
                if (account.asMemberOrNull() == null) return@runBlocking TextParser.parse(config[LinkingSpec.notInServerMessage])

                val requiredRoles = config[LinkingSpec.requiredRoles]
                if (requiredRoles.isEmpty()) return@runBlocking null

                return@runBlocking if (account.asMemberOrNull()?.roleIds?.any { requiredRoles.contains(it.value) } == true) {
                    null
                } else {
                    TextParser.parse(config[LinkingSpec.requiredRoleDisconnectMessage])
                }
            }

            return@runBlocking config.formatUnlinkedDisconnectMessage(this@canJoin, server)
        }

        return@runBlocking null
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
