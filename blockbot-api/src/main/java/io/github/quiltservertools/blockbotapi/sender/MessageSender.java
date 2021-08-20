package io.github.quiltservertools.blockbotapi.sender;

import net.minecraft.text.Text;

public class MessageSender {
    private final Text name;
    private final Text displayName;
    private final MessageType type;

    public MessageSender(Text name, Text displayName, MessageType type) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
    }

    public Text getName() {
        return name;
    }

    public MessageType getType() {
        return type;
    }

    public Text getDisplayName() {
        return displayName;
    }

    public enum MessageType {
        REGULAR,
        EMOTE,
        ANNOUNCEMENT
    }
}
