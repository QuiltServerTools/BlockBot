package io.github.quiltservertools.blockbotdiscord.utility

import com.vdurmont.emoji.EmojiManager
import dev.kord.core.entity.Guild
import dev.kord.core.firstOrNull
import eu.pb4.placeholders.PlaceholderAPI
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import java.util.regex.Pattern


suspend fun convertStringToMention(message: String, guild: Guild): String {
    val mentionsRegex = Regex("@(.{3,32})")
    val mentions = mentionsRegex.findAll(message)
    var mentionMessage = message

    for (mention in mentions) {
        val name = mention.groupValues[1]
        var mentionString = guild.members.firstOrNull { it.nickname == name || it.username == name }?.mention
        if (mentionString == null) {
            mentionString = guild.roles.firstOrNull { it.name == name }?.mention
        }

        if (mentionString != null) {
            mentionMessage = message.replace(mention.value, mentionString)
        }
    }

    return mentionMessage
}

private val emojiPattern =
    Pattern.compile("[:](?<id>[^:\\s]+)[:]")

private val emojiAliases = EmojiManager.getAll().flatMap { it.aliases }.map {
    it to
        TranslatableText(
            "%1\$s%3256342\$s", ":$it:", EmojiManager.getForAlias(it).unicode
        )
}.toMap(
    mutableMapOf()
)

fun convertEmojiToTranslatable(
    input: MutableText
): MutableText {
    return PlaceholderAPI.parsePredefinedText(
        input,
        emojiPattern,
        emojiAliases as Map<String, Text>
    ) as MutableText
}
