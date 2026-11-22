package com.ember.client.util;

public final class Colors {
    public static final int ACCENT = 0xFFFF7A2F;
    public static final int ACCENT_DARK = 0xFFB34A12;

    private Colors() {}

    /** Smoothly cycling rainbow, ARGB with full alpha. offsetMs shifts the hue. */
    public static int rainbow(long offsetMs) {
        float hue = ((System.currentTimeMillis() + offsetMs) % 3000L) / 3000f;
        return 0xFF000000 | hsv(hue, 0.65f, 1f);
    }

    public static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0xFFFFFF);
    }

    public static int hsv(float h, float s, float v) {
        float r, g, b;
        int i = (int) Math.floor(h * 6);
        float f = h * 6 - i, p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        switch (Math.floorMod(i, 6)) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
