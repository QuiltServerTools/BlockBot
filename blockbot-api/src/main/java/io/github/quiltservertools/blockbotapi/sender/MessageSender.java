package io.github.quiltservertools.blockbotapi.sender;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class MessageSender {
    private final Text name;
    private final Text displayName;
    private final MessageType type;

    public MessageSender(Text name, Text displayName, @NotNull MessageType type) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
    }

    public static MessageSender of(ServerCommandSource commandSource, MessageType type) {
        var entity = commandSource.getEntity();
        MessageSender sender;
        if (entity instanceof ServerPlayerEntity player) {
            sender = new PlayerMessageSender(
                player,
                type
            );
        } else {
            sender = new MessageSender(
                Text.literal(commandSource.getName()),
                commandSource.getDisplayName(),
                type
            );
        }
        return sender;
    }

    public Text getName() {
        return name;
    }

    @NotNull
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
