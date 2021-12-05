package io.github.quiltservertools.blockbotdiscord.config

import com.mojang.authlib.GameProfile
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import eu.pb4.placeholders.PlaceholderAPI
import eu.pb4.placeholders.TextParser
import io.github.quiltservertools.blockbotdiscord.extensions.linking.linkCode
import io.github.quiltservertools.blockbotdiscord.utility.literal
import net.minecraft.server.MinecraftServer
import net.minecraft.text.LiteralText

object LinkingSpec : ConfigSpec() {
    val enabled by required<Boolean>()
    val requireLinking by required<Boolean>()
    val unlinkedDisconnectMessage by required<List<String>>()
    val requiredRoles by required<List<ULong>>()
    val requiredRoleDisconnectMessage by required<String>()

    object MessagesSpec : ConfigSpec() {
        val noLinkedAccounts by required<String>()
        val alreadyLinked by required<String>()
        val failedUnlink by required<String>()
        val successfulUnlink by required<String>()
        val successfulLink by required<String>()
        val linkCode by required<String>()
    }
}

fun Config.formatUnlinkedDisconnectMessage(gameProfile: GameProfile, server: MinecraftServer) =
    LiteralText("").apply {
        config[LinkingSpec.unlinkedDisconnectMessage].forEach {
            this.append(
                formatLine(
                    it,
                    server,
                    gameProfile.linkCode
                )
            )
            this.append(LiteralText("\n"))
        }
    }

private fun formatLine(line: String, server: MinecraftServer, code: String) =
    PlaceholderAPI.parseText(
        PlaceholderAPI.parsePredefinedText(
            TextParser.parse(line),
            PlaceholderAPI.ALT_PLACEHOLDER_PATTERN_CUSTOM,
            mapOf(
                "code" to code.literal()
            )
        ),
        server
    )
