package com.github.quiltservertools.blockbotapi;

import com.github.quiltservertools.blockbotapi.event.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class BlockBotApi implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("BlockBot API");
    private static final Set<Bot> bots = new HashSet<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing BlockBotAPI");

    }

    public static Set<Bot> getBots() {
        return bots;
    }

    public static void registerBot(Bot bot) {
        BlockBotApi.bots.add(bot);

        ChatMessageEvent.EVENT.register(bot::onChatMessage);
        ServerPlayConnectionEvents.JOIN.register(bot::onPlayerConnect);
        ServerPlayConnectionEvents.DISCONNECT.register(bot::onPlayerDisconnect);
        PlayerDeathEvent.EVENT.register(bot::onPlayerDeath);
        PlayerAdvancementGrantEvent.EVENT.register(bot::onAdvancementGrant);
        ServerLifecycleEvents.SERVER_STARTED.register(bot::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(bot::onServerStop);

        LOGGER.info("Registered bot: " + bot);
    }

    public static void sendDiscordMessage(String content, String channel) {
        bots.forEach(bot -> bot.sendDiscordMessage(content, channel));
    }
}
