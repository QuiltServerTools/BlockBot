package io.github.quiltservertools.blockbotapi.mixin;

import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.MeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
    private static void relayPlayerMeToDiscord(PlayerManager playerManager, ServerCommandSource source, SignedMessage message, CallbackInfo ci) {
        var entity = source.getEntity();
        MessageSender sender;
        if (entity instanceof ServerPlayerEntity player) {
            sender = new PlayerMessageSender(
                player,
                MessageSender.MessageType.EMOTE
            );
        } else {
            sender = new MessageSender(
                Text.literal(source.getName()),
                source.getDisplayName(),
                MessageSender.MessageType.EMOTE
            );
        }

        ChatMessageEvent.EVENT.invoker().message(
            sender,
            message.getContent()
        );
    }


}
