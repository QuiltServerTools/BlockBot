package com.github.quiltservertools.blockbot.mixin;

import com.github.quiltservertools.blockbot.BlockBot;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
    @Inject(method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;filterText(Ljava/lang/String;Ljava/util/function/Consumer;)V"))
    public void sendChatMessageToDiscord(ChatMessageC2SPacket packet, CallbackInfo ci) {
        BlockBot.DISCORD.sendMessageToDiscord(packet, ((ServerPlayNetworkHandler)(Object)this).player);
    }

    @Inject(method = "onDisconnected(Lnet/minecraft/text/Text;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    public void sendDisconnectMessageToDiscord(Text reason, CallbackInfo ci) {
        BlockBot.DISCORD.joinLeaveToDiscord(true, ((ServerPlayNetworkHandler)(Object)this).player);
    }

}
