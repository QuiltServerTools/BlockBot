package io.github.quiltservertools.blockbotapi.mixin;

import com.mojang.brigadier.context.CommandContext;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.MeCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeCommand.class)
public abstract class MeCommandMixin {
    @Inject(
        method = "method_43645",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/network/message/MessageType$Parameters;)V")
    )
    private static void relayPlayerMeToDiscord(CommandContext<ServerCommandSource> ctx, SignedMessage message, CallbackInfo ci) {
        MessageSender sender = MessageSender.of(ctx.getSource(), MessageSender.MessageType.EMOTE);
        ChatMessageEvent.EVENT.invoker().message(
            sender,
            message.getContent()
        );
    }


}
