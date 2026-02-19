package com.dayssky.zenithplus.hud;

import com.dayssky.zenithplus.config.ZenithPlusConfig;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudPositionScreen extends Screen {
    private static final int SNAP_THRESHOLD = 5;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 3.0f;
    private static final float SCALE_STEP = 0.1f;

    private final Screen parent;
    private HudEntry dragging = null;
    private int dragOffsetX, dragOffsetY;

    public HudPositionScreen(Screen parent) {
        super(Component.literal("Edit HUD Positions"));
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        int centerX = width / 2;
        int centerY = height / 2;

        graphics.fill(centerX - 1, 0, centerX + 1, height, 0x40FFFFFF);
        graphics.fill(0, centerY - 1, width, centerY + 1, 0x40FFFFFF);

        for (HudEntry entry : HudManager.getAll()) {
            String text = entry.text.isEmpty() ? entry.displayName : entry.text;
            int baseWidth = font.width(text);
            int baseHeight = font.lineHeight;
            int scaledWidth = (int) (baseWidth * entry.scale);
            int scaledHeight = (int) (baseHeight * entry.scale);
            int x = entry.posX - scaledWidth / 2;
            int y = entry.posY - scaledHeight / 2;

            graphics.fill(x - 4, y - 4, x + scaledWidth + 4, y + scaledHeight + 4, 0x80000000);

            graphics.pose().pushPose();
            graphics.pose().translate(entry.posX, entry.posY, 0);
            graphics.pose().scale(entry.scale, entry.scale, 1.0f);
            graphics.drawString(font, text, -baseWidth / 2, -baseHeight / 2, entry.color);
            graphics.pose().popPose();

            String label = String.format("%s (%.1fx)", entry.displayName, entry.scale);
            graphics.drawString(font, label, x, y - 12, 0x888888);
        }

        String hint = HudManager.isEmpty()
            ? "No HUD entries registered"
            : "Drag to move, Scroll to resize. Press ESC to save.";
        graphics.drawCenteredString(font, hint, width / 2, height - 20, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (HudEntry entry : HudManager.getAll()) {
                if (isMouseOver(entry, mouseX, mouseY)) {
                    dragging = entry;
                    dragOffsetX = (int) mouseX - entry.posX;
                    dragOffsetY = (int) mouseY - entry.posY;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null && button == 0) {
            int centerX = width / 2;
            int centerY = height / 2;

            int newX = (int) mouseX - dragOffsetX;
            int newY = (int) mouseY - dragOffsetY;

            if (Math.abs(newX - centerX) < SNAP_THRESHOLD) newX = centerX;
            if (Math.abs(newY - centerY) < SNAP_THRESHOLD) newY = centerY;

            dragging.posX = Math.max(20, Math.min(width - 20, newX));
            dragging.posY = Math.max(20, Math.min(height - 20, newY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private boolean isMouseOver(HudEntry entry, double mouseX, double mouseY) {
        String text = entry.text.isEmpty() ? entry.displayName : entry.text;
        int baseWidth = font.width(text);
        int baseHeight = font.lineHeight;
        int scaledWidth = (int) (baseWidth * entry.scale);
        int scaledHeight = (int) (baseHeight * entry.scale);
        int x = entry.posX - scaledWidth / 2;
        int y = entry.posY - scaledHeight / 2;

        return mouseX >= x - 4 && mouseX <= x + scaledWidth + 4 &&
               mouseY >= y - 4 && mouseY <= y + scaledHeight + 4;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HudEntry entry : HudManager.getAll()) {
            if (isMouseOver(entry, mouseX, mouseY)) {
                float newScale = entry.scale + (float) verticalAmount * SCALE_STEP;
                entry.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClose() {
        HudManager.saveAllPositions();
        AutoConfig.getConfigHolder(ZenithPlusConfig.class).save();
        minecraft.setScreen(parent);
    }
}
