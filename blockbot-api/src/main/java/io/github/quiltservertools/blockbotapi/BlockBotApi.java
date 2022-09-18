package io.github.quiltservertools.blockbotapi;

import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.event.PlayerAdvancementGrantEvent;
import io.github.quiltservertools.blockbotapi.event.PlayerDeathEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import me.drex.vanish.api.VanishAPI;
import me.drex.vanish.api.VanishEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class BlockBotApi implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    private static final Set<Bot> bots = new HashSet<>();

    public static final boolean VANISH = FabricLoader.getInstance().isModLoaded("melius-vanish");

    @Override
    public void onInitialize() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> ChatMessageEvent.EVENT.invoker().message(
            new PlayerMessageSender(sender, MessageSender.MessageType.REGULAR),
            message.getContent()
        ));
    }

    public static Set<Bot> getBots() {
        return bots;
    }

    public static void registerBot(Bot bot) {
        BlockBotApi.bots.add(bot);

        ChatMessageEvent.EVENT.register(bot::onChatMessage);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (shouldSend(handler.player)) bot.onPlayerConnect(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (shouldSend(handler.player)) bot.onPlayerDisconnect(handler.player);
        });
        PlayerDeathEvent.EVENT.register((player, message) -> {
            if (shouldSend(player)) bot.onPlayerDeath(player, message);
        });
        PlayerAdvancementGrantEvent.EVENT.register((player, advancement) -> {
            if (shouldSend(player)) bot.onAdvancementGrant(player, advancement);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(bot::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(bot::onServerStop);
        ServerTickEvents.END_SERVER_TICK.register(bot::onServerTick);
        if (VANISH) {
            VanishEvents.VANISH_EVENT.register((player, vanished) -> {
                if (vanished) {
                    bot.onPlayerDisconnect(player);
                } else {
                    bot.onPlayerConnect(player);
                }
            });
        }

        LOGGER.info("Registered bot: " + bot);
    }

    public static boolean shouldSend(ServerPlayerEntity player) {
        if (VANISH) {
            return !VanishAPI.isVanished(player);
        } else {
            return true;
        }
    }

    public static void sendRelayMessage(String content, String channel) {
        bots.forEach(bot -> bot.sendRelayMessage(content, channel));
    }
}
