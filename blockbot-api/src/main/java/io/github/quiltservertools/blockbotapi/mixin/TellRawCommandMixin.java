package io.github.quiltservertools.blockbotapi.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.MessageType;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TellRawCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TellRawCommand.class)
public abstract class TellRawCommandMixin {
    @Inject(
        method = "method_13777",
        at = @At(value = "HEAD")
    )
    private static void relayMeToDiscord(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (context.getInput().replace("/tellraw ", "").startsWith("@a")) {
            var entity = context.getSource().getEntity();
            MessageSender sender;
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
                MessageType.REGULAR,
                Texts.parse(context.getSource(), TextArgumentType.getTextArgument(context, "message"), entity, 0).getString()
            );
        }
    }
}
