package com.dayssky.zenithplus.boss;

import com.dayssky.zenithplus.ZenithPlusClient;
import com.dayssky.zenithplus.config.ZenithPlusConfig;
import com.dayssky.zenithplus.hud.HudCountdownTimer;
import com.dayssky.zenithplus.hud.HudEntry;
import com.dayssky.zenithplus.hud.HudManager;
import com.dayssky.zenithplus.utils.BossBarUtils;
import com.dayssky.zenithplus.utils.ChatUtils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;

public class BroodHelper {
    private static boolean active = false;
    private static Float corePhaseStartHealth = null;
    private static Slime trackedCore = null;
    private static boolean wasGlowing = false;
    private static final ZenithPlusConfig CONFIG = ZenithPlusClient.getConfig();
    private static final HudEntry timerHud = new HudEntry(
        "wpTimer",
        "Brood Weakpoint Timer",
        100,
        100
    );
    private static final HudCountdownTimer timer = new HudCountdownTimer(timerHud, "BroodTimer");

    public static void register() {
        HudManager.register(timerHud);

        BossBarUtils.onBossBar("broodmother", text -> {
            if (text.contains("The Broodmother")) {
                active = true;
                if (CONFIG.brood.weakpointTimer) {
                    timer.start(10);
                }
                ChatUtils.onChatMessage("broodmother_end", t -> {
                    if (t.startsWith("[Zenith Party] Your party earned 18")) {
                        cleanup();
                    }
                });
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!active || trackedCore == null) return;

            if (trackedCore.isRemoved()) {
                trackedCore = null;
                corePhaseStartHealth = null;
                wasGlowing = false;
                return;
            }

            boolean isGlowing = trackedCore.isCurrentlyGlowing();
            if (wasGlowing && !isGlowing) {
                corePhaseStartHealth = null;
                if (CONFIG.brood.weakpointTimer) {
                    timer.start(10);
                }
            }
            wasGlowing = isGlowing;
        });
    }

    public static boolean isBroodmother(Entity entity) {
        if (!active) return false;
        if (!(entity instanceof Slime slime)) return false;
        if (!slime.hasCustomName()) return false;
        var name = slime.getCustomName();
        return name != null && name.getString().contains("The Broodmother");
    }

    public static boolean isLimb(Entity entity) {
        if (!active) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        if (!living.hasCustomName()) return false;
        var name = living.getCustomName();
        return name != null && name.getString().contains("Broodmother Limb");
    }

    public static int getCoreHealthColor(Entity entity) {
        if (entity instanceof Slime slime && trackedCore != slime) {
            trackedCore = slime;
            wasGlowing = slime.isCurrentlyGlowing();
        }

        if (!(entity instanceof LivingEntity living)) return 0x00FF00;

        float currentHealth = living.getHealth();
        float maxHealth = living.getMaxHealth();

        if (corePhaseStartHealth == null) {
            corePhaseStartHealth = currentHealth;
        }

        float phaseEndHealth = corePhaseStartHealth - (maxHealth * 0.25f);
        float phaseRange = maxHealth * 0.25f;

        if (phaseRange <= 0) return 0x00FF00;

        float healthPercent = Math.max(0f, Math.min(1f, (currentHealth - phaseEndHealth) / phaseRange));
        return healthPercentToColor(healthPercent);
    }

    public static int getLimbHealthColor(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return 0x00FF00;

        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();
        if (maxHealth <= 0) return 0x00FF00;

        float healthPercent = Math.max(0f, Math.min(1f, health / maxHealth));
        return healthPercentToColor(healthPercent);
    }

    private static int healthPercentToColor(float healthPercent) {
        int red = (int) ((1f - healthPercent) * 255);
        int green = (int) (healthPercent * 255);
        return (red << 16) | (green << 8);
    }

    public static void cleanup() {
        timer.stop();
        active = false;
        corePhaseStartHealth = null;
        trackedCore = null;
        wasGlowing = false;
        ChatUtils.removeChat("broodmother_end");
    }
}
