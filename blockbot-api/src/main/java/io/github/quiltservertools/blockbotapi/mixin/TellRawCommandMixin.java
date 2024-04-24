package io.github.quiltservertools.blockbotapi.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TellRawCommand;
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
    private static void relayTellrawToDiscord(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        // We are checking for "@a" to make sure only messages intended for the public are relayed.
        // Messages with a selector like @a[distance=..100] should not be relayed.
        String input = context.getInput();
        ParsedCommandNode<ServerCommandSource> parsedCommandNode = context.getNodes().get(context.getNodes().size() - 2);
        if (parsedCommandNode.getRange().get(input).equals("@a")) {
            var entity = context.getSource().getEntity();
            MessageSender sender = MessageSender.of(context.getSource(), MessageSender.MessageType.EMOTE);
            ChatMessageEvent.EVENT.invoker().message(
                sender,
                Texts.parse(context.getSource(), TextArgumentType.getTextArgument(context, "message"), entity, 0)
            );
        }
    }
}
