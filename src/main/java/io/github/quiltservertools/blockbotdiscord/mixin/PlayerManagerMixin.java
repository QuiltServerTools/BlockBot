package io.github.quiltservertools.blockbotdiscord.mixin;

import com.mojang.authlib.GameProfile;
import io.github.quiltservertools.blockbotdiscord.extensions.linking.LinkingExtensionKt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.Whitelist;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    private boolean whitelistEnabled;

    @Shadow
    @Final
    private Whitelist whitelist;

    @Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
    private void enforceAccountLinking(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        if (whitelistEnabled && whitelist.isAllowed(profile)) return;

        var message = LinkingExtensionKt.canJoin(profile, server);
        if (message != null) {
            cir.setReturnValue(message);
        }
    }
}
