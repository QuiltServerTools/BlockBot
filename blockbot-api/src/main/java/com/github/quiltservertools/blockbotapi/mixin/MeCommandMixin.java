package com.github.quiltservertools.blockbotapi.mixin;

import com.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import com.github.quiltservertools.blockbotapi.sender.MessageSender;
import com.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.MeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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
                player,
                MessageSender.MessageType.EMOTE
            );
        } else {
            sender = new MessageSender(
                context.getSource().getDisplayName(),
                MessageSender.MessageType.EMOTE
            );
        }

        ChatMessageEvent.EVENT.invoker().message(
            sender,
            message
        );
    }
}
