package com.github.quiltservertools.blockbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Listeners extends ListenerAdapter {
    private final String channel;
    private final MinecraftServer server;

    public Listeners(String channel, MinecraftServer server) {
        this.channel = channel;
        this.server = server;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getId().equals(channel) && !event.getMessage().isWebhookMessage()) sendMessageToGame(server, event);
    }

    private void sendMessageToGame(MinecraftServer server, MessageReceivedEvent event) {
        String msg = event.getMessage().getAttachments().isEmpty() ? event.getMessage().getContentDisplay() : "[Image] " + event.getMessage().getContentDisplay();
        int colour = Objects.requireNonNull(event.getMember()).getColorRaw();
        Text message = new LiteralText(msg);
        MutableText sender = new LiteralText("<@").append(new LiteralText(event.getAuthor().getName()).styled(style -> style.withColor(TextColor.fromRgb(colour)))).append(new LiteralText("> "));
        server.getPlayerManager().broadcastChatMessage(sender.append(message), MessageType.SYSTEM, Util.NIL_UUID);
    }
}
