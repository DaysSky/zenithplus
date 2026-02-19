package com.dayssky.zenithplus.hud;

public class HudEntry {
    public final String id;
    public final String displayName;
    public int posX;
    public int posY;
    public float scale = 1.0f;
    public String text;
    public int color;
    public boolean visible = true;

    public HudEntry(String id, String displayName, int posX, int posY) {
        this.id = id;
        this.displayName = displayName;
        this.posX = posX;
        this.posY = posY;
        this.text = "";
        this.color = 0xFFFFFF;
    }

    public void set(String text, int color) {
        this.text = text;
        this.color = color;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void show() {
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }
}
