package com.dayssky.zenithplus.mixin;

import com.dayssky.zenithplus.boss.BroodHelper;
import com.dayssky.zenithplus.clear.GBombHelper;
import com.dayssky.zenithplus.clear.SidearmHelper;
import com.dayssky.zenithplus.utils.EntityUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void changeGlowingColor(CallbackInfoReturnable<Integer> cir) {
        Entity e = (Entity) (Object) this;

        if (EntityUtils.isFakeEntity(e)) {
            Integer color = EntityUtils.getGlowColor(e);
            if (color != null) {
                cir.setReturnValue(color);
                return;
            }
        } else if (BroodHelper.isCore(e)) {
            cir.setReturnValue(BroodHelper.getCoreHealthColor(e));
            return;
        } else if (BroodHelper.isLimb(e)) {
            cir.setReturnValue(BroodHelper.getLimbHealthColor(e));
            return;
        } else if (GBombHelper.isGBombBee(e)) {
            cir.setReturnValue(GBombHelper.getGlowColor());
        } else if (SidearmHelper.isOneShot(e)) {
            cir.setReturnValue(SidearmHelper.getGlowColor());
        }
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void forceGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity e = (Entity) (Object) this;
        if (EntityUtils.isFakeEntity(e) || SidearmHelper.isOneShot(e)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "displayFireAnimation", at = @At("HEAD"), cancellable = true)
    private void hideBroodmotherFire(CallbackInfoReturnable<Boolean> cir) {
        Entity e = (Entity) (Object) this;
        if (BroodHelper.isBroodmother(e) || BroodHelper.isLimb(e)) {
            cir.setReturnValue(false);
        }
    }
}
