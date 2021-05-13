package com.github.quiltservertools.blockbot.command.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class DiscordCommandOutputHelper {

    public static ServerCommandSource buildCommandSource(MinecraftServer server, Member member, CommandOutput output) {
        boolean allowedOp = member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR);
        String username = '@' + member.getEffectiveName() + '#' + member.getUser().getDiscriminator();
        return new ServerCommandSource(output, Vec3d.ZERO, Vec2f.ZERO, server.getOverworld(), allowedOp ? 4 : 0,
                username, new LiteralText(username), server, null);
    }

    public static DiscordCommandOutput createOutput(TextChannel channel) {
        return new DiscordCommandOutput(channel);
    }
}
