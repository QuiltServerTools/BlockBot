package io.github.quiltservertools.blockbotapi.mixin;

import io.github.quiltservertools.blockbotapi.event.PlayerDeathEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer {
    @Inject(
        method = "die",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
        )
    )
    public void sendDeathMessageToDiscord(DamageSource source, CallbackInfo ci) {
        source.getLocalizedDeathMessage((ServerPlayer) (Object) this).getString();
        PlayerDeathEvent.EVENT.invoker().death((ServerPlayer) (Object) this, source.getLocalizedDeathMessage((ServerPlayer) (Object) this));
    }
}
