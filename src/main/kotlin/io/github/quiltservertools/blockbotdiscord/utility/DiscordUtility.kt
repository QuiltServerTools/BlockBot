package io.github.quiltservertools.blockbotdiscord.utility

import com.vdurmont.emoji.EmojiManager
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import eu.pb4.placeholders.PlaceholderAPI
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.guildId
import kotlinx.coroutines.flow.firstOrNull
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import java.util.regex.Pattern


suspend fun convertStringToMention(message: String, guild: Guild): String {
    val mentionsRegex = Regex("@(\\S{3,32})")
    val mentions = mentionsRegex.findAll(message)
    var mentionMessage = message

    for (mention in mentions) {
        val name = mention.groupValues[1]
        val member =  guild.members.firstOrNull { it.nickname.equals(name, ignoreCase = true) || it.username.equals(name, ignoreCase = true) }
        var mentionString = member?.mention
        if (mentionString == null) {
            mentionString = guild.roles.firstOrNull { it.name == name }?.mention
        }

        if (mentionString != null) {
            mentionMessage = mentionMessage.replace(mention.value, mentionString)
        }
    }

    return mentionMessage
}

private val emojiPattern =
    Pattern.compile("[:](?<id>[^:\\s]+)[:]")

private val emojiAliases = EmojiManager.getAll().flatMap { it.aliases }.map {
    it to
        TranslatableText(":$it:")
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

suspend fun User.asMemberOrNull() = this.asMemberOrNull(config.guildId)
