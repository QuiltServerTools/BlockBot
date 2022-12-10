package io.github.quiltservertools.blockbotapi.mixin;

import com.mojang.brigadier.context.CommandContext;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.SayCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SayCommand.class)
public abstract class SayCommandMixin {
    @Inject(
        method = "method_43657",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/network/message/MessageType$Parameters;)V")
    )
    private static void relayPlayerSayToDiscord(CommandContext<ServerCommandSource> ctx, SignedMessage message, CallbackInfo ci) {
        var entity = ctx.getSource().getEntity();
        MessageSender sender;
        if (entity instanceof ServerPlayerEntity player) {
            sender = new PlayerMessageSender(
                player,
                MessageSender.MessageType.ANNOUNCEMENT
            );
        } else {
            sender = new MessageSender(
                Text.literal(ctx.getSource().getName()),
                ctx.getSource().getDisplayName(),
                MessageSender.MessageType.ANNOUNCEMENT
            );
        }

        ChatMessageEvent.EVENT.invoker().message(
            sender,
            message.getContent()
        );
    }

}
