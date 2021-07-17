package com.github.quiltservertools.blockbotapi;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.advancement.Advancement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface Bot {
    void onChatMessage(@Nullable ServerPlayerEntity player, String message);
    void onPlayerConnect(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server);
    void onPlayerDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server);
    void onPlayerDeath(ServerPlayerEntity player, Text message);
    void onAdvancementGrant(ServerPlayerEntity player, Advancement advancement);
    void onServerStart(MinecraftServer server);
    void onServerStop(MinecraftServer server);

    void sendDiscordMessage(String content, String channel);
    void onDiscordMessage(String content, String channel);
}
