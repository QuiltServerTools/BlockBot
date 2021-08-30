package io.github.quiltservertools.blockbotdiscord

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.supplier.EntitySupplyStrategy
import io.github.quiltservertools.blockbotdiscord.config.config
import io.github.quiltservertools.blockbotdiscord.config.getGuild
import io.github.quiltservertools.mcdiscordreserializer.renderer.implementation.DefaultMinecraftRenderer
import kotlinx.coroutines.runBlocking
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.text.TextColor

class MentionToMinecraftRenderer(
    private val bot: ExtensibleBot,
) : DefaultMinecraftRenderer() {
    override fun appendChannelMention(text: MutableText, id: String): MutableText {
        return runBlocking {
            val channel = bot.getKoin().get<Kord>()
                .getChannel(Snowflake(id), EntitySupplyStrategy.cacheWithRestFallback)
            val name = channel?.data?.name?.value ?: "deleted-channel"

            text.append(
                LiteralText(
                    "#$name"
                ).styled { it.withColor(BLURPLE) }
            )
        }
    }

    override fun appendUserMention(component: MutableText, id: String): MutableText {
        return runBlocking {
            val member = config.getGuild(bot).getMemberOrNull(Snowflake(id))
            val name = member?.displayName ?: "unknown-user"

            component.append(
                LiteralText("@$name")
            ).styled { it.withColor(BLURPLE) }
        }
    }

    override fun appendRoleMention(text: MutableText, id: String): MutableText {
        return runBlocking {
            val role = config.getGuild(bot).getRoleOrNull(Snowflake(id))
            val name = role?.name ?: "deleted-role"
            val color = if (role != null && role.color.rgb != 0) role.color.rgb else BLURPLE.rgb

            text.append(
                LiteralText(
                    "@${name}"
                ).styled { it.withColor(color) }
            )
        }
    }

    companion object {
        private val BLURPLE = TextColor.fromRgb(0x7289da)
    }
}
