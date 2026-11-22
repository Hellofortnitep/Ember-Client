package com.ember.client.render;

import net.minecraft.client.render.VertexConsumer;

/** Wraps a VertexConsumer and multiplies every vertex colour by a tint. */
public class TintedVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final int tr, tg, tb;

    public TintedVertexConsumer(VertexConsumer delegate, int argb) {
        this.delegate = delegate;
        this.tr = (argb >> 16) & 0xFF;
        this.tg = (argb >> 8) & 0xFF;
        this.tb = argb & 0xFF;
    }

    @Override public VertexConsumer vertex(float x, float y, float z) { delegate.vertex(x, y, z); return this; }
    @Override public VertexConsumer color(int r, int g, int b, int a) {
        delegate.color(r * tr / 255, g * tg / 255, b * tb / 255, a); return this;
    }
    @Override public VertexConsumer texture(float u, float v) { delegate.texture(u, v); return this; }
    @Override public VertexConsumer overlay(int u, int v) { delegate.overlay(u, v); return this; }
    @Override public VertexConsumer light(int u, int v) { delegate.light(u, v); return this; }
    @Override public VertexConsumer normal(float x, float y, float z) { delegate.normal(x, y, z); return this; }
}
