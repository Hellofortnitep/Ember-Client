package com.ember.client.mixin;

import com.ember.client.module.ModuleManager;
import com.ember.client.module.cosmetic.TotemFx;
import com.ember.client.module.render.Zoom;
import com.ember.client.render.TintedVertexConsumer;
import com.ember.client.util.Colors;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void ember$zoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        Zoom zoom = ModuleManager.get(Zoom.class);
        if (zoom == null) return;
        double f = zoom.factor(tickDelta);
        if (f != 1.0) cir.setReturnValue(cir.getReturnValue() / f);
    }

    /** Rainbow tint on the big totem that pops up on screen. */
    @ModifyArg(method = "renderFloatingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;I)V"),
            index = 5, require = 0)
    private VertexConsumerProvider ember$rainbowTotem(VertexConsumerProvider original) {
        TotemFx fx = ModuleManager.get(TotemFx.class);
        if (fx == null || !fx.tintFloatingTotem()) return original;
        int color = Colors.rainbow(0);
        return layer -> new TintedVertexConsumer(original.getBuffer(layer), color);
    }
}
