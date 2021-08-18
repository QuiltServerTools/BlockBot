package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.ConfigSpec

object BotSpec : ConfigSpec() {
    val token by required<String>()
    val guild by required<Long>()
    val channels by required<Map<String, Long>>()
}
