package com.ember.client.gui;

import com.ember.client.EmberClient;
import com.ember.client.config.EmberConfig;
import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.ModuleManager;
import com.ember.client.module.setting.ActionSetting;
import com.ember.client.module.setting.BoolSetting;
import com.ember.client.module.setting.NumberSetting;
import com.ember.client.module.setting.Setting;
import com.ember.client.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Meteor-style click GUI: one draggable panel per category.
 * Left-click a module to toggle it, right-click to open its settings.
 * Drag a panel by its header, right-click the header to collapse it.
 */
public class ClickGui extends Screen {
    private static final int W = 116, HEADER = 16, ROW = 14, SROW = 12;
    private static final Set<Module> EXPANDED = new HashSet<>();

    private enum Kind { HEADER, MODULE, SETTING, BIND }
    private record Row(Kind kind, Category cat, Module module, Setting<?> setting, int x, int y, int w, int h) {
        boolean hit(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private Category dragging;
    private int dragDx, dragDy;
    private Row sliding;
    private Module binding;

    public ClickGui() {
        super(Text.literal("Ember ClickGUI"));
    }

    // ---------- layout ----------

    private List<Row> layout() {
        List<Row> rows = new ArrayList<>();
        for (Category c : Category.values()) {
            int[] p = EmberConfig.panel(c);
            int x = p[0], y = p[1];
            rows.add(new Row(Kind.HEADER, c, null, null, x, y, W, HEADER));
            if (p[2] == 0) continue;
            y += HEADER;
            for (Module m : ModuleManager.in(c)) {
                rows.add(new Row(Kind.MODULE, c, m, null, x, y, W, ROW));
                y += ROW;
                if (EXPANDED.contains(m)) {
                    for (Setting<?> s : m.settings) {
                        rows.add(new Row(Kind.SETTING, c, m, s, x, y, W, SROW));
                        y += SROW;
                    }
                    rows.add(new Row(Kind.BIND, c, m, null, x, y, W, SROW));
                    y += SROW;
                }
            }
        }
        return rows;
    }

    // ---------- rendering ----------

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        Module hovered = null;
        for (Row r : layout()) {
            boolean hover = r.hit(mouseX, mouseY);
            switch (r.kind) {
                case HEADER -> drawHeader(ctx, r);
                case MODULE -> {
                    drawModule(ctx, r, hover);
                    if (hover) hovered = r.module;
                }
                case SETTING -> drawSetting(ctx, r, hover);
                case BIND -> drawBind(ctx, r, hover);
            }
        }

        // Footer: hint or module description.
        String hint = hovered != null ? hovered.description
                : "Left click: toggle   Right click: settings   Drag headers to move";
        ctx.drawCenteredTextWithShadow(textRenderer, hint, width / 2, height - 14, 0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer, EmberClient.NAME + " " + EmberClient.VERSION,
                width - textRenderer.getWidth(EmberClient.NAME + " " + EmberClient.VERSION) - 4, height - 14,
                Colors.rainbow(0));
    }

    private void drawHeader(DrawContext ctx, Row r) {
        int c1 = Colors.rainbow(r.cat.ordinal() * 400L);
        int c2 = Colors.rainbow(r.cat.ordinal() * 400L + 600);
        ctx.fillGradient(r.x, r.y, r.x + r.w, r.y + r.h, Colors.withAlpha(c1, 0xF0), Colors.withAlpha(c2, 0xF0));
        ctx.drawTextWithShadow(textRenderer, r.cat.title, r.x + 5, r.y + 4, 0xFFFFFFFF);
        boolean open = EmberConfig.panel(r.cat)[2] == 1;
        String arrow = open ? "-" : "+";
        ctx.drawTextWithShadow(textRenderer, arrow, r.x + r.w - 9, r.y + 4, 0xFFFFFFFF);
    }

    private void drawModule(DrawContext ctx, Row r, boolean hover) {
        boolean on = r.module.isEnabled();
        int accent = Colors.rainbow(r.y * 8L);
        int bg = on ? Colors.withAlpha(accent, hover ? 0x90 : 0x70) : (hover ? 0xE0262626 : 0xE0141414);
        ctx.fill(r.x, r.y, r.x + r.w, r.y + r.h, bg);
        if (on) ctx.fill(r.x, r.y, r.x + 2, r.y + r.h, accent);
        ctx.drawTextWithShadow(textRenderer, r.module.name, r.x + 6, r.y + 3, on ? 0xFFFFFFFF : 0xFFB0B0B0);
        String more = EXPANDED.contains(r.module) ? "v" : "...";
        ctx.drawTextWithShadow(textRenderer, more, r.x + r.w - textRenderer.getWidth(more) - 4, r.y + 3, 0xFF808080);
    }

