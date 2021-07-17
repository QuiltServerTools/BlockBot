package com.github.quiltservertools.blockbotapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface DiscordMessageEvent {
    Event<DiscordMessageEvent> EVENT = EventFactory.createArrayBacked(DiscordMessageEvent.class, (listeners) -> (sender, channel, message) -> {
        for (DiscordMessageEvent listener : listeners) {
            ActionResult result = listener.message(sender, channel, message);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult message(String sender, String channel, String message);
}
