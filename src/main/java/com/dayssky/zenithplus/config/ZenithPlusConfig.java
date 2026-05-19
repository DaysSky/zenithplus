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


    public static class Clear {
        @ConfigEntry.Gui.CollapsibleObject
        public GravityBomb gravityBomb = new GravityBomb();

        @ConfigEntry.Gui.CollapsibleObject
        public SidearmOneShot sidearmOneShot = new SidearmOneShot();

        public static class GravityBomb {
            public boolean enabled = true;

            @ConfigEntry.ColorPicker
            public int color = 0x5555FF;
        }

        public static class SidearmOneShot {
            public boolean enabled = true;
            public boolean damageHud = false;

            @ConfigEntry.ColorPicker
            public int color = 0xFFAA00;
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

        @ConfigEntry.Gui.CollapsibleObject
        public MagusDisplay magusDisplay = new MagusDisplay();

        @ConfigEntry.Gui.CollapsibleObject
        public CrystalDisplay crystalDisplay = new CrystalDisplay();

        @ConfigEntry.Gui.CollapsibleObject
        public TpHighlight tpHighlight = new TpHighlight();

        public static class MagusDisplay {
            public boolean enabled = true;

            @ConfigEntry.ColorPicker
            public int glowColor = 0x00FFFF;

        }

        public static class CrystalDisplay {
            public boolean enabled = true;

            @ConfigEntry.ColorPicker
            public int glowColor = 0x00FFFF;

        }

        public static class TpHighlight {
            public boolean enabled = true;

            @ConfigEntry.ColorPicker
            public int color = 0xFF5555;

            public float size = 0.5F;
        }
    }
}
