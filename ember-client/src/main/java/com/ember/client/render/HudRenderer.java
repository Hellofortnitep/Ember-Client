package com.ember.client.render;

import com.ember.client.EmberClient;
import com.ember.client.module.Module;
import com.ember.client.module.ModuleManager;
import com.ember.client.module.cosmetic.TotemFx;
import com.ember.client.module.music.Spotify;
import com.ember.client.module.render.Hud;
import com.ember.client.spotify.Lyrics;
import com.ember.client.spotify.SpotifyService;
import com.ember.client.util.Colors;
import com.ember.client.util.CpsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HudRenderer {
    private static final int PAD = 4;
    private static final int BG = 0x90000000;
    private static final int WHITE = 0xFFFFFFFF;

    private HudRenderer() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.options.hudHidden) return;

        TotemFx totem = ModuleManager.get(TotemFx.class);
        renderTotemFlash(ctx, mc, totem);

        if (mc.getDebugHud().shouldShowDebugHud()) return;

        Hud hud = ModuleManager.get(Hud.class);
        int leftY = PAD;
        if (hud.isEnabled()) {
            if (hud.watermark.on()) leftY = renderWatermark(ctx, mc.textRenderer, hud) + 2;
            leftY = renderInfo(ctx, mc, p, hud, leftY) + PAD;
            if (hud.moduleList.on()) renderModuleList(ctx, mc.textRenderer, hud);
            if (hud.keystrokes.on()) renderKeystrokes(ctx, mc, hud);
            if (hud.armor.on()) renderArmor(ctx, mc, p);
        }

        Spotify spotify = ModuleManager.get(Spotify.class);
        if (spotify.isEnabled() && spotify.hudWidget.on()) renderNowPlaying(ctx, mc.textRenderer, leftY);
    }

    private static int color(Hud hud, long offset) {
        return hud.rainbow.on() ? Colors.rainbow(offset) : Colors.ACCENT;
    }

    // ---------- watermark ----------

    private static int renderWatermark(DrawContext ctx, TextRenderer tr, Hud hud) {
        String name = "Ember";
        var m = ctx.getMatrices();
        m.push();
        m.translate(PAD, PAD, 0);
        m.scale(1.6f, 1.6f, 1f);
        int x = 0;
        for (int i = 0; i < name.length(); i++) {
            String c = String.valueOf(name.charAt(i));
            ctx.drawTextWithShadow(tr, c, x, 0, color(hud, -i * 120L));
            x += tr.getWidth(c);
        }
        m.pop();
        int endX = PAD + (int) (x * 1.6f) + 3;
        ctx.drawTextWithShadow(tr, "v" + EmberClient.VERSION, endX, PAD + 5, 0xFFAAAAAA);
        return PAD + (int) (tr.fontHeight * 1.6f);
    }

    // ---------- info lines ----------

    private static int renderInfo(DrawContext ctx, MinecraftClient mc, ClientPlayerEntity p, Hud hud, int top) {
        TextRenderer tr = mc.textRenderer;
        List<String[]> lines = new ArrayList<>();
        if (hud.fps.on()) lines.add(new String[]{"FPS", String.valueOf(mc.getCurrentFps())});
        if (hud.coords.on()) lines.add(new String[]{"XYZ", String.format("%.1f %.1f %.1f", p.getX(), p.getY(), p.getZ())});
        if (hud.direction.on()) lines.add(new String[]{"Facing", facing(p)});
        if (hud.ping.on() && mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
            if (e != null) lines.add(new String[]{"Ping", e.getLatency() + "ms"});
        }
        if (hud.cps.on() && !hud.keystrokes.on())
            lines.add(new String[]{"CPS", CpsTracker.left() + " | " + CpsTracker.right()});
        if (lines.isEmpty()) return top;

        int y = top;
        for (int i = 0; i < lines.size(); i++) {
            String[] l = lines.get(i);
            int x = ctx.drawTextWithShadow(tr, l[0] + " ", PAD, y, color(hud, -i * 150L));
            ctx.drawTextWithShadow(tr, l[1], x, y, WHITE);
            y += tr.fontHeight + 1;
        }
        return y;
    }

    private static String facing(ClientPlayerEntity p) {
        String dir = p.getHorizontalFacing().asString();
        dir = Character.toUpperCase(dir.charAt(0)) + dir.substring(1);
        String axis = switch (p.getHorizontalFacing()) {
            case NORTH -> "-Z";
            case SOUTH -> "+Z";
            case EAST -> "+X";
            case WEST -> "-X";
            default -> "";
        };
        return dir + " (" + axis + ")";
    }

    // ---------- module list (top right) ----------

    private static void renderModuleList(DrawContext ctx, TextRenderer tr, Hud hud) {
        List<Module> on = new ArrayList<>();
        for (Module m : ModuleManager.all()) if (m.isEnabled() && m.visible) on.add(m);
        on.sort(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed());

        int right = ctx.getScaledWindowWidth();
        int y = 2;
        for (int i = 0; i < on.size(); i++) {
            String name = on.get(i).name;
            int w = tr.getWidth(name);
            int c = color(hud, i * 180L);
            ctx.fill(right - w - 8, y, right, y + 11, 0x80000000);
            ctx.fill(right - 2, y, right, y + 11, c);
            ctx.drawTextWithShadow(tr, name, right - w - 5, y + 2, c);
            y += 11;
        }
    }

    // ---------- keystrokes (bottom right) ----------

    private static void renderKeystrokes(DrawContext ctx, MinecraftClient mc, Hud hud) {
        TextRenderer tr = mc.textRenderer;
        final int k = 22, gap = 2;
        int total = k * 3 + gap * 2;
        int left = ctx.getScaledWindowWidth() - total - PAD;
        int top = ctx.getScaledWindowHeight() - (k * 3 + gap * 3 + 12) - PAD;
        int accent = color(hud, 0);

        key(ctx, tr, mc.options.forwardKey, "W", left + k + gap, top, k, k, accent);
        int row2 = top + k + gap;
        key(ctx, tr, mc.options.leftKey, "A", left, row2, k, k, accent);
        key(ctx, tr, mc.options.backKey, "S", left + k + gap, row2, k, k, accent);
        key(ctx, tr, mc.options.rightKey, "D", left + (k + gap) * 2, row2, k, k, accent);

        int row3 = row2 + k + gap;
        int half = (total - gap) / 2;
        String lmb = hud.cps.on() ? CpsTracker.left() + " CPS" : "LMB";
        String rmb = hud.cps.on() ? CpsTracker.right() + " CPS" : "RMB";
        key(ctx, tr, mc.options.attackKey, lmb, left, row3, half, k, accent);
        key(ctx, tr, mc.options.useKey, rmb, left + half + gap, row3, half, k, accent);

        key(ctx, tr, mc.options.jumpKey, "———", left, row3 + k + gap, total, 12, accent);
    }

    private static void key(DrawContext ctx, TextRenderer tr, KeyBinding bind, String label,
                            int x, int y, int w, int h, int accent) {
        boolean down = bind.isPressed();
        ctx.fill(x, y, x + w, y + h, down ? Colors.withAlpha(accent, 0xC0) : BG);
        int tx = x + (w - tr.getWidth(label)) / 2;
        int ty = y + (h - tr.fontHeight) / 2 + 1;
        ctx.drawText(tr, label, tx, ty, down ? 0xFF000000 : WHITE, !down);
    }

    // ---------- armor (right of the hotbar) ----------

    private static void renderArmor(DrawContext ctx, MinecraftClient mc, ClientPlayerEntity p) {
        TextRenderer tr = mc.textRenderer;
        int x = ctx.getScaledWindowWidth() / 2 + 96;
        int y = ctx.getScaledWindowHeight() - 19;
        for (int slot = 3; slot >= 0; slot--) {
            ItemStack stack = p.getInventory().getArmorStack(slot);
            if (stack.isEmpty()) continue;
            ctx.drawItem(stack, x, y);
            ctx.drawItemInSlot(tr, stack, x, y);
            if (stack.isDamageable()) {
                int left = stack.getMaxDamage() - stack.getDamage();
                float pct = left / (float) stack.getMaxDamage();
                int c = pct > 0.5f ? 0xFF55FF55 : pct > 0.2f ? 0xFFFFFF55 : 0xFFFF5555;
                String s = Math.round(pct * 100) + "%";
                var m = ctx.getMatrices();
                m.push();
                m.translate(x + 8, y - 6, 200);
                m.scale(0.6f, 0.6f, 1f);
                ctx.drawTextWithShadow(tr, s, -tr.getWidth(s) / 2, 0, c);
                m.pop();
            }
            x += 18;
        }
    }

    // ---------- Spotify now playing ----------

    private static void renderNowPlaying(DrawContext ctx, TextRenderer tr, int top) {
        SpotifyService.Track t = SpotifyService.track();
        int x = PAD, w = 150;
        if (t == null) {
            String msg = "♫ " + SpotifyService.status();
            ctx.fill(x, top, x + tr.getWidth(msg) + 8, top + 14, BG);
            ctx.drawTextWithShadow(tr, msg, x + 4, top + 3, 0xFF1DB954);
            return;
        }
        String title = trim(tr, t.name(), w - 8);
        String artist = trim(tr, t.artist(), w - 8);
        ctx.fill(x, top, x + w, top + 34, BG);
        ctx.fill(x, top, x + 2, top + 34, 0xFF1DB954);
        ctx.drawTextWithShadow(tr, title, x + 6, top + 4, WHITE);
        ctx.drawTextWithShadow(tr, artist, x + 6, top + 14, 0xFFAAAAAA);

        long pos = SpotifyService.position();
        float pct = t.durationMs() > 0 ? Math.min(1f, pos / (float) t.durationMs()) : 0;
        ctx.fill(x + 6, top + 27, x + w - 6, top + 29, 0x60FFFFFF);
        ctx.fill(x + 6, top + 27, x + 6 + (int) ((w - 12) * pct), top + 29, 0xFF1DB954);

        Lyrics l = SpotifyService.lyrics();
        if (!l.lines.isEmpty()) {
            String line = l.lineAt(l.indexAt(pos));
            if (line != null && !line.isBlank()) {
                String s = trim(tr, line, 300);
                ctx.drawTextWithShadow(tr, s, x + 2, top + 38, Colors.rainbow(0));
            }
        }
    }

    private static String trim(TextRenderer tr, String s, int max) {
        if (tr.getWidth(s) <= max) return s;
        return tr.trimToWidth(s, max - tr.getWidth("...")) + "...";
    }

    // ---------- totem pop flash ----------

    private static void renderTotemFlash(DrawContext ctx, MinecraftClient mc, TotemFx totem) {
        float f = totem.flashProgress();
        if (f <= 0) return;
        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        int a = (int) (f * 0xB0);
        int c1 = Colors.withAlpha(Colors.rainbow(0), a);
        int c2 = Colors.withAlpha(Colors.rainbow(1500), a);
        int depth = 50;
        ctx.fillGradient(0, 0, w, depth, c1, 0x00000000);
        ctx.fillGradient(0, h - depth, w, h, 0x00000000, c2);
        ctx.fill(0, 0, 4, h, c1);
        ctx.fill(w - 4, 0, w, h, c2);

        TextRenderer tr = mc.textRenderer;
        String text = "TOTEM POPPED!  x" + totem.selfPops();
        var m = ctx.getMatrices();
        m.push();
        float scale = 2.2f + (1 - f) * 0.6f;
        m.translate(w / 2f, h / 3.2f, 0);
        m.scale(scale, scale, 1);
        int textA = Math.max(0x10, (int) (f * 255));
        int x = -tr.getWidth(text) / 2;
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            ctx.drawTextWithShadow(tr, ch, x, -4, Colors.withAlpha(Colors.rainbow(-i * 80L), textA));
            x += tr.getWidth(ch);
        }
        m.pop();
    }
}