    private void drawSetting(DrawContext ctx, Row r, boolean hover) {
        ctx.fill(r.x, r.y, r.x + r.w, r.y + r.h, hover ? 0xF01C1C1C : 0xF00A0A0A);
        ctx.fill(r.x, r.y, r.x + 1, r.y + r.h, Colors.ACCENT_DARK);
        int tx = r.x + 8, ty = r.y + 2;
        Setting<?> s = r.setting;
        if (s instanceof BoolSetting b) {
            ctx.drawTextWithShadow(textRenderer, b.name, tx, ty, 0xFFD0D0D0);
            int bx = r.x + r.w - 14;
            ctx.fill(bx, r.y + 2, bx + 8, r.y + 10, b.on() ? Colors.ACCENT : 0xFF3A3A3A);
        } else if (s instanceof NumberSetting n) {
            double frac = (n.get() - n.min) / (n.max - n.min);
            ctx.fill(r.x + 4, r.y + 1, r.x + 4 + (int) ((r.w - 8) * frac), r.y + r.h - 1,
                    Colors.withAlpha(Colors.ACCENT, 0x70));
            ctx.drawTextWithShadow(textRenderer, n.name, tx, ty, 0xFFD0D0D0);
            String v = n.display();
            ctx.drawTextWithShadow(textRenderer, v, r.x + r.w - textRenderer.getWidth(v) - 6, ty, 0xFFFFFFFF);
        } else if (s instanceof ActionSetting a) {
            ctx.drawTextWithShadow(textRenderer, "> " + a.name, tx, ty, hover ? 0xFFFFFFFF : Colors.ACCENT);
        }
    }

    private void drawBind(DrawContext ctx, Row r, boolean hover) {
        ctx.fill(r.x, r.y, r.x + r.w, r.y + r.h, hover ? 0xF01C1C1C : 0xF00A0A0A);
        ctx.fill(r.x, r.y, r.x + 1, r.y + r.h, Colors.ACCENT_DARK);
        String text = binding == r.module ? "Press a key... (Esc = none)" : "Bind: " + keyName(r.module.key);
        ctx.drawTextWithShadow(textRenderer, text, r.x + 8, r.y + 2, 0xFF909090);
    }

    private static String keyName(int key) {
        if (key < 0) return "None";
        return InputUtil.fromKeyCode(key, 0).getLocalizedText().getString();
    }

    // ---------- input ----------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        List<Row> rows = layout();
        // Topmost panel wins: iterate backwards.
        for (int i = rows.size() - 1; i >= 0; i--) {
            Row r = rows.get(i);
            if (!r.hit(mx, my)) continue;
            switch (r.kind) {
                case HEADER -> {
                    if (button == 0) {
                        dragging = r.cat;
                        dragDx = (int) mx - r.x;
                        dragDy = (int) my - r.y;
                    } else if (button == 1) {
                        int[] p = EmberConfig.panel(r.cat);
                        p[2] = p[2] == 1 ? 0 : 1;
                    }
                }
                case MODULE -> {
                    if (button == 0) r.module.toggle();
                    else if (button == 1 && !EXPANDED.remove(r.module)) EXPANDED.add(r.module);
                }
                case SETTING -> {
                    if (r.setting instanceof BoolSetting b) b.toggle();
                    else if (r.setting instanceof ActionSetting a) { EmberConfig.save(); a.run(); }
                    else if (r.setting instanceof NumberSetting) { sliding = r; slide(mx); }
                }
                case BIND -> binding = r.module;
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            int[] p = EmberConfig.panel(dragging);
            p[0] = MathHelper.clamp((int) mx - dragDx, 0, Math.max(0, width - W));
            p[1] = MathHelper.clamp((int) my - dragDy, 0, Math.max(0, height - HEADER));
            return true;
        }
        if (sliding != null) { slide(mx); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        sliding = null;
        return super.mouseReleased(mx, my, button);
    }

    private void slide(double mx) {
        if (!(sliding.setting instanceof NumberSetting n)) return;
        double frac = MathHelper.clamp((mx - sliding.x - 4) / (sliding.w - 8), 0, 1);
        n.set(n.min + frac * (n.max - n.min));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding != null) {
            boolean clear = keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE
                    || keyCode == GLFW.GLFW_KEY_BACKSPACE;
            binding.key = clear ? -1 : keyCode;
            binding = null;
            EmberConfig.save();
            return true;
        }
        if (EmberClient.menuKey.matchesKey(keyCode, scanCode)) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        EmberConfig.save();
        super.close();
    }

    @Override
    public boolean shouldPause() { return false; }
}
