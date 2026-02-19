package com.dayssky.zenithplus.hud;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dayssky.zenithplus.ZenithPlusClient;
import com.dayssky.zenithplus.config.ZenithPlusConfig.HudPosition;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class HudManager {
    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static final Map<String, HudEntry> entries = new LinkedHashMap<>();
    private static boolean registered = false;

    public static void register(HudEntry entry) {
        loadPosition(entry);
        entries.put(entry.id, entry);
        ensureCallbackRegistered();
    }

    public static void unregister(String id) {
        entries.remove(id);
    }

    public static HudEntry get(String id) {
        return entries.get(id);
    }

    public static Collection<HudEntry> getAll() {
        return entries.values();
    }

    public static boolean isEmpty() {
        return entries.isEmpty();
    }

    public static void saveAllPositions() {
        var config = ZenithPlusClient.getConfig();
        for (HudEntry entry : entries.values()) {
            config.hudPositions.put(entry.id, new HudPosition(entry.posX, entry.posY, entry.scale));
        }
    }

    private static void loadPosition(HudEntry entry) {
        var config = ZenithPlusClient.getConfig();
        HudPosition pos = config.hudPositions.get(entry.id);
        if (pos != null) {
            entry.posX = pos.x;
            entry.posY = pos.y;
            entry.scale = pos.scale;
        }
    }

    private static void ensureCallbackRegistered() {
        if (registered) return;
        registered = true;

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            if (MINECRAFT.player == null) return;
            if (MINECRAFT.options.hideGui) return;

            for (HudEntry entry : entries.values()) {
                if (!entry.visible || entry.text.isEmpty()) continue;
                render(graphics, entry);
            }
        });
    }

    private static void render(GuiGraphics graphics, HudEntry entry) {
        graphics.pose().pushPose();
        graphics.pose().translate(entry.posX, entry.posY, 0);
        graphics.pose().scale(entry.scale, entry.scale, 1.0f);

        int textWidth = MINECRAFT.font.width(entry.text);
        int textHeight = MINECRAFT.font.lineHeight;
        int x = -textWidth / 2;
        int y = -textHeight / 2;
        graphics.drawString(MINECRAFT.font, entry.text, x, y, entry.color);

        graphics.pose().popPose();
    }
}
