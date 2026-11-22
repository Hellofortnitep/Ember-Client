package com.ember.client.mixin;

import com.ember.client.module.ModuleManager;
import com.ember.client.module.cosmetic.CrystalFx;
import com.ember.client.util.Colors;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalEntityRenderer.class)
public abstract class EndCrystalEntityRendererMixin {
    @Unique private static final String RENDER =
            "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V";
    @Unique private static RenderLayer emberLayer;

    @Unique private static boolean pushed;
    @Unique private static ModelPart lastPart;
    @Unique private static int partIndex;

    @Inject(method = RENDER, at = @At("HEAD"))
    private void ember$begin(EndCrystalEntity crystal, float yaw, float tickDelta, MatrixStack matrices,
                             VertexConsumerProvider buffers, int light, CallbackInfo ci) {
        CrystalFx fx = ModuleManager.get(CrystalFx.class);
        pushed = fx != null && fx.isEnabled();
        partIndex = 0;
        if (!pushed) return;
        matrices.push();
        float t = crystal.endCrystalAge + tickDelta;
        if (fx.bob.on()) matrices.translate(0, 0.25f * MathHelper.sin(t * 0.12f), 0);
        float s = fx.scale.f();
        matrices.scale(s, s, s);
    }

    /** Every crystal part (frames + core) gets its own shifting rainbow colour. */
    @Redirect(method = RENDER, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"),
            require = 0)
    private void ember$tint(ModelPart part, MatrixStack matrices, VertexConsumer vc, int light, int overlay) {
        lastPart = part;
        CrystalFx fx = ModuleManager.get(CrystalFx.class);
        if (pushed && fx.rainbow.on()) {
            part.render(matrices, vc, LightmapTextureManager.MAX_LIGHT_COORDINATE, overlay, Colors.rainbow(partIndex++ * 350L));
        } else {
            part.render(matrices, vc, light, overlay);
        }
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void ember$end(EndCrystalEntity crystal, float yaw, float tickDelta, MatrixStack matrices,
                           VertexConsumerProvider buffers, int light, CallbackInfo ci) {
        if (!pushed) return;
        pushed = false;
        CrystalFx fx = ModuleManager.get(CrystalFx.class);
        if (fx.orbit.on() && lastPart != null) {
            if (emberLayer == null) emberLayer = RenderLayer.getEntityCutoutNoCull(
                    Identifier.ofVanilla("textures/entity/end_crystal/end_crystal.png"));
            VertexConsumer vc = buffers.getBuffer(emberLayer);
            float t = crystal.endCrystalAge + tickDelta;
            int n = fx.orbitCount.i();
            for (int i = 0; i < n; i++) {
                matrices.push();
                matrices.translate(0, 1.1f + 0.2f * MathHelper.sin(t * 0.1f + i), 0);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(t * 4f + i * (360f / n)));
                matrices.translate(1.3f, 0, 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(t * 7f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(t * 5f));
                matrices.scale(0.35f, 0.35f, 0.35f);
                lastPart.render(matrices, vc, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV,
                        Colors.rainbow(i * 400L));
                matrices.pop();
            }
        }
        matrices.pop();
    }
}
