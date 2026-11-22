package com.ember.client.render;

import com.ember.client.module.ModuleManager;
import com.ember.client.module.music.Spotify;
import com.ember.client.spotify.Lyrics;
import com.ember.client.spotify.SpotifyService;
import com.ember.client.util.Colors;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Draws the current lyric line huge in the sky in front of you. */
public final class LyricsRenderer {
    private static float smoothYaw = Float.NaN;
    private static int lastIndex = -2;
    private static long lineChangedAt;

    private LyricsRenderer() {}

    public static void render(WorldRenderContext context) {
        Spotify mod = ModuleManager.get(Spotify.class);
        if (mod == null || !mod.isEnabled() || !mod.skyLyrics.on()) return;
        SpotifyService.Track track = SpotifyService.track();
        if (track == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        Camera cam = context.camera();

        // Work out which lines to show.
        Lyrics lyrics = SpotifyService.lyrics();
        int idx = lyrics.indexAt(SpotifyService.position());
        String prev, cur, next;
        if (lyrics.lines.isEmpty()) {
            prev = null;
            cur = lyrics.instrumental ? "♪ instrumental ♪" : "♫ " + track.name();
            next = lyrics.instrumental ? null : track.artist();
        } else {
            prev = lyrics.lineAt(idx - 1);
            cur = lyrics.lineAt(idx);
            next = lyrics.lineAt(idx + 1);
            if (cur == null || cur.isBlank()) cur = "♪";
        }
        if (idx != lastIndex) { lastIndex = idx; lineChangedAt = System.currentTimeMillis(); }
        float pop = Math.min(1f, (System.currentTimeMillis() - lineChangedAt) / 250f);

        // Position: ahead of where you're looking, up in the sky. Smoothly trails your view.
        float yaw = cam.getYaw();
        if (Float.isNaN(smoothYaw) || !mod.follow.on()) smoothYaw = yaw;
        else smoothYaw += MathHelper.wrapDegrees(yaw - smoothYaw) * 0.04f;

        double dist = mod.distance.get();
        double rad = Math.toRadians(smoothYaw);
        Vec3d camPos = cam.getPos();
        Vec3d offset = new Vec3d(-Math.sin(rad) * dist, mod.height.get(), Math.cos(rad) * dist);

        MatrixStack ms = new MatrixStack();
        // Some render paths already have the view rotation applied, some don't. Handle both.
        Matrix4f modelView = RenderSystem.getModelViewMatrix();
        if (modelView.equals(new Matrix4f(), 1e-4f)) ms.multiplyPositionMatrix(context.positionMatrix());

        ms.translate(offset.x, offset.y, offset.z);
        ms.multiply(cam.getRotation());
        float s = (float) (0.0028 * mod.size.get() * dist / 10.0);
        ms.scale(s, -s, s);

        VertexConsumerProvider.Immediate buffers = mc.getBufferBuilders().getEntityVertexConsumers();
        int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;

        if (prev != null && !prev.isBlank()) drawLine(tr, ms, buffers, prev, -22, 0.8f, 0x99AAAAAA, light);
        int curColor = mod.rainbow.on() ? Colors.rainbow(0) : 0xFFFFFFFF;
        drawLine(tr, ms, buffers, cur, 0, 1.6f * (0.85f + 0.15f * pop), curColor, light);
        if (next != null && !next.isBlank()) drawLine(tr, ms, buffers, next, 22, 0.8f, 0x99AAAAAA, light);

        // Song title above the lyrics.
        drawLine(tr, ms, buffers, "♫ " + track.name() + " — " + track.artist(), -44, 0.7f,
                Colors.withAlpha(Colors.ACCENT, 0xCC), light);

        buffers.draw();
    }

    private static void drawLine(TextRenderer tr, MatrixStack ms, VertexConsumerProvider buffers,
                                 String text, float y, float scale, int color, int light) {
        ms.push();
        ms.translate(0, y, 0);
        ms.scale(scale, scale, scale);
        float x = -tr.getWidth(text) / 2f;
        tr.draw(text, x, -tr.fontHeight / 2f, color, true, ms.peek().getPositionMatrix(), buffers,
                TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        ms.pop();
    }
}
