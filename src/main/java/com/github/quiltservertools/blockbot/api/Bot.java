package com.github.quiltservertools.blockbot.api;

import com.github.quiltservertools.blockbot.Config;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.advancement.Advancement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import javax.security.auth.login.LoginException;

public interface Bot {
    void registerListeners(Config config, MinecraftServer server) throws LoginException;

    void onChatMessage(ServerPlayerEntity player, String message);

    void serverStatus(boolean starting);

    void onDeathMessage(ServerPlayerEntity player, Text message);

    void onAdvancementGrant(ServerPlayerEntity player, Advancement advancement);

    void onShutdown();

    void tickStatus(MinecraftServer server);

    void onPlayerConnect(ServerPlayNetworkHandler handler, PacketSender packetSender, MinecraftServer server);

    void onPlayerDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server);

    void onAlert(String alert);
}
