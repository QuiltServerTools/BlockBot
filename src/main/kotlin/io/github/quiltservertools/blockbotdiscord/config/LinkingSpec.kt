package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import io.github.quiltservertools.blockbotdiscord.extensions.linking.linkCode
import io.github.quiltservertools.blockbotdiscord.utility.formatText
import net.minecraft.server.MinecraftServer
import net.minecraft.server.PlayerConfigEntry
import net.minecraft.text.MutableText
import net.minecraft.text.Text

object LinkingSpec : ConfigSpec() {
    val enabled by required<Boolean>()
    val requireLinking by required<Boolean>()
    val nicknameSync by required<Boolean>()
    val unlinkedDisconnectMessage by required<List<String>>()
    val requiredRoles by required<List<ULong>>()
    val syncedRoles by required<Map<String, ULong>>()
    val requiredRoleDisconnectMessage by required<String>()
    val requireInServer by required<Boolean>()
    val notInServerMessage by required<String>()

    object MessagesSpec : ConfigSpec() {
        val noLinkedAccounts by required<String>()
        val alreadyLinked by required<String>()
        val failedUnlink by required<String>()
        val successfulUnlink by required<String>()
        val successfulLink by required<String>()
        val linkCode by required<String>()
    }
}

fun Config.formatUnlinkedDisconnectMessage(playerConfigEntry: PlayerConfigEntry, server: MinecraftServer): MutableText =
    Text.empty().apply {
        config[LinkingSpec.unlinkedDisconnectMessage].forEach {
            this.append(
                it.replace(
                    "{code}",
                    playerConfigEntry.linkCode
                ).formatText()
            )
            this.append(Text.literal("\n"))
        }
    }

