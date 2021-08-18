package io.github.quiltservertools.blockbotapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.Advancement;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerAdvancementGrantEvent {
    Event<PlayerAdvancementGrantEvent> EVENT = EventFactory.createArrayBacked(PlayerAdvancementGrantEvent.class, (listeners) -> (player, advancement) -> {
        for (PlayerAdvancementGrantEvent listener : listeners) {
            listener.onAdvancementGrant(player, advancement);
        }
    });

    void onAdvancementGrant(ServerPlayerEntity player, Advancement advancement);
}
