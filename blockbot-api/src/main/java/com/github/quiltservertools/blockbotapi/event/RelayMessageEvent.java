package com.github.quiltservertools.blockbotapi.event;

import com.github.quiltservertools.blockbotapi.sender.RelayMessageSender;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface RelayMessageEvent {
    Event<RelayMessageEvent> EVENT = EventFactory.createArrayBacked(RelayMessageEvent.class, (listeners) -> (sender, channel, message) -> {
        for (RelayMessageEvent listener : listeners) {
            ActionResult result = listener.message(sender, channel, message);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult message(RelayMessageSender sender, String channel, String message);
}
