package com.github.quiltservertools.blockbotdiscord.config

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.uchuhimo.konf.ConfigSpec

object BotSpec : ConfigSpec() {
    val token by required<String>()
    val guild by required<Long>()
    val channels by required<Map<String, Long>>()
}
