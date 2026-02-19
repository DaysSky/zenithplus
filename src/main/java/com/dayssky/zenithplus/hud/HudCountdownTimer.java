package com.dayssky.zenithplus.hud;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;

public class HudCountdownTimer {
    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    private final HudEntry entry;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;

    public HudCountdownTimer(HudEntry entry, String threadName) {
        this.entry = entry;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, threadName);
            t.setDaemon(true);
            return t;
        });
    }

    public void start(double durationSeconds) {
        stop();
        long durationMs = (long) (durationSeconds * 1000.0);
        if (durationMs <= 0) return;

        long endTime = System.currentTimeMillis() + durationMs;
        entry.show();

        task = scheduler.scheduleAtFixedRate(() -> {
            long remaining = endTime - System.currentTimeMillis();
            MINECRAFT.execute(() -> {
                if (remaining <= 0) {
                    stop();
                    return;
                }

                float timePercent = Math.max(0f, Math.min(1f, remaining / (float) durationMs));
                int red = (int) (timePercent * 255);
                int green = (int) ((1f - timePercent) * 255);
                int color = (red << 16) | (green << 8);
                entry.set(String.format("%.2f", remaining / 1000.0), color);
            });
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        entry.hide();
    }
}
