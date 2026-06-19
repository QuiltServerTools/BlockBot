package io.github.quiltservertools.blockbotapi.mixin;

import io.github.quiltservertools.blockbotapi.event.PlayerAdvancementGrantEvent;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements {
    @Shadow
    private ServerPlayer player;

    @Inject(
        method = "lambda$award$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
        )
    )
    public void announceAdvancement(AdvancementHolder advancement, DisplayInfo display, CallbackInfo ci) {
        PlayerAdvancementGrantEvent.EVENT.invoker().onAdvancementGrant(player, advancement.value());
    }
}
