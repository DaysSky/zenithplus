package com.dayssky.zenithplus.mixin;

import com.dayssky.zenithplus.utils.BossBarUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;

@Mixin(ClientboundBossEventPacket.class)
public class BossBarPacketMixin {

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V", at = @At("HEAD"))
    private void onHandle(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        ClientboundBossEventPacket packet = (ClientboundBossEventPacket) (Object) this;

        if (mc.isSameThread()) {
            BossBarUtils.handlePacket(packet);
        } else {
            mc.execute(() -> BossBarUtils.handlePacket(packet));
        }
    }
}
