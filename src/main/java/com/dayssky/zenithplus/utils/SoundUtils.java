package com.dayssky.zenithplus.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;

public class SoundUtils {

    private static final Map<String, Consumer<ClientboundSoundPacket>> soundListeners = new HashMap<>();

    public static void onSound(String key, Consumer<ClientboundSoundPacket> listener) {
        soundListeners.put(key, listener);
    }

    public static void removeSound(String key) {
        soundListeners.remove(key);
    }

    public static void handlePacket(ClientboundSoundPacket packet) {
        if (soundListeners.isEmpty()) return;

        for (Consumer<ClientboundSoundPacket> listener : new ArrayList<>(soundListeners.values())) {
            listener.accept(packet);
        }
    }

    public static void cleanup() {
        soundListeners.clear();
    }
}
