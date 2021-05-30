package com.github.quiltservertools.blockbot;

import com.github.quiltservertools.blockbot.command.discord.DiscordCommandOutput;
import com.github.quiltservertools.blockbot.command.discord.DiscordCommandOutputHelper;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Listeners extends ListenerAdapter {
    private final Config config;
    private final MinecraftServer server;

    public Listeners(Config config, MinecraftServer server) {
        this.config = config;
        this.server = server;
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server1) -> {
            BlockBot.DISCORD.joinLeaveToDiscord(false, handler.player);
            BlockBot.DISCORD.getStatus().addPlayer(handler.player.getUuid());
            BlockBot.DISCORD.getStatus().update();
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, sender) -> {
            BlockBot.DISCORD.joinLeaveToDiscord(true, handler.player);
            BlockBot.DISCORD.getStatus().removePlayer(handler.player.getUuid());
            BlockBot.DISCORD.getStatus().update();
        });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (event.getChannel().getId().equals(config.getChannel()) && !message.getAuthor().isBot()) {
            String content = message.getContentRaw();
            if (config.enableInlineCommands() && content.startsWith("//")) { // inline commands
                String minecraftCommand = content.substring(2);
                this.server.execute(() -> {
                    DiscordCommandOutput output = DiscordCommandOutputHelper.createOutput(event.getTextChannel());
                    this.server.getCommandManager().execute(DiscordCommandOutputHelper.buildCommandSource(
                            this.server,
                            Objects.requireNonNull(event.getMember(), "event.getMember()"),
                            output, event.getMember().getRoles().stream().anyMatch(config::adminRole)
                    ), minecraftCommand);
                    output.sendBufferedContent();
                });
            } else {
                if (!botCommand(message)) {
                    sendMessageToGame(server, event);
                }
            }
        }
    }

    private void sendMessageToGame(MinecraftServer server, MessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();
        int colour = Objects.requireNonNull(event.getMember()).getColorRaw();
        Text message = new LiteralText(msg);
        MutableText sender = new LiteralText("<§7§o@§r").append(new LiteralText(event.getMember().getEffectiveName()).styled(style -> style.withColor(TextColor.fromRgb(colour)))).append(new LiteralText("> "));
        if (!event.getMessage().getAttachments().isEmpty()) {
            event.getMessage().getAttachments().forEach(attachment -> server.getPlayerManager().broadcastChatMessage(sender.append(new LiteralText(attachment.getUrl()).styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, attachment.getUrl())))), MessageType.SYSTEM, Util.NIL_UUID));
            if (!msg.equals("")) server.getPlayerManager().broadcastChatMessage(sender.append(message), MessageType.SYSTEM, Util.NIL_UUID);
        } else {
            server.getPlayerManager().broadcastChatMessage(sender.append(message), MessageType.SYSTEM, Util.NIL_UUID);
        }

    }

    private boolean botCommand(Message message) {
        if (!message.getContentRaw().startsWith(config.getCommandPrefix())) return false;
        String msg = message.getContentRaw().replaceFirst(this.config.getCommandPrefix(), "");
        if (msg.startsWith("whitelist")) {
            if (!this.config.whitelistCommandEnabled()) return false;
            String user = msg.replaceFirst("whitelist add ", "");
            this.server.getCommandManager().execute(this.server.getCommandSource(), "whitelist add " + user);
            message.getChannel().sendMessage("Whitelisted " + user).queue();
            return true;
        }
        return false;
    }
}
