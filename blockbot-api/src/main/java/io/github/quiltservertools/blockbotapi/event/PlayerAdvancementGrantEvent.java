package io.github.quiltservertools.blockbotapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerAdvancementGrantEvent {
    Event<PlayerAdvancementGrantEvent> EVENT = EventFactory.createArrayBacked(PlayerAdvancementGrantEvent.class, (listeners) -> (player, advancement) -> {
        for (PlayerAdvancementGrantEvent listener : listeners) {
            listener.onAdvancementGrant(player, advancement);
        }
    });

    void onAdvancementGrant(ServerPlayer player, Advancement advancement);
}
