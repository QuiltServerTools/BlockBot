package com.github.quiltservertools.blockbot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.*;
import com.github.quiltservertools.blockbot.api.Bot;
import com.github.quiltservertools.blockbot.api.event.ChatMessageEvent;
import com.github.quiltservertools.blockbot.api.event.PlayerAdvancementGrantEvent;
import com.github.quiltservertools.blockbot.api.event.PlayerDeathEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.Objects;

import static com.github.quiltservertools.blockbot.BlockBotUtils.getAvatarUrl;

public class BlockBotDiscord implements Bot {
    private WebhookClient webhook;
    private JDA jda;
    private Status status;

    @Override
    public void registerListeners(Config config, MinecraftServer server) throws LoginException {
        jda = JDABuilder.createDefault(config.getIdentifier()).build();
        jda.addEventListener(new Listeners(config, server));
        BlockBot.LOG.info("Setup discord bot with token provided");
        // Init webhook
        WebhookClientBuilder builder = new WebhookClientBuilder(config.getWebhook());
        builder.setHttpClient(new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build());
        builder.setDaemon(true);
        builder.setAllowedMentions(AllowedMentions.none().withParseUsers(true));
        this.webhook = builder.build();

        this.status = new Status();
    }

    public JDA getBot() {
        return jda;
    }

    @Override
    public void onChatMessage(ServerPlayerEntity player, String message) {
        webhook.send(prepareChatMessage(player, message));
    }

    private WebhookMessage prepareChatMessage(ServerPlayerEntity player, String message) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(player.getName().getString());
        builder.setAvatarUrl(getAvatarUrl(player));
        builder.setContent(message);
        return builder.build();
    }

    @Override
    public void serverStatus(boolean starting) {
        System.out.println(BlockBot.CONFIG.sendStatusMessages());
        if (!BlockBot.CONFIG.sendStatusMessages()) return;
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setAuthor(new WebhookEmbed.EmbedAuthor(BlockBot.CONFIG.getName(), BlockBot.CONFIG.getLogo(), null));
        builder.setColor(starting ? 3334259 : 14695980);
        builder.setDescription(starting ? "Server Started" : "Server Stopped");
        webhook.send(builder.build());
    }

    @Override
    public void onDeathMessage(ServerPlayerEntity player, Text message) {
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setAuthor(new WebhookEmbed.EmbedAuthor(player.getName().asString(), getAvatarUrl(player), null));
        String msg = message.getString().replaceFirst(player.getName().asString() + " ", "");
        msg = StringUtils.capitalize(msg);
        builder.setDescription(msg);
        builder.setColor(15789375);
        webhook.send(builder.build());
    }

    @Override
    public void onAdvancementGrant(ServerPlayerEntity player, Advancement advancement) {
        if (!BlockBot.CONFIG.sendAdvancementMessages()) return;
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setDescription(player.getName().getString() + " has made the advancement [" + Objects.requireNonNull(advancement.getDisplay()).getTitle().getString() + "]");
        builder.setColor(16771646);
        builder.setAuthor(new WebhookEmbed.EmbedAuthor(player.getName().asString(), getAvatarUrl(player), null));
        webhook.send(builder.build());
    }

    @Override
    public void onShutdown() {
        jda.shutdown();
        webhook.close();
    }

    @Override
    public void tickStatus(MinecraftServer server) {
        if (!BlockBot.CONFIG.showPresence()) return;
        Presence presence = jda.getPresence();
        Activity status = Activity.playing(BlockBot.CONFIG.getName() + " - " + this.status.getPlayerCount() + " online");
        presence.setActivity(status);
    }

    @Override
    public void onPlayerConnect(ServerPlayNetworkHandler handler, PacketSender packetSender, MinecraftServer server) {
        webhook.send(buildConnectMessage(handler.getPlayer(), true));
        tickStatus(server);
    }

    @Override
    public void onPlayerDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        webhook.send(buildConnectMessage(handler.getPlayer(), false));
        tickStatus(server);
    }

    @Override
    public void onAlert(String alert) {
        // BlockBot by default does nothing, may add admin channel functionality later
        // The alert method also remains unused at this time
    }

    private WebhookEmbed buildConnectMessage(ServerPlayerEntity player, boolean joined) {
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setAuthor(new WebhookEmbed.EmbedAuthor(player.getName().asString(), getAvatarUrl(player), null));
        builder.setDescription(joined ? "Joined the game" : "Left the game");
        builder.setColor(joined ? 3334259 : 14695980);
        return builder.build();
    }
}
