package io.github.quiltservertools.blockbotdiscord.utility

import com.google.common.collect.Iterables
import com.mojang.authlib.GameProfile
import dev.kord.core.entity.Message
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.text.Text

fun String.literal() = LiteralText(this)

fun Message.summary(): String {
    if (this.content.length > 20) {
        return this.content.take(20).trim() + ".."
    }

    return this.content
}

fun GameProfile.getTextures() = Iterables.getFirst(this.properties.get("textures"), null)?.value

fun Component.toNative(server: MinecraftServer): MutableText = FabricServerAudiences.of(server).toNative(this).shallowCopy()

fun Text.toAdventure(server: MinecraftServer) = FabricServerAudiences.of(server).toAdventure(this)
