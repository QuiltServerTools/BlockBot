package io.github.quiltservertools.blockbotapi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.RandomCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RandomCommand.class)
public abstract class RandomCommandMixin {

    @WrapOperation(
        method = "execute",
        at = @At(
            value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"
        )
    )
    private static void relayRandomRollToDiscord(PlayerManager instance, Text message, boolean overlay, Operation<Void> original, ServerCommandSource source) {
        original.call(instance, message, overlay);
        MessageSender sender = MessageSender.of(source, MessageSender.MessageType.ANNOUNCEMENT);
        ChatMessageEvent.EVENT.invoker().message(
            sender,
            // Change translation to literal
            Text.literal(message.getString())
        );
    }

}
