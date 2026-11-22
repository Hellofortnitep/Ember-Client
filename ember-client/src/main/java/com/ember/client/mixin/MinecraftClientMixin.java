package com.ember.client.mixin;

import com.ember.client.EmberClient;
import com.ember.client.module.ModuleManager;
import com.ember.client.module.combat.Esp;
import com.ember.client.module.cosmetic.CrystalFx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "getWindowTitle", at = @At("RETURN"), cancellable = true)
    private void ember$title(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(EmberClient.NAME + " " + EmberClient.VERSION + " | " + cir.getReturnValue());
    }

    /** ESP and crystal glow outlines. */
    @Inject(method = "hasOutline", at = @At("RETURN"), cancellable = true)
    private void ember$outline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        Esp esp = ModuleManager.get(Esp.class);
        if (esp != null && esp.shouldGlow(entity)) { cir.setReturnValue(true); return; }
        CrystalFx fx = ModuleManager.get(CrystalFx.class);
        if (fx != null && fx.isEnabled() && fx.glow.on() && entity instanceof EndCrystalEntity) cir.setReturnValue(true);
    }
}
