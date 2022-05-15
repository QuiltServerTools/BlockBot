package io.github.quiltservertools.blockbotapi.mixin;

import com.mojang.brigadier.context.CommandContext;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import net.minecraft.network.encryption.SignedChatMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.MeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MeCommand.class)
public abstract class MeCommandMixin {
    @Inject(
        method = "method_43645",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/encryption/SignedChatMessage;Lnet/minecraft/server/filter/TextStream$Message;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/registry/RegistryKey;)V"),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void relayPlayerMeToDiscord(ServerCommandSource serverCommandSource, ServerPlayerEntity player, SignedChatMessage signedChatMessage, TextStream.Message message, CallbackInfo ci, PlayerManager playerManager, SignedChatMessage signedChatMessage2) {
        MessageSender sender = new PlayerMessageSender(
                player,
                MessageSender.MessageType.EMOTE
            );

        ChatMessageEvent.EVENT.invoker().message(
            sender,
            signedChatMessage2.method_44125()
        );
    }

    @Inject(
        method = "method_13238",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/encryption/SignedChatMessage;Lnet/minecraft/network/MessageSender;Lnet/minecraft/util/registry/RegistryKey;)V"),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void relayNonPlayerMeToDiscord(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir, SignedChatMessage signedChatMessage, ServerCommandSource serverCommandSource) {
        MessageSender sender = new MessageSender(
            Text.literal(context.getSource().getName()),
            context.getSource().getDisplayName(),
            MessageSender.MessageType.EMOTE
        );

        ChatMessageEvent.EVENT.invoker().message(
            sender,
            signedChatMessage.method_44125()
        );
    }


}
