package com.dayssky.zenithplus.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;

public class ChatUtils {

    private static final Map<String, Consumer<String>> chatListeners = new HashMap<>();
    private static final Map<String, Consumer<String>> titleListeners = new HashMap<>();
    private static final Map<String, Consumer<String>> subtitleListeners = new HashMap<>();

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String text = message.getString();
            for (Consumer<String> listener : new ArrayList<>(chatListeners.values())) {
                listener.accept(text);
            }
        });
    }

    public static void onChatMessage(String key, Consumer<String> listener) {
        chatListeners.put(key, listener);
    }

    public static void onTitle(String key, Consumer<String> listener) {
        titleListeners.put(key, listener);
    }

    public static void onSubtitle(String key, Consumer<String> listener) {
        subtitleListeners.put(key, listener);
    }

    public static void removeChat(String key) {
        chatListeners.remove(key);
    }

    public static void removeTitle(String key) {
        titleListeners.remove(key);
    }

    public static void removeSubtitle(String key) {
        subtitleListeners.remove(key);
    }

    public static void handleTitle(Component title) {
        if (title == null) return;
        String text = title.getString();
        for (Consumer<String> listener : new ArrayList<>(titleListeners.values())) {
            listener.accept(text);
        }
    }

    public static void handleSubtitle(Component subtitle) {
        if (subtitle == null) return;
        String text = subtitle.getString();
        for (Consumer<String> listener : new ArrayList<>(subtitleListeners.values())) {
            listener.accept(text);
        }
    }

    public static void cleanup() {
        chatListeners.clear();
        titleListeners.clear();
        subtitleListeners.clear();
    }
}
