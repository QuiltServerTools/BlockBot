package com.github.quiltservertools.blockbot;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Status {
    private List<UUID> players;
    public Status() {
        players = new ArrayList<>();
    }

    private int getPlayerCount() {
        return players.size();
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }
    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public void update() {
        if (!BlockBot.CONFIG.showPresence()) return;
        Presence presence = BlockBot.DISCORD.getJda().getPresence();
        Activity status = Activity.playing(BlockBot.CONFIG.getName() + " - " + getPlayerCount() + " online");
        presence.setActivity(status);
    }
}
