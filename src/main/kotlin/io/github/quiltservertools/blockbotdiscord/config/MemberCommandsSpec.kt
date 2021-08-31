package io.github.quiltservertools.blockbotdiscord.config

import com.uchuhimo.konf.ConfigSpec

object MemberCommandsSpec : ConfigSpec() {
    val playerList by required<Boolean>()
    val whitelist by required<Boolean>()
}
