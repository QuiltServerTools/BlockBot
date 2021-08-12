package com.github.quiltservertools.blockbotapi.sender;

import net.minecraft.text.Text;

public class MessageSender {
    private final Text name;
    private final MessageType type;

    public MessageSender(Text name, MessageType type) {
        this.name = name;
        this.type = type;
    }

    public Text getName() {
        return name;
    }

    public MessageType getType() {
        return type;
    }

    public enum MessageType {
        REGULAR,
        EMOTE,
        ANNOUNCEMENT
    }
}
