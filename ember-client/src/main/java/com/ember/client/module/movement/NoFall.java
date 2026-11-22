package com.ember.client.module.movement;

import com.ember.client.module.Category;
import com.ember.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/** Tells the server you're on the ground while falling so you take no fall damage. */
public class NoFall extends Module {
    public NoFall() {
        super("NoFall", Category.MOVEMENT, "No fall damage.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.fallDistance > 2.5f && !mc.player.isFallFlying()) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
    }
}
