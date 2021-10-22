package io.github.quiltservertools.blockbotdiscord.utility

import com.google.common.collect.Iterables
import com.mojang.authlib.GameProfile
import dev.kord.core.entity.Message
import net.minecraft.text.LiteralText

fun String.literal() = LiteralText(this)

fun Message.summary(): String {
    if (this.content.length > 20) {
        return this.content.take(20).trim() + ".."
    }

    return this.content
}

fun GameProfile.getTextures() = Iterables.getFirst(this.properties.get("textures"), null)?.value
