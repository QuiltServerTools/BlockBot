package io.github.quiltservertools.blockbotapi.mixin;

import io.github.quiltservertools.blockbotapi.BlockBotApi;
import io.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import io.github.quiltservertools.blockbotapi.sender.MessageSender;
import io.github.quiltservertools.blockbotapi.sender.PlayerMessageSender;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {
    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Inject(
        method = "handleDecoratedMessage",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/server/filter/FilteredMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/registry/RegistryKey;)V")
    )
    public void sendChatMessageToDiscord(FilteredMessage<SignedMessage> message, CallbackInfo ci) {
        if (!BlockBotApi.STYLED_CHAT_COMPAT) {
            ChatMessageEvent.EVENT.invoker().message(new PlayerMessageSender(
                this.getPlayer(),
                MessageSender.MessageType.REGULAR
            ), message.raw().getContent());
        }
    }
}
