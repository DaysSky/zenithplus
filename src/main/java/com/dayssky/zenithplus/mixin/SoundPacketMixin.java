package com.dayssky.zenithplus.mixin;

import com.dayssky.zenithplus.utils.SoundUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;

@Mixin(ClientboundSoundPacket.class)
public class SoundPacketMixin {

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void onHandle(CallbackInfo ci) {
        ClientboundSoundPacket packet = (ClientboundSoundPacket) (Object) this;

        SoundUtils.handlePacket(packet);

    }
}
