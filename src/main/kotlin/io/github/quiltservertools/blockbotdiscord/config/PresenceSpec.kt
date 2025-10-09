package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import eu.pb4.placeholders.api.PlaceholderContext
import io.github.quiltservertools.blockbotdiscord.utility.formatText
import net.minecraft.server.MinecraftServer

object PresenceSpec : ConfigSpec() {
    val activityType by required<ActivityType>()
    val activityText by required<String>()
}

enum class ActivityType {
    Disabled,
    Game,
    Listening,
    Watching,
    Competing;
}

fun Config.formatPresenceText(
    server: MinecraftServer
): String = this[PresenceSpec.activityText].formatText(PlaceholderContext.of(server).asParserContext()).string
