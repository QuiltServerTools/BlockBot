package io.github.quiltservertools.blockbotapi.mixin;

import io.github.quiltservertools.blockbotapi.event.PlayerAdvancementGrantEvent;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class MixinPlayerAdvancementTracker {
    @Shadow
    private ServerPlayerEntity owner;

    @Inject(
        method = "grantCriterion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"
        )
    )
    public void announceAdvancement(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        PlayerAdvancementGrantEvent.EVENT.invoker().onAdvancementGrant(owner, advancement);
    }
}
