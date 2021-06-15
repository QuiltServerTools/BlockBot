package com.github.quiltservertools.blockbot;

import net.minecraft.server.network.ServerPlayerEntity;

import java.time.LocalDateTime;

public class BlockBotUtils {
    public static String getAvatarUrl(ServerPlayerEntity player) {
        String url = "https://crafatar.com/avatars/" + player.getUuidAsString() + "?t=" + LocalDateTime.now().getHour();
        if (BlockBot.CONFIG.enableSkinOverlay()) url = url.concat("&overlay");
        return url;
    }
}
