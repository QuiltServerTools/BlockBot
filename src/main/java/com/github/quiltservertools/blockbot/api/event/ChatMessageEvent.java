package com.github.quiltservertools.blockbot.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ChatMessageEvent {
    Event<ChatMessageEvent> EVENT = EventFactory.createArrayBacked(ChatMessageEvent.class, (listeners) -> (player, message) -> {
        for(ChatMessageEvent listener: listeners) {
            listener.message(player, message);
        }
    });

    void message(ServerPlayerEntity player, String message);
}
