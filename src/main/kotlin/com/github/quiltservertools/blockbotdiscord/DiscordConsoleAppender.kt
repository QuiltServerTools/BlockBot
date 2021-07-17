package com.github.quiltservertools.blockbotdiscord

import com.github.quiltservertools.blockbotdiscord.config.ConsoleRelaySpec
import com.github.quiltservertools.blockbotdiscord.config.config
import com.github.quiltservertools.blockbotdiscord.extensions.ConsoleExtension
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.layout.PatternLayout

private val PATTERN_LAYOUT = PatternLayout.newBuilder().withPattern(config[ConsoleRelaySpec.pattern]).build()

@Plugin(name = "DiscordConsoleAppender", category = "Core", elementType = "appender", printObject = true)
class DiscordConsoleAppender : AbstractAppender("DiscordConsoleAppender", null, PATTERN_LAYOUT, false) {
    init {
        (LogManager.getRootLogger() as Logger).addAppender(this)
    }

    override fun append(event: LogEvent) {
        if (event.level <= Level.valueOf(config[ConsoleRelaySpec.minLevel])) {
            ConsoleExtension.consoleQueue.add(PATTERN_LAYOUT.toSerializable(event).toString())
        }
    }

}
