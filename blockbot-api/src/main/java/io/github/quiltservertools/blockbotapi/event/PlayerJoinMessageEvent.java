package io.github.quiltservertools.blockbotapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerJoinMessageEvent {
    Event<PlayerJoinMessageEvent> EVENT = EventFactory.createArrayBacked(PlayerJoinMessageEvent.class, (listeners) -> (player) -> {
        for (PlayerJoinMessageEvent listener : listeners) {
            listener.onPlayerJoinMessage(player);
        }
    });

    void onPlayerJoinMessage(ServerPlayerEntity player);
}
