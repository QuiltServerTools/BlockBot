package com.github.quiltservertools.blockbot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Status {
    private final List<UUID> players;

    public Status() {
        players = new ArrayList<>();
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

}
