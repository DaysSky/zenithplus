package com.dayssky.zenithplus.clear;

import com.dayssky.zenithplus.ZenithPlusClient;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;

public class GBombHelper {

    public static boolean isGBombBee(Entity entity) {

        if (!ZenithPlusClient.getConfig().clear.gravityBomb.enabled) return false;
        if (!(entity instanceof Bee bee)) return false;
        if (!bee.hasCustomName()) return false;
        var name = bee.getCustomName();
        return name != null && "Gravity Bomb".equals(name.getString());
    }

    public static int getGlowColor() {
        return ZenithPlusClient.getConfig().clear.gravityBomb.color;
    }
}
