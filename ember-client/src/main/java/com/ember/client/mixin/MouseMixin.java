package com.ember.client.mixin;

import com.ember.client.module.ModuleManager;
import com.ember.client.module.render.Zoom;
import com.ember.client.util.CpsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void ember$cps(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS && MinecraftClient.getInstance().currentScreen == null) CpsTracker.click(button);
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void ember$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        Zoom zoom = ModuleManager.get(Zoom.class);
        if (zoom != null && zoom.isZooming() && vertical != 0) {
            zoom.scroll(vertical);
            ci.cancel();
        }
    }
}
