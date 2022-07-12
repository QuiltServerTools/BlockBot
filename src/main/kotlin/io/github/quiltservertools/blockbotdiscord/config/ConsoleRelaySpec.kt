package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.ConfigSpec

object ConsoleRelaySpec : ConfigSpec() {
    val pattern by required<String>()
    val minLevel by required<String>()
    val delay by required<Long>()
    val requireAdmin by required<Boolean>()
}
