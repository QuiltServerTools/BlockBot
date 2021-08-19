package io.github.quiltservertools.blockbotapi.mixin;

import com.mojang.brigadier.context.CommandContext;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import net.minecraft.server.command.SayCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SayCommand.class)
public abstract class SayCommandMixin {
    @Inject(
        method = "method_13563",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;getEntity()Lnet/minecraft/entity/Entity;"),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void relaySayToDiscord(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir, Text message, Text formatted) {
        var entity = context.getSource().getEntity();
        MessageSender sender = null;
        if (entity instanceof ServerPlayerEntity player) {
            sender = new PlayerMessageSender(
                player,
                MessageSender.MessageType.ANNOUNCEMENT
            );
        } else {
            sender = new MessageSender(
                context.getSource().getDisplayName(),
                MessageSender.MessageType.ANNOUNCEMENT
            );
        }

        ChatMessageEvent.EVENT.invoker().message(
            sender,
            message.getString()
        );
    }
}
