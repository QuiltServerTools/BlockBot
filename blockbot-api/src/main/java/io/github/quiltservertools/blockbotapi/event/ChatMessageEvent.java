package io.github.quiltservertools.blockbotapi.event;

import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.MessageType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ChatMessageEvent {
    Event<ChatMessageEvent> EVENT = EventFactory.createArrayBacked(ChatMessageEvent.class, (listeners) -> (sender, type, message) -> {
        for(ChatMessageEvent listener: listeners) {
            listener.message(sender, type, message);
        }
    });

    void message(MessageSender sender, MessageType type, String message);
}
