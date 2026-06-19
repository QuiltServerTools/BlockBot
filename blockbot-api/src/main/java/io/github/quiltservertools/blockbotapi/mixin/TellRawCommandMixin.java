package io.github.quiltservertools.blockbotapi.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TellRawCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TellRawCommand.class)
public abstract class TellRawCommandMixin {
    @Inject(
        method = "lambda$register$0",
        at = @At(value = "HEAD")
    )
    private static void relayTellrawToDiscord(CommandContext<CommandSourceStack> context, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        // We are checking for "@a" to make sure only messages intended for the public are relayed.
        // Messages with a selector like @a[distance=..100] should not be relayed.
        String input = context.getInput();
        ParsedCommandNode<CommandSourceStack> parsedCommandNode = context.getNodes().get(context.getNodes().size() - 2);
        if (parsedCommandNode.getRange().get(input).equals("@a")) {
            var entity = context.getSource().getEntity();
            MessageSender sender = MessageSender.of(context.getSource(), MessageSender.MessageType.EMOTE);
            ChatMessageEvent.EVENT.invoker().message(
                sender,
                ComponentArgument.getResolvedComponent(context, "message", entity)
            );
        }
    }
}
