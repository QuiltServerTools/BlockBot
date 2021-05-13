package com.github.quiltservertools.blockbot;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;

public class BlockBot implements DedicatedServerModInitializer {
    public static Config CONFIG;
    public static Logger LOG;
    public static Discord DISCORD;

    @Override
    public void onInitializeServer() {
        LOG = LogManager.getLogger();
        CONFIG = new Config();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                DISCORD = new Discord(CONFIG, server);
                DISCORD.serverStatus(true);
            } catch (LoginException e) {
                e.printStackTrace();
                server.stop(false);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            CONFIG.shutdown();
            DISCORD.serverStatus(false);
            DISCORD.shutdown();
        });
    }
}
