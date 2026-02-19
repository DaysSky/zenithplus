package com.dayssky.zenithplus.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;

public class BossBarUtils {
    private static final Map<String, Consumer<String>> bossBarCallbacks = new ConcurrentHashMap<>();

    public static void onBossBar(String id, Consumer<String> callback) {
        bossBarCallbacks.put(id, callback);
    }

    public static void removeBossBar(String id) {
        bossBarCallbacks.remove(id);
    }

    public static void handlePacket(ClientboundBossEventPacket packet) {
        packet.dispatch(new ClientboundBossEventPacket.Handler() {
            @Override
            public void add(
                java.util.UUID id,
                Component name,
                float progress,
                net.minecraft.world.BossEvent.BossBarColor color,
                net.minecraft.world.BossEvent.BossBarOverlay overlay,
                boolean darkenScreen,
                boolean playMusic,
                boolean createWorldFog
            ) {
                String nameStr = name.getString();
                for (Consumer<String> callback : bossBarCallbacks.values()) {
                    callback.accept(nameStr);
                }
            }

            @Override
            public void remove(java.util.UUID id) {}

            @Override
            public void updateProgress(java.util.UUID id, float progress) {}

            @Override
            public void updateName(java.util.UUID id, Component name) {}

            @Override
            public void updateStyle(java.util.UUID id, net.minecraft.world.BossEvent.BossBarColor color, net.minecraft.world.BossEvent.BossBarOverlay overlay) {}

            @Override
            public void updateProperties(java.util.UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {}
        });
    }

    public static void cleanup() {
        bossBarCallbacks.clear();
    }
}
