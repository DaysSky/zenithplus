package com.dayssky.zenithplus.mixin;

import com.dayssky.zenithplus.utils.ParticleUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;

@Mixin(ClientboundLevelParticlesPacket.class)
public class ParticlePacketMixin {

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V", at = @At("HEAD"))
    private void onHandle(CallbackInfo ci) {
        ParticleUtils.handlePacket((ClientboundLevelParticlesPacket) (Object) this);
    }
}
