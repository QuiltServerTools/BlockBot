package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import dev.kord.common.entity.ActivityType
import eu.pb4.placeholders.PlaceholderAPI
import eu.pb4.placeholders.TextParser
import net.minecraft.server.MinecraftServer

object PresenceSpec : ConfigSpec() {
    val activityType by required<ActivityType>()
    val activityText by required<String>()
}

fun Config.formatPresenceText(
    server: MinecraftServer
): String = PlaceholderAPI.parseText(
    TextParser.parse(
        this[PresenceSpec.activityText]
    ),
    server
).string
