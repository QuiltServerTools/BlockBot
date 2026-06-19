package io.github.quiltservertools.blockbotapi.sender;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;

public class PlayerMessageSender extends MessageSender {
    private final GameProfile profile;
    private final ServerPlayer player;

    public PlayerMessageSender(ServerPlayer player, MessageType type) {
        super(player.getName(), player.getDisplayName(), type, player.level().getServer().registryAccess());
        this.profile = player.getGameProfile();
        this.player = player;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
