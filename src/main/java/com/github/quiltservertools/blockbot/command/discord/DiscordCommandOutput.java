package com.github.quiltservertools.blockbot.command.discord;

import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;

import java.util.UUID;

public class DiscordCommandOutput implements CommandOutput {
    private final StringBuffer buffer = new StringBuffer();
    private boolean shouldBuffer = true;
    private final TextChannel channel;

    public DiscordCommandOutput(TextChannel channel) {
        this.channel = channel;
    }

    @Override
    public void sendSystemMessage(Text message, UUID senderUuid) {
        String content = message.getString();

        if (shouldBuffer) {
            if (buffer.length() + content.length() > 2000) { // discord character limit
                channel.sendMessage(buffer).queue();
                buffer.delete(0, buffer.length());
            }

            if (buffer.length() > 0) {
                buffer.append('\n');
            }
            buffer.append(content);
        } else {
            this.channel.sendMessage(content).queue();
        }
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return true;
    }

    @Override
    public boolean shouldTrackOutput() {
        return true;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return true;
    }

    public void sendBufferedContent() {
        if (this.buffer.length() > 0) {
            this.channel.sendMessage(this.buffer).queue();
        }
        this.shouldBuffer = false;
    }
}
