package io.github.quiltservertools.blockbotapi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.commands.RandomCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RandomCommand.class)
public abstract class RandomCommandMixin {

    @WrapOperation(
        method = "randomSample",
        at = @At(
            value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
        )
    )
    private static void relayRandomRollToDiscord(PlayerList instance, Component message, boolean overlay, Operation<Void> original, CommandSourceStack source) {
        original.call(instance, message, overlay);
        MessageSender sender = MessageSender.of(source, MessageSender.MessageType.ANNOUNCEMENT);
        ChatMessageEvent.EVENT.invoker().message(
            sender,
            // Change translation to literal
            Component.literal(message.getString())
        );
    }

}
