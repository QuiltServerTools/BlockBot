package io.github.quiltservertools.blockbotapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerLeaveMessageEvent {
    Event<PlayerLeaveMessageEvent> EVENT = EventFactory.createArrayBacked(PlayerLeaveMessageEvent.class, (listeners) -> (player) -> {
        for (PlayerLeaveMessageEvent listener : listeners) {
            listener.onPlayerLeaveMessage(player);
        }
    });

    void onPlayerLeaveMessage(ServerPlayer player);
}
