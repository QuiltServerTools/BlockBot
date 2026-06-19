package io.github.quiltservertools.blockbotdiscord.utility

import com.google.common.collect.Iterables
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mojang.authlib.GameProfile
import com.mojang.serialization.JsonOps
import dev.kord.core.entity.Message
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.node.DynamicTextNode
import eu.pb4.placeholders.api.parsers.NodeParser
import eu.pb4.placeholders.api.parsers.TagLikeParser
import me.drex.vanish.api.VanishAPI
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.core.HolderLookup
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import java.util.function.Function

val GSON: Gson = GsonBuilder().create()

val DYN_KEY = DynamicTextNode.key("blockbot")

val TEXT_PARSER = NodeParser.builder()
    .simplifiedTextFormat()
    .quickText()
    .serverPlaceholders()
    .placeholders(TagLikeParser.PLACEHOLDER_ALTERNATIVE, DYN_KEY)
    .staticPreParsing()
    .build()

fun String.literal(): MutableComponent = Component.literal(this)

fun Message.summary(): String {
    if (this.content.length > 20) {
        return this.content.take(20).trim() + ".."
    }

    return this.content
}

typealias AdventureComponent = net.kyori.adventure.text.Component

fun GameProfile.getTextures() = Iterables.getFirst(this.properties.get("textures"), null)?.value

fun AdventureComponent.toNative(wrapperLookup: HolderLookup.Provider): MutableComponent {
    val json = GSON.fromJson(GsonComponentSerializer.gson().serialize(this), JsonElement::class.java)
    return ComponentSerialization.CODEC.decode(wrapperLookup.createSerializationContext(JsonOps.INSTANCE), json)
        .result().map { it.first }.orElse(Component.empty()).copy()
}

fun Component.toAdventure(wrapperLookup: HolderLookup.Provider): AdventureComponent {
    return ComponentSerialization.CODEC.encodeStart(wrapperLookup.createSerializationContext(JsonOps.INSTANCE), this).result()
        .map { json -> GsonComponentSerializer.gson().deserialize(json.toString()) }
        .orElseGet { AdventureComponent.empty() }
}

fun ServerPlayer.isVanished() =
    FabricLoader.getInstance().isModLoaded("melius-vanish") && VanishAPI.isVanished(this)

fun String.formatText(context: ParserContext = ParserContext.of(), placeholders: Map<String, Component> = emptyMap()): Component {
    return TEXT_PARSER.parseComponent(this, context.with(DYN_KEY, Function { key: String -> placeholders[key] }))
}

fun String.formatText(player: ServerPlayer, placeholders: Map<String, Component> = emptyMap()) = this.formatText(PlaceholderContext.of(player).asParserContext(), placeholders)
