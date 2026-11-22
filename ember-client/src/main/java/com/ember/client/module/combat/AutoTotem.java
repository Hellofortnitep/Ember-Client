package com.ember.client.module.combat;

import com.ember.client.module.Category;
import com.ember.client.module.Module;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Keeps a totem in your offhand. */
public class AutoTotem extends Module {
    private int cooldown;

    public AutoTotem() {
        super("AutoTotem", Category.COMBAT, "Moves a totem into your offhand whenever it's empty.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (cooldown > 0) { cooldown--; return; }
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) return;

        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (!inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) continue;
            int slot = i < 9 ? 36 + i : i; // inventory index -> player screen slot id
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, 40,
                    SlotActionType.SWAP, mc.player);
            cooldown = 3;
            return;
        }
    }
}
