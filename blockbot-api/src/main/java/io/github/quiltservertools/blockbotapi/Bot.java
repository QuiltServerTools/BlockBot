package io.github.quiltservertools.blockbotapi;

import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public interface Bot {
    void onChatMessage(MessageSender sender, Component message);
    void onPlayerJoinMessage(ServerPlayer player);
    void onPlayerLeaveMessage(ServerPlayer player);
    void onPlayerDeath(ServerPlayer player, Component message);
    void onAdvancementGrant(ServerPlayer player, Advancement advancement);
    void onServerStart(MinecraftServer server);
    void onServerStop(MinecraftServer server);
    void onServerTick(MinecraftServer server);

    void sendRelayMessage(String content, String channel);
    void onRelayMessage(String content, String channel);
}
