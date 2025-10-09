package io.github.quiltservertools.blockbotdiscord.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.quiltservertools.blockbotdiscord.extensions.linking.LinkingExtensionKt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @ModifyReturnValue(method = "checkCanJoin", at = @At("RETURN"))
    private Text enforceAccountLinking(Text original, @Local(argsOnly = true) PlayerConfigEntry playerConfigEntry) {
        var message = LinkingExtensionKt.canJoin(playerConfigEntry, server);
        if (message != null) {
            return message;
        }
        return original;
    }
}
