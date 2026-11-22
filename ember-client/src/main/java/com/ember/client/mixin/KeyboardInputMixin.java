package com.ember.client.mixin;

import com.ember.client.module.ModuleManager;
import com.ember.client.module.render.Freecam;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Stops your player walking around while the freecam is moving. */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void ember$freezeInFreecam(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        Freecam fc = ModuleManager.get(Freecam.class);
        if (fc == null || !fc.active()) return;
        Input in = (Input) (Object) this;
        in.movementForward = 0;
        in.movementSideways = 0;
        in.pressingForward = in.pressingBack = in.pressingLeft = in.pressingRight = false;
        in.jumping = false;
        in.sneaking = false;
    }
}
