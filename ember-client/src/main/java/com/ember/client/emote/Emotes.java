package com.ember.client.emote;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.model.BipedEntityModel;

/** Plays one emote on your own player. Moving, jumping or going first person stops it. */
public final class Emotes {
    private static Emote current;
    private static long start;
    private static Perspective savedPerspective;

    private Emotes() {}

    public static boolean active() { return current != null; }
    public static Emote current() { return current; }

    public static void play(Emote e) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (current == null) savedPerspective = mc.options.getPerspective();
        current = e;
        start = System.currentTimeMillis();
        mc.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
    }

    public static void stop() {
        if (current == null) return;
        current = null;
        if (savedPerspective != null) MinecraftClient.getInstance().options.setPerspective(savedPerspective);
        savedPerspective = null;
    }

    public static void tick(MinecraftClient mc) {
        if (current == null) return;
        if (mc.player == null) { current = null; return; }
        var in = mc.player.input;
        boolean moving = in.movementForward != 0 || in.movementSideways != 0 || in.jumping || in.sneaking;
        long age = System.currentTimeMillis() - start;
        if (age > 400 && moving) { stop(); return; }
        if (mc.options.getPerspective().isFirstPerson()) { current = null; savedPerspective = null; }
    }

    public static void apply(BipedEntityModel<?> model) {
        if (current != null) current.apply(model, (System.currentTimeMillis() - start) / 1000f);
    }
}
