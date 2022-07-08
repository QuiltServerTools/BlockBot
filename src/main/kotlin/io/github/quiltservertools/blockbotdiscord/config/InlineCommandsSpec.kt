package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.ConfigSpec

object InlineCommandsSpec : ConfigSpec() {
    val enabled by required<Boolean>()
    val opLevel by required<Int>()
}
