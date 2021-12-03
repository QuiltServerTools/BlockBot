package io.github.quiltservertools.blockbotapi.mixin;

import io.github.quiltservertools.blockbotapi.BlockBotApi;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {
    @Inject(method = "onChatMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;filterText(Ljava/lang/String;Ljava/util/function/Consumer;)V"))
    public void sendChatMessageToDiscord(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (!BlockBotApi.STYLED_CHAT_COMPAT) {
            ChatMessageEvent.EVENT.invoker().message(new PlayerMessageSender(
                ((ServerPlayNetworkHandler) (Object) this).getPlayer(),
                MessageSender.MessageType.REGULAR
            ), Text.of(packet.getChatMessage()));
        }
    }
}
