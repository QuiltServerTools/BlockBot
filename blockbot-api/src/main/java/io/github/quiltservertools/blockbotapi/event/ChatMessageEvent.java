package io.github.quiltservertools.blockbotapi.event;

import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;

public interface ChatMessageEvent {
    Event<ChatMessageEvent> EVENT = EventFactory.createArrayBacked(ChatMessageEvent.class, (listeners) -> (sender, message) -> {
        for(ChatMessageEvent listener: listeners) {
            listener.message(sender, message);
        }
    });

    void message(MessageSender sender, Text message);
}
