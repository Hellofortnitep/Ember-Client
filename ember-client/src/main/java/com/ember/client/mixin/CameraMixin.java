package com.ember.client.mixin;

import com.ember.client.module.ModuleManager;
import com.ember.client.module.render.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPos(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void ember$freecam(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView,
                               float tickDelta, CallbackInfo ci) {
        Freecam fc = ModuleManager.get(Freecam.class);
        if (fc == null || !fc.active()) return;
        Vec3d p = fc.pos(tickDelta);
        setRotation(fc.yaw, fc.pitch);
        setPos(p.x, p.y, p.z);
    }

    /** Makes the game draw your own body while in freecam. */
    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    private void ember$thirdPerson(CallbackInfoReturnable<Boolean> cir) {
        Freecam fc = ModuleManager.get(Freecam.class);
        if (fc != null && fc.active()) cir.setReturnValue(true);
    }
}
