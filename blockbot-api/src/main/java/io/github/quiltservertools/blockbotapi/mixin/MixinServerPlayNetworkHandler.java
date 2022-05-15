package io.github.quiltservertools.blockbotapi.mixin;

import io.github.quiltservertools.blockbotapi.BlockBotApi;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import net.minecraft.network.encryption.ChatMessageSignature;
import net.minecraft.network.encryption.SignedChatMessage;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {
    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Inject(
        method = "handleMessage",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/encryption/SignedChatMessage;Lnet/minecraft/server/filter/TextStream$Message;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/registry/RegistryKey;)V"),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void sendChatMessageToDiscord(ChatMessageC2SPacket chatMessageC2SPacket, TextStream.Message message, CallbackInfo ci, Text text, ChatMessageSignature chatMessageSignature, SignedChatMessage signedChatMessage) {
        if (!BlockBotApi.STYLED_CHAT_COMPAT) {
            ChatMessageEvent.EVENT.invoker().message(new PlayerMessageSender(
                this.getPlayer(),
                MessageSender.MessageType.REGULAR
            ), signedChatMessage.method_44125());
        }
    }
}
