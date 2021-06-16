package com.github.quiltservertools.blockbot.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ServerAlertEvent {
    Event<ServerAlertEvent> EVENT = EventFactory.createArrayBacked(ServerAlertEvent.class, (listeners) -> (message) -> {
        for(ServerAlertEvent listener : listeners) {
            listener.alert(message);
        }
    });

    void alert(String message);
}
