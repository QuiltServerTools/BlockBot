package com.github.quiltservertools.blockbotdiscord.utility

import dev.kord.core.entity.Message
import net.minecraft.text.LiteralText

fun String.literal() = LiteralText(this)

fun Message.summary(): String {
    if (this.content.length > 20) {
        return this.content.take(20).trim() + ".."
    }

    return this.content
}
