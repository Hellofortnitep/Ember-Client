package com.ember.client.module.render;

import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.setting.NumberSetting;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Detach the camera and fly it around. Your player stays put. */
public class Freecam extends Module {
    public final NumberSetting speed = add(new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1));

    private Vec3d pos = Vec3d.ZERO, prevPos = Vec3d.ZERO;
    public float yaw, pitch;

    public Freecam() {
        super("Freecam", Category.RENDER, "Fly the camera around freely. Your body stays where it is.");
        persistent = false;
    }

    public boolean active() { return isEnabled() && mc.player != null; }

    @Override
    public void onEnable() {
        if (mc.player == null) { setEnabled(false); return; }
        pos = prevPos = mc.gameRenderer.getCamera().getPos();
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        mc.chunkCullingEnabled = false;
    }

    @Override
    public void onDisable() {
        mc.chunkCullingEnabled = true;
    }

    @Override
    public void onTick() {
        if (mc.player == null) { setEnabled(false); return; }
        prevPos = pos;
        if (mc.currentScreen != null) return;

        GameOptions o = mc.options;
        double fwd = (o.forwardKey.isPressed() ? 1 : 0) - (o.backKey.isPressed() ? 1 : 0);
        double side = (o.leftKey.isPressed() ? 1 : 0) - (o.rightKey.isPressed() ? 1 : 0);
        double up = (o.jumpKey.isPressed() ? 1 : 0) - (o.sneakKey.isPressed() ? 1 : 0);

        double rad = Math.toRadians(yaw);
        double sin = MathHelper.sin((float) rad), cos = MathHelper.cos((float) rad);
        double dx = -sin * fwd + cos * side;
        double dz = cos * fwd + sin * side;
        Vec3d move = new Vec3d(dx, up, dz);
        if (move.lengthSquared() > 0) {
            double s = speed.get() * (o.sprintKey.isPressed() ? 2.0 : 1.0);
            pos = pos.add(move.normalize().multiply(s));
        }
    }

    public void look(double dx, double dy) {
        yaw += (float) (dx * 0.15);
        pitch = MathHelper.clamp(pitch + (float) (dy * 0.15), -90f, 90f);
    }

    public Vec3d pos(float tickDelta) {
        return prevPos.lerp(pos, tickDelta);
    }
}
