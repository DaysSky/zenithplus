package com.dayssky.zenithplus.boss;

import com.dayssky.zenithplus.ZenithPlusClient;
import com.dayssky.zenithplus.hud.HudCountdownTimer;
import com.dayssky.zenithplus.hud.HudEntry;
import com.dayssky.zenithplus.hud.HudManager;
import com.dayssky.zenithplus.utils.BossBarUtils;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;

import org.joml.Vector3f;

public class VesperidysHelper {
    private static final HudEntry fightTimerHud = new HudEntry(
        "vesperidysTimer",
        "Vesperidys Fight Timer",
        100,
        120
    );
    private static final HudCountdownTimer timer = new HudCountdownTimer(fightTimerHud, "VesperidysTimer");
    private static boolean inFight;

    public static void register() {
        HudManager.register(fightTimerHud);

        BossBarUtils.onBossBar("vesperidys_timer", name -> {
            if (!name.contains("The Vesperidys")) {
                return;
            }

            inFight = true;

            if (ZenithPlusClient.getConfig().vesperidys.fightTimer) {
                timer.start(12.5);
            }
        });
    }

    public static ParticleOptions recolorParticle(ParticleOptions options) {
        var config = ZenithPlusClient.getConfig().vesperidys;
        if (!inFight || !config.tpHighlight.enabled) {
            return options;
        }

        if (!(options instanceof DustParticleOptions dust) || dust.getScale() != 0.5F) {
            return options;
        }

        Vector3f color = dust.getColor();
        if (color.x() != 1.0F || color.y() != 0.0F || color.z() != 0.0F) {
            return options;
        }

        int particleColor = config.tpHighlight.color & 0xFFFFFF;
        float size = Mth.clamp(0.5F, config.tpHighlight.size, 5F);
        float red = (particleColor >> 16 & 0xFF) / 255.0F;
        float green = (particleColor >> 8 & 0xFF) / 255.0F;
        float blue = (particleColor  & 0xFF) / 255.0F;
        return new DustParticleOptions(new Vector3f(red, green, blue), size);
    }

    public static void cleanup() {
        inFight = false;
        timer.stop();
    }
}
