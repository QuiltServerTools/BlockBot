package io.github.quiltservertools.blockbotapi.event;

import io.github.quiltservertools.blockbotapi.sender.RelayMessageSender;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;

public interface RelayMessageEvent {
    Event<RelayMessageEvent> EVENT = EventFactory.createArrayBacked(RelayMessageEvent.class, (listeners) -> (sender, channel, message) -> {
        for (RelayMessageEvent listener : listeners) {
            InteractionResult result = listener.message(sender, channel, message);

            if (result != InteractionResult.PASS) {
                return result;
            }
        }

        return InteractionResult.PASS;
    });

    InteractionResult message(RelayMessageSender sender, String channel, String message);
}
