package io.github.quiltservertools.blockbotapi;

import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.advancement.Advancement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface Bot {
    void onChatMessage(MessageSender sender, Text message);
    void onPlayerConnect(ServerPlayerEntity player);
    void onPlayerDisconnect(ServerPlayerEntity player);
    void onPlayerDeath(ServerPlayerEntity player, Text message);
    void onAdvancementGrant(ServerPlayerEntity player, Advancement advancement);
    void onServerStart(MinecraftServer server);
    void onServerStop(MinecraftServer server);
    void onServerTick(MinecraftServer server);

    void sendRelayMessage(String content, String channel);
    void onRelayMessage(String content, String channel);
}
