package com.dayssky.zenithplus.config;

import com.dayssky.zenithplus.hud.HudPositionScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ZenithPlusModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Screen autoScreen = AutoConfig.getConfigScreen(ZenithPlusConfig.class, parent).get();
            return new ConfigScreenWrapper(autoScreen, parent);
        };
    }

    private static class ConfigScreenWrapper extends Screen {
        private final Screen wrapped;
        private final Screen parent;
        private boolean initialized = false;
        private Button hudButton;

        protected ConfigScreenWrapper(Screen wrapped, Screen parent) {
            super(wrapped.getTitle());
            this.wrapped = wrapped;
            this.parent = parent;
        }

        @Override
        protected void init() {
            if (!initialized) {
                wrapped.init(minecraft, width, height);
                initialized = true;
            }

            hudButton = Button.builder(Component.literal("Edit HUD"), button -> {
                minecraft.setScreen(new HudPositionScreen(this));
            }).bounds(width - 160, 6, 150, 20).build();
            addRenderableWidget(hudButton);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            wrapped.render(graphics, mouseX, mouseY, delta);
            hudButton.render(graphics, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) return true;
            return wrapped.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (super.mouseReleased(mouseX, mouseY, button)) return true;
            return wrapped.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
            return wrapped.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
            return wrapped.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
            return wrapped.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            if (super.charTyped(chr, modifiers)) return true;
            return wrapped.charTyped(chr, modifiers);
        }

        @Override
        public void onClose() {
            wrapped.onClose();
        }

        @Override
        public void tick() {
            wrapped.tick();
        }
    }
}
