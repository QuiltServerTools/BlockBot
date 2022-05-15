package io.github.quiltservertools.blockbotdiscord.utility

import com.google.common.collect.Iterables
import com.mojang.authlib.GameProfile
import dev.kord.core.entity.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.text.MutableText
import net.minecraft.text.Text

fun String.literal(): MutableText = Text.literal(this)

fun Message.summary(): String {
    if (this.content.length > 20) {
        return this.content.take(20).trim() + ".."
    }

    return this.content
}

fun GameProfile.getTextures() = Iterables.getFirst(this.properties.get("textures"), null)?.value

fun Component.toNative(): MutableText = Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(this))?: Text.empty()

fun Text.toAdventure() = GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(this))
