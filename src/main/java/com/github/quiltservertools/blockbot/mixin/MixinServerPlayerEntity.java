package com.github.quiltservertools.blockbot.mixin;

import com.github.quiltservertools.blockbot.BlockBot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {

    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    public void sendDeathMessageToDiscord(DamageSource source, CallbackInfo ci) {
        Text message = ((ServerPlayerEntity) (Object) this).getDamageTracker().getDeathMessage();
        BlockBot.DISCORD.sendDeathMessage((ServerPlayerEntity) (Object) this, message);
    }
}
