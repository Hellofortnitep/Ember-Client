package com.ember.client.gui;

import com.ember.client.emote.Emote;
import com.ember.client.emote.Emotes;
import com.ember.client.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Radial emote picker (V by default). */
public class EmoteWheel extends Screen {
    private static final int RADIUS = 85, BW = 74, BH = 20;

    public EmoteWheel() {
        super(Text.literal("Emotes"));
    }

    @Override
    protected void init() {
        Emote[] emotes = Emote.values();
        int cx = width / 2, cy = height / 2;
        for (int i = 0; i < emotes.length; i++) {
            Emote e = emotes[i];
            double ang = -Math.PI / 2 + i * (Math.PI * 2 / emotes.length);
            int x = cx + (int) (Math.cos(ang) * RADIUS) - BW / 2;
            int y = cy + (int) (Math.sin(ang) * RADIUS) - BH / 2;
            addDrawableChild(ButtonWidget.builder(Text.literal(e.title), b -> {
                Emotes.play(e);
                close();
            }).dimensions(x, y, BW, BH).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Stop"), b -> {
            Emotes.stop();
            close();
        }).dimensions(cx - 25, cy - 10, 50, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "Emotes", width / 2, height / 2 - RADIUS - 30, Colors.rainbow(0));
        ctx.drawCenteredTextWithShadow(textRenderer, "Move or jump to stop. Only you can see emotes.",
                width / 2, height / 2 + RADIUS + 24, 0xFFAAAAAA);
    }

    @Override
    public boolean shouldPause() { return false; }
}
