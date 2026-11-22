package com.ember.client.module.render;

import com.ember.client.EmberClient;
import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.setting.BoolSetting;
import com.ember.client.module.setting.NumberSetting;
import net.minecraft.util.math.MathHelper;

/** Hold the Zoom key (C by default) to zoom. Scroll while zooming to change the amount. */
public class Zoom extends Module {
    public final NumberSetting level = add(new NumberSetting("Level", 4, 2, 15, 0.5));
    public final BoolSetting smooth = add(new BoolSetting("Smooth", true));
    public final BoolSetting cinematic = add(new BoolSetting("Cinematic", true));

    private double target = 4, prevFactor = 1, factor = 1;
    private boolean zooming;
    private Boolean savedSmoothCam;

    public Zoom() {
        super("Zoom", Category.RENDER, "Hold C to zoom, scroll to adjust.");
        visible = false;
        setEnabled(true);
    }

    public boolean isZooming() { return isEnabled() && zooming; }

    @Override
    public void onTick() {
        boolean want = EmberClient.zoomKey.isPressed() && mc.currentScreen == null;
        if (want && !zooming) {
            target = level.get();
            if (cinematic.on()) {
                savedSmoothCam = mc.options.smoothCameraEnabled;
                mc.options.smoothCameraEnabled = true;
            }
        } else if (!want && zooming && savedSmoothCam != null) {
            mc.options.smoothCameraEnabled = savedSmoothCam;
            savedSmoothCam = null;
        }
        zooming = want;

        double goal = zooming ? target : 1.0;
        prevFactor = factor;
        factor = smooth.on() ? factor + (goal - factor) * 0.5 : goal;
    }

    @Override
    public void onDisable() {
        zooming = false;
        factor = prevFactor = 1;
        if (savedSmoothCam != null) mc.options.smoothCameraEnabled = savedSmoothCam;
        savedSmoothCam = null;
    }

    public void scroll(double amount) {
        target = MathHelper.clamp(target * (amount > 0 ? 1.25 : 0.8), 1.5, 50);
    }

    /** Vanilla FOV is divided by this. */
    public double factor(float tickDelta) {
        return isEnabled() ? MathHelper.lerp(tickDelta, prevFactor, factor) : 1.0;
    }
}
