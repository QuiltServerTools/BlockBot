package com.github.quiltservertools.blockbotapi.mixin;

import com.github.quiltservertools.blockbotapi.event.ChatMessageEvent;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Nullable
    public abstract ServerPlayerEntity getPlayer(UUID uuid);

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void onBroadcastChatMessage(Text message, MessageType type, UUID senderUuid, CallbackInfo ci) {
        ServerPlayerEntity player = this.getPlayer(senderUuid);
        String content = getContent(message);
        if (content != null && !content.isBlank()) {
            ChatMessageEvent.EVENT.invoker().message(player, content);
        }
    }

    @Unique
    @Nullable
    private static String getContent(Text text) {
        if (text instanceof TranslatableText translatableText) {
            // TODO only do this for chat keys
            Object[] args = translatableText.getArgs();
            if (args.length == 2) {
                Object content = args[1];
                if (content instanceof String) {
                    return ((String) content);
                } else if (content instanceof StringVisitable) {
                    return ((StringVisitable) content).getString();
                }
            }
        } else {
            return text.getString();
        }
        return null;
    }
}
