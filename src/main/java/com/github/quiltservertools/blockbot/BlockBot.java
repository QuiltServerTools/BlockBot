package com.github.quiltservertools.blockbot;

import com.github.quiltservertools.blockbot.api.Bot;
import com.github.quiltservertools.blockbot.api.event.ChatMessageEvent;
import com.github.quiltservertools.blockbot.api.event.PlayerAdvancementGrantEvent;
import com.github.quiltservertools.blockbot.api.event.PlayerDeathEvent;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public class BlockBot implements DedicatedServerModInitializer {
    public static Config CONFIG;
    public static Logger LOG;
    public static final List<Bot> bots = new ArrayList<>();

    @Override
    public void onInitializeServer() {
        LOG = LogManager.getLogger();
        CONFIG = new Config();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                var bot = new BlockBotDiscord();
                registerBot(bot, CONFIG, server);
            } catch (LoginException e) {
                e.printStackTrace();
                server.stop(false);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> bots.forEach(bot -> bot.serverStatus(true)));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            CONFIG.shutdown();
            bots.forEach(bot -> {
                bot.serverStatus(false);
                bot.onShutdown();
            });
        });
    }

    public void registerBot(Bot bot, Config config, MinecraftServer server) throws LoginException {
        bots.add(bot);
        bot.registerListeners(config, server);
        // Events
        ChatMessageEvent.EVENT.register(bot::onChatMessage);
        PlayerDeathEvent.EVENT.register(bot::onDeathMessage);
        PlayerAdvancementGrantEvent.EVENT.register(bot::onAdvancementGrant);

        ServerPlayConnectionEvents.JOIN.register(bot::onPlayerConnect);
        ServerPlayConnectionEvents.DISCONNECT.register(bot::onPlayerDisconnect);
    }
}
