package io.github.quiltservertools.blockbotapi.sender;

import net.minecraft.core.HolderLookup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class MessageSender {
    private final Component name;
    private final Component displayName;
    private final MessageType type;
    private final HolderLookup.Provider wrapperLookup;

    public MessageSender(Component name, Component displayName, @NotNull MessageType type, HolderLookup.Provider wrapperLookup) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.wrapperLookup = wrapperLookup;
    }

    public static MessageSender of(CommandSourceStack commandSource, MessageType type) {
        var entity = commandSource.getEntity();
        MessageSender sender;
        if (entity instanceof ServerPlayer player) {
            sender = new PlayerMessageSender(
                player,
                type
            );
        } else {
            sender = new MessageSender(
                Component.literal(commandSource.getTextName()),
                commandSource.getDisplayName(),
                type,
                commandSource.registryAccess()
            );
        }
        return sender;
    }

    public Component getName() {
        return name;
    }

    @NotNull
    public MessageType getType() {
        return type;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public HolderLookup.Provider getWrapperLookup() {
        return wrapperLookup;
    }

    public enum MessageType {
        REGULAR,
        EMOTE,
        ANNOUNCEMENT
    }
}
