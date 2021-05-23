package com.github.quiltservertools.blockbot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.minecraft.advancement.Advancement;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.Collections;
import java.util.Objects;


public class Discord {
    private final JDA jda;
    private final WebhookClient webhook;
    private final String name;
    private final String logo;
    private final Status status;

    public Discord(Config config, MinecraftServer server) throws LoginException {
        jda = JDABuilder.createDefault(config.getIdentifier()).build();
        jda.addEventListener(new Listeners(config, server));
        BlockBot.LOG.info("Setup discord bot with token provided");

        // Init webhook
        WebhookClientBuilder builder = new WebhookClientBuilder(config.getWebhook());
        builder.setHttpClient(new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build());
        builder.setDaemon(true);
        this.webhook = builder.build();
        this.name = config.getName();
        this.logo = config.getLogo();
        this.status = new Status();
    }

    public void shutdown() {
        jda.shutdown();
        webhook.close();
    }

    public Status getStatus() {
        return status;
    }

    public JDA getJda() {
        return jda;
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
        String url = "https://crafatar.com/avatars/" + UUID + "?t=" + LocalDateTime.now().getHour();
        if (BlockBot.CONFIG.enableSkinOverlay()) url = url.concat("&overlay");
        return url;
    }

    private WebhookMessage prepare(String username, String uuid, String content) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(username);
        builder.setAvatarUrl(getAvatar(uuid));
        builder.setContent(content);
        return builder.build();
    }

    public void serverStatus(boolean start) {
        if (!BlockBot.CONFIG.sendStatusMessages()) return;
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setAuthor(new WebhookEmbed.EmbedAuthor(this.name, this.logo, null));
        builder.setColor(start ? 3334259 : 14695980);
        builder.setDescription(start ? "Server Started" : "Server Stopped");
        webhook.send(builder.build());
    }

    public void sendDeathMessage(ServerPlayerEntity player, Text text) {
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setAuthor(new WebhookEmbed.EmbedAuthor(player.getName().asString(), getAvatar(player.getUuidAsString()), null));
        String message = text.getString();
        message = message.replaceFirst(player.getName().asString() + " ", "");
        message = StringUtils.capitalize(message);
        builder.setDescription(message);
        builder.setColor(15789375);
        webhook.send(builder.build());
    }

    public void sendAdvancementMessage(ServerPlayerEntity player, Advancement advancement) {
        if (!BlockBot.CONFIG.sendAdvancementMessages()) return;
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setDescription(player.getName().getString() + " has made the advancement [" + Objects.requireNonNull(advancement.getDisplay()).getTitle().getString() + "]");
        builder.setColor(16771646);
        builder.setAuthor(new WebhookEmbed.EmbedAuthor(player.getName().asString(), getAvatar(player.getUuidAsString()), null));
        webhook.send(builder.build());
    }
}
