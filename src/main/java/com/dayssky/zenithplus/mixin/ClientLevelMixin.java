package com.dayssky.zenithplus.mixin;

import com.dayssky.zenithplus.boss.VesperidysHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @ModifyArg(
        method = {
            "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
            "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V",
            "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
            "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V"
        ),
        index = 0
    )
    private ParticleOptions recolorVesperidysRedstone(ParticleOptions options) {
        return VesperidysHelper.recolorParticle(options);
    }
}
