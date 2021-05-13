package com.github.quiltservertools.blockbot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.*;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Discord {
    private final JDA jda;
    private final WebhookClient webhook;

    public Discord(Config config, MinecraftServer server) throws LoginException {
        jda = JDABuilder.createDefault(config.getIdentifier()).build();
        jda.addEventListener(new Listeners(config, server));
        System.out.println(config.getChannel());
        BlockBot.LOG.info("Setup discord bot with token provided");

        // Init webhook
        WebhookClientBuilder builder = new WebhookClientBuilder(config.getWebhook());
        builder.setHttpClient(new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1)).build());
        builder.setDaemon(true);
        this.webhook = builder.build();
    }

    public void shutdown() {
        jda.shutdownNow();
        webhook.close();
    }

    public void sendMessageToDiscord(ChatMessageC2SPacket packet, ServerPlayerEntity player) {
        webhook.send(prepare(player.getName().asString(), player.getUuidAsString(), packet.getChatMessage()));
    }

    public void joinLeaveToDiscord(boolean left, ServerPlayerEntity player) {
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setAuthor(new WebhookEmbed.EmbedAuthor(player.getName().asString(), getAvatar(player.getUuidAsString()), null));
        builder.setDescription(left ? "Left the game" : "Joined the game");
        builder.setColor(left ? 14695980 : 3334259);
        webhook.send(builder.build());
    }

    private String getAvatar(String UUID) {
        return "https://visage.surgeplay.com/face/" + UUID;
    }

    private WebhookMessage prepare(String username, String uuid, String content) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(username);
        builder.setAvatarUrl(getAvatar(uuid));
        builder.setContent(content);
        return builder.build();
    }
}
