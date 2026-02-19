package com.dayssky.zenithplus.config;

import java.util.HashMap;
import java.util.Map;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "zenithplus")
public class ZenithPlusConfig implements ConfigData {

    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.Excluded
    public Map<String, HudPosition> hudPositions = new HashMap<>();

    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    public static class HudPosition {
        public int x;
        public int y;
        public float scale = 1.0f;

        public HudPosition() {}

        public HudPosition(int x, int y, float scale) {
            this.x = x;
            this.y = y;
            this.scale = scale;
        }
    }

    @ConfigEntry.Category("Clear")
    @ConfigEntry.Gui.TransitiveObject
    public Clear clear = new Clear();

    @ConfigEntry.Category("Vesp")
    @ConfigEntry.Gui.TransitiveObject
    public Vesperidys vesperidys = new Vesperidys();

    @ConfigEntry.Category("Brood")
    @ConfigEntry.Gui.TransitiveObject
    public Brood brood = new Brood();

    public static class General {
        public boolean meow = true;
    }

    public static class Clear {
        @ConfigEntry.Gui.CollapsibleObject
        public GravityBomb gravityBomb = new GravityBomb();

        public static class GravityBomb {
            public boolean enabled = true;

            @ConfigEntry.ColorPicker
            public int color = 0x5555FF;
        }
    }

    public static class Brood {
        public boolean removeFire = true;
        public boolean limbHealthColor = true;
        public boolean coreHealthColor = true;
        public boolean weakpointTimer = true;
    }

    public static class Vesperidys {
        public boolean fightTimer = true;
    }
}
