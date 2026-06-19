package io.github.quiltservertools.blockbotapi.mixin;

import com.mojang.brigadier.context.CommandContext;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.EmoteCommands;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmoteCommands.class)
public abstract class EmoteCommandsMixin {
    @Inject(
        method = "lambda$register$1",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/network/chat/ChatType$Bound;)V")
    )
    private static void relayPlayerMeToDiscord(CommandContext<CommandSourceStack> ctx, PlayerChatMessage message, CallbackInfo ci) {
        MessageSender sender = MessageSender.of(ctx.getSource(), MessageSender.MessageType.EMOTE);
        ChatMessageEvent.EVENT.invoker().message(
            sender,
            message.decoratedContent()
        );
    }


}
