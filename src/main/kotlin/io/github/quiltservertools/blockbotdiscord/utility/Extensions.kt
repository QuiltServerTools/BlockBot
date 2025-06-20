package io.github.quiltservertools.blockbotdiscord.utility

import com.google.common.collect.Iterables
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mojang.authlib.GameProfile
import com.mojang.serialization.JsonOps
import dev.kord.core.entity.Message
import me.drex.vanish.api.VanishAPI
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs

val GSON: Gson = GsonBuilder().create()

fun String.literal(): MutableText = Text.literal(this)

fun Message.summary(): String {
    if (this.content.length > 20) {
        return this.content.take(20).trim() + ".."
    }

    return this.content
}

fun GameProfile.getTextures() = Iterables.getFirst(this.properties.get("textures"), null)?.value

fun Component.toNative(wrapperLookup: RegistryWrapper.WrapperLookup): MutableText {
    val json = GSON.fromJson(GsonComponentSerializer.gson().serialize(this), JsonElement::class.java)
    return TextCodecs.CODEC.decode(wrapperLookup.getOps(JsonOps.INSTANCE), json)
        .result().map { it.first }.orElse(Text.empty()).copy()
}

fun Text.toAdventure(wrapperLookup: RegistryWrapper.WrapperLookup): Component {
    return TextCodecs.CODEC.encodeStart(wrapperLookup.getOps(JsonOps.INSTANCE), this).result()
        .map { json -> GsonComponentSerializer.gson().deserialize(json.toString()) }
        .orElseGet { Component.empty() }
}

fun ServerPlayerEntity.isVanished() =
    FabricLoader.getInstance().isModLoaded("melius-vanish") && VanishAPI.isVanished(this)
