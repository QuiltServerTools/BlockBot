package io.github.quiltservertools.blockbotapi.sender;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;

public class ServerMessageSender extends MessageSender {
    public ServerMessageSender(MinecraftServer server) {
        super(new LiteralText(server.getCommandSource().getName()), server.getCommandSource().getDisplayName());
    }
}
