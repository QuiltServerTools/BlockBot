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
            text.append(
                LiteralText(
                    "#${
                        bot.getKoin().get<Kord>()
                            .getChannel(Snowflake(id), EntitySupplyStrategy.cacheWithRestFallback)?.data?.name?.value
                    }"
                ).styled { it.withColor(BLURPLE) }
            )
        }
    }

    override fun appendUserMention(component: MutableText, id: String): MutableText {
        return runBlocking {
            component.append(
                LiteralText("@${config.getGuild(bot).getMemberOrNull(Snowflake(id))?.displayName}")
            ).styled { it.withColor(BLURPLE) }
        }
    }

    override fun appendRoleMention(text: MutableText, id: String): MutableText {
        return runBlocking {
            val role = config.getGuild(bot).getRoleOrNull(Snowflake(id))

            text.append(
                LiteralText(
                    "@${role?.name}"
                ).styled { it.withColor(role?.color?.rgb ?: BLURPLE.rgb) }
            )
        }
    }

    companion object {
        private val BLURPLE = TextColor.fromRgb(0x7289da)
    }
}
