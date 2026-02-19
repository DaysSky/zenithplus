package com.dayssky.zenithplus.boss;

import com.dayssky.zenithplus.ZenithPlusClient;
import com.dayssky.zenithplus.hud.HudCountdownTimer;
import com.dayssky.zenithplus.hud.HudEntry;
import com.dayssky.zenithplus.hud.HudManager;
import com.dayssky.zenithplus.utils.BossBarUtils;

public class VesperidysHelper {
    private static final HudEntry fightTimerHud = new HudEntry(
        "vesperidysTimer",
        "Vesperidys Fight Timer",
        100,
        120
    );
    private static final HudCountdownTimer timer = new HudCountdownTimer(fightTimerHud, "VesperidysTimer");

    public static void register() {
        HudManager.register(fightTimerHud);

        BossBarUtils.onBossBar("vesperidys_timer", name -> {
            if (name.contains("The Vesperidys") && ZenithPlusClient.getConfig().vesperidys.fightTimer) {
                timer.start(12.5);
            }
        });
    }

    public static void cleanup() {
        timer.stop();
    }
}
