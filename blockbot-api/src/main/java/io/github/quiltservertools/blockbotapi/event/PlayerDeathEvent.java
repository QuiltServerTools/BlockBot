package io.github.quiltservertools.blockbotapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public interface PlayerDeathEvent {
    Event<PlayerDeathEvent> EVENT = EventFactory.createArrayBacked(PlayerDeathEvent.class, (listeners) -> (player, message) -> {
        for (PlayerDeathEvent listener: listeners) {
            listener.death(player, message);
        }
    });

    void death(ServerPlayer player, Component message);
}
