package io.github.quiltservertools.blockbotapi.sender;

import net.minecraft.text.Text;

public class MessageSender {
    private final Text name;
    private final Text displayName;

    public MessageSender(Text name, Text displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public Text getName() {
        return name;
    }

    public Text getDisplayName() {
        return displayName;
    }
}
