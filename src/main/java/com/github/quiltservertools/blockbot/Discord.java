package com.github.quiltservertools.blockbot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.security.auth.login.LoginException;

public class Discord {
    private final JDA jda;
    private final WebhookClient webhook;

    public Discord(Config config, MinecraftServer server) throws LoginException {
        jda = JDABuilder.createDefault(config.getIdentifier()).build();
        jda.addEventListener(new Listeners(config.getChannel(), server));
        System.out.println(config.getChannel());
        BlockBot.LOG.info("Setup discord bot with token provided");

        // Init webhook
        WebhookClientBuilder builder = new WebhookClientBuilder(config.getWebhook());
        builder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName("Webhook thread");
            thread.setDaemon(true);
            return thread;
        });
        builder.setWait(true);
        this.webhook = builder.build();
        builder.setDaemon(true);
    }

    public void shutdown() {
        jda.shutdownNow();
        webhook.close();
    }

    public void sendMessageToDiscord(ChatMessageC2SPacket packet, ServerPlayerEntity player) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(player.getName().asString());
        builder.setAvatarUrl("https://visage.surgeplay.com/face/" + player.getUuidAsString());
        builder.setContent(packet.getChatMessage());
        webhook.send(builder.build());

    }
}
