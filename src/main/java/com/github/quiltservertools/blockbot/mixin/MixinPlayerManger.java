package com.github.quiltservertools.blockbot.mixin;

import com.github.quiltservertools.blockbot.BlockBot;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManger {
    @Inject(method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("RETURN"))
    public void sendJoinMessageToDiscord(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        BlockBot.DISCORD.joinLeaveToDiscord(false, player);
    }
}
