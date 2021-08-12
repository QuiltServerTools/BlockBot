package com.github.quiltservertools.blockbotapi.sender;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class PlayerMessageSender extends MessageSender {
    private final UUID uuid;
    private final ServerPlayerEntity player;

    public PlayerMessageSender(ServerPlayerEntity player, MessageType type) {
        super(player.getDisplayName(), type);
        this.uuid = player.getUuid();
        this.player = player;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
}
