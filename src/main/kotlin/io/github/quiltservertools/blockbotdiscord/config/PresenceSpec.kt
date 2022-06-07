package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import dev.kord.common.entity.ActivityType
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.server.MinecraftServer

object PresenceSpec : ConfigSpec() {
    val activityType by required<ActivityType>()
    val activityText by required<String>()
}

fun Config.formatPresenceText(
    server: MinecraftServer
): String = Placeholders.parseText(
    TextParserUtils.formatText(
        this[PresenceSpec.activityText]
    ),
    PlaceholderContext.of(server)
).string
