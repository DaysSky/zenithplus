package com.dayssky.zenithplus.mixin;

import com.dayssky.zenithplus.boss.VesperidysHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @ModifyVariable(
        method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private ParticleOptions recolorParticle(ParticleOptions options) {
        return VesperidysHelper.recolorParticle(options);
    }

    @ModifyVariable(
        method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private ParticleOptions recolorForcedParticle(ParticleOptions options) {
        return VesperidysHelper.recolorParticle(options);
    }
}
