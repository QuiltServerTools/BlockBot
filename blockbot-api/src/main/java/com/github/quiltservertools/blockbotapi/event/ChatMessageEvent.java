package com.github.quiltservertools.blockbotapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface ChatMessageEvent {
    Event<ChatMessageEvent> EVENT = EventFactory.createArrayBacked(ChatMessageEvent.class, (listeners) -> (player, message) -> {
        for(ChatMessageEvent listener: listeners) {
            listener.message(player, message);
        }
    });

    void message(@Nullable ServerPlayerEntity player, String message);
}
