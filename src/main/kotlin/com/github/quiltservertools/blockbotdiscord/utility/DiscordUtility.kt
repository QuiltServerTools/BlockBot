package com.github.quiltservertools.blockbotdiscord.utility

import dev.kord.core.entity.Guild
import dev.kord.core.firstOrNull

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
