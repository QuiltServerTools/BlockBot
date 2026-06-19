package io.github.quiltservertools.blockbotdiscord.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.quiltservertools.blockbotdiscord.extensions.linking.LinkingExtensionKt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @ModifyReturnValue(method = "canPlayerLogin", at = @At("RETURN"))
    private Component enforceAccountLinking(Component original, @Local(argsOnly = true) NameAndId playerConfigEntry) {
        var message = LinkingExtensionKt.canJoin(playerConfigEntry, server);
        if (message != null) {
            return message;
        }
        return original;
    }
}
