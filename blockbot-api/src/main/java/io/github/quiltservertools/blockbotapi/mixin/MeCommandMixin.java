package io.github.quiltservertools.blockbotapi.mixin;

import com.mojang.brigadier.context.CommandContext;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.MessageType;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import net.minecraft.server.command.MeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MeCommand.class)
public abstract class MeCommandMixin {
    @Inject(
        method = "method_13238",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;getEntity()Lnet/minecraft/entity/Entity;"),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void relayMeToDiscord(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir, String message) {
        var entity = context.getSource().getEntity();
        MessageSender sender = null;
        if (entity instanceof ServerPlayerEntity player) {
            sender = new PlayerMessageSender(
                player
            );
        } else {
            sender = new MessageSender(
                new LiteralText(context.getSource().getName()),
                context.getSource().getDisplayName()
            );
        }

        ChatMessageEvent.EVENT.invoker().message(
            sender,
            MessageType.EMOTE,
            message
        );
    }
}
