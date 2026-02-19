package com.dayssky.zenithplus.mixin;

import com.dayssky.zenithplus.ZenithPlusClient;
import com.dayssky.zenithplus.boss.BroodHelper;
import com.dayssky.zenithplus.clear.GBombHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void changeGlowingColor(CallbackInfoReturnable<Integer> cir) {
        Entity e = (Entity) (Object) this;

        if (ZenithPlusClient.getConfig().brood.coreHealthColor) {
            if (e instanceof Slime slime && slime.isCurrentlyGlowing() && BroodHelper.isBroodmother(e)) {
                cir.setReturnValue(BroodHelper.getCoreHealthColor(e));
                return;
            }
        }

        if (ZenithPlusClient.getConfig().brood.limbHealthColor) {
            if (BroodHelper.isLimb(e)) {
                cir.setReturnValue(BroodHelper.getLimbHealthColor(e));
                return;
            }
        }

        if (GBombHelper.isGBombBee(e)) {
            cir.setReturnValue(GBombHelper.getGlowColor());
        }
    }

    @Inject(method = "displayFireAnimation", at = @At("HEAD"), cancellable = true)
    private void hideBroodmotherFire(CallbackInfoReturnable<Boolean> cir) {
        Entity e = (Entity) (Object) this;

        if (!ZenithPlusClient.getConfig().brood.removeFire) return;

        if (BroodHelper.isBroodmother(e) || BroodHelper.isLimb(e)) {
            cir.setReturnValue(false);
            return;
        }

    }
}
