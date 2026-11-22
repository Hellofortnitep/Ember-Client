package com.ember.client.module.render;

import com.ember.client.mixin.SimpleOptionAccessor;
import com.ember.client.module.Category;
import com.ember.client.module.Module;

/** Pushes gamma past the slider limit so caves and night are fully lit. */
public class Fullbright extends Module {
    private static final double BRIGHT = 16.0;
    private Double saved;

    public Fullbright() {
        super("Fullbright", Category.RENDER, "See in the dark.");
    }

    @Override
    public void onTick() {
        double current = mc.options.getGamma().getValue();
        if (current != BRIGHT) {
            if (saved == null) saved = current;
            set(BRIGHT);
        }
    }

    @Override
    public void onDisable() { restore(); }

    public void restore() {
        if (saved != null) {
            set(saved);
            saved = null;
        }
    }

    private void set(double v) {
        // setValue() rejects values above 1.0, so write the field directly.
        ((SimpleOptionAccessor) (Object) mc.options.getGamma()).setEmberValue(v);
    }
}
