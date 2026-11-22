package com.ember.client.module.movement;

import com.ember.client.module.Category;
import com.ember.client.module.Module;

/** Keeps the sprint key held. */
public class Sprint extends Module {
    public Sprint() {
        super("Sprint", Category.MOVEMENT, "Always sprint.");
    }

    @Override
    public void onTick() {
        mc.options.sprintKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        mc.options.sprintKey.setPressed(false);
    }
}
