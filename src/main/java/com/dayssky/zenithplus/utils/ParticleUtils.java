package com.dayssky.zenithplus.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;

public class ParticleUtils {

    private static final Map<String, Consumer<ClientboundLevelParticlesPacket>> particleListeners = new HashMap<>();

    public static void onParticle(String key, Consumer<ClientboundLevelParticlesPacket> listener) {
        particleListeners.put(key, listener);
    }

    public static void removeParticle(String key) {
        particleListeners.remove(key);
    }

    public static void handlePacket(ClientboundLevelParticlesPacket packet) {
        if (particleListeners.isEmpty()) return;

        for (Consumer<ClientboundLevelParticlesPacket> listener : new ArrayList<>(particleListeners.values())) {
            listener.accept(packet);
        }
    }

    public static void cleanup() {
        particleListeners.clear();
    }
}
