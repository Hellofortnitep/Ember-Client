package com.ember.client.mixin;

import com.ember.client.module.ModuleManager;
import com.ember.client.module.combat.Esp;
import com.ember.client.module.cosmetic.CrystalFx;
import com.ember.client.module.render.Freecam;
import com.ember.client.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /** In freecam, the mouse turns the camera instead of your player. */
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void ember$freecamLook(double dx, double dy, CallbackInfo ci) {
        if ((Object) this != MinecraftClient.getInstance().player) return;
        Freecam fc = ModuleManager.get(Freecam.class);
        if (fc != null && fc.active()) {
            fc.look(dx, dy);
            ci.cancel();
        }
    }

    /** Outline colour for ESP / glowing crystals. */
    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void ember$outlineColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        Esp esp = ModuleManager.get(Esp.class);
        if (esp != null && esp.shouldGlow(self)) {
            cir.setReturnValue(esp.color(self));
            return;
        }
        CrystalFx fx = ModuleManager.get(CrystalFx.class);
        if (fx != null && fx.isEnabled() && fx.glow.on() && self instanceof EndCrystalEntity) {
            cir.setReturnValue(Colors.rainbow(self.getId() * 200L) & 0xFFFFFF);
        }
    }
}
