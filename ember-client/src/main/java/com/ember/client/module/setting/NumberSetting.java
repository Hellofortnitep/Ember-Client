package com.ember.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.math.MathHelper;

public class NumberSetting extends Setting<Double> {
    public final double min, max, step;

    public NumberSetting(String name, double value, double min, double max, double step) {
        super(name, value);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void set(Double v) {
        double clamped = MathHelper.clamp(v, min, max);
        value = Math.round(clamped / step) * step;
    }

    public float f() { return value.floatValue(); }
    public int i() { return (int) Math.round(value); }

    public String display() {
        return step >= 1 ? String.valueOf(i()) : String.format("%.2f", value);
    }

    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement json) { set(json.getAsDouble()); }
}
