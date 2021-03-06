package com.github.quiltservertools.blockbot.mixin;

import com.github.quiltservertools.blockbot.api.event.ChatMessageEvent;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
    @Inject(method = "onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;filterText(Ljava/lang/String;Ljava/util/function/Consumer;)V"))
    public void sendChatMessageToDiscord(ChatMessageC2SPacket packet, CallbackInfo ci) {
        ChatMessageEvent.EVENT.invoker().message(((ServerPlayNetworkHandler)(Object)this).getPlayer(), packet.getChatMessage());
    }

}
