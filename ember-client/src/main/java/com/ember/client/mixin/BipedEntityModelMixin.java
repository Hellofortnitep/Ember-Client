package com.ember.client.mixin;

import com.ember.client.emote.Emotes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies the active emote to your own player model. */
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void ember$emote(LivingEntity entity, float limbAngle, float limbDistance, float age,
                             float headYaw, float headPitch, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (entity != mc.player || !Emotes.active()) return;
        if (mc.options.getPerspective().isFirstPerson()) return; // don't twist the first-person arm
        Emotes.apply((BipedEntityModel<?>) (Object) this);
    }
}
