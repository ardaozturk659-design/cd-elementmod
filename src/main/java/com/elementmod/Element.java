package com.elementmod;

import net.minecraft.util.text.TextFormatting;

public enum Element {
    WATER("water", "su", "Su", 0.10f, 0.40f, 1.00f, TextFormatting.BLUE),            // mavi
    FIRE("fire", "ates", "Ateş", 1.00f, 0.35f, 0.00f, TextFormatting.RED),            // kırmızı-turuncu
    EARTH("earth", "toprak", "Toprak", 0.10f, 0.85f, 0.10f, TextFormatting.GREEN),    // yeşil
    ELECTRIC("electric", "elektrik", "Elektrik", 0.40f, 0.90f, 1.00f, TextFormatting.AQUA), // açık mavi
    LIGHT("light", "isik", "Işık", 1.00f, 0.90f, 0.10f, TextFormatting.YELLOW),       // sarı
    DARK("dark", "karanlik", "Karanlık", 0.001f, 0.001f, 0.001f, TextFormatting.DARK_GRAY); // siyah

    public final String id, tr, title;
    public final float r, g, b;
    public final TextFormatting fmt;

    Element(String id, String tr, String title, float r, float g, float b, TextFormatting fmt) {
        this.id = id; this.tr = tr; this.title = title;
        this.r = r; this.g = g; this.b = b; this.fmt = fmt;
    }

    public static Element byId(String s) {
        for (Element e : values()) {
            if (e.id.equalsIgnoreCase(s) || e.tr.equalsIgnoreCase(s)) return e;
        }
        return null;
    }
}
