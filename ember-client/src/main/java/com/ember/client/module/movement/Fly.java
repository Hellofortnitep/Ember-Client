package com.ember.client.module.movement;

import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.setting.NumberSetting;

/** Creative-style flight. Vanilla servers kick for this unless allow-flight is on. */
public class Fly extends Module {
    public final NumberSetting speed = add(new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1));

    public Fly() {
        super("Fly", Category.MOVEMENT, "Fly like in creative.");
        persistent = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        var abilities = mc.player.getAbilities();
        abilities.allowFlying = true;
        abilities.flying = true;
        abilities.setFlySpeed(0.05f * speed.f());
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        var abilities = mc.player.getAbilities();
        abilities.setFlySpeed(0.05f);
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            abilities.flying = false;
            abilities.allowFlying = false;
        }
    }
}
