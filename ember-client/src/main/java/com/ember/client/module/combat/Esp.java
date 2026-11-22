package com.ember.client.module.combat;

import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.setting.BoolSetting;
import com.ember.client.util.Colors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

/** Glowing outlines through walls. */
public class Esp extends Module {
    public final BoolSetting players = add(new BoolSetting("Players", true));
    public final BoolSetting items = add(new BoolSetting("Items", false));
    public final BoolSetting rainbow = add(new BoolSetting("Rainbow", true));

    public Esp() {
        super("ESP", Category.COMBAT, "Outlines players (and items) through walls.");
    }

    public boolean shouldGlow(Entity e) {
        if (!isEnabled() || e == mc.player) return false;
        return (players.on() && e instanceof PlayerEntity) || (items.on() && e instanceof ItemEntity);
    }

    public int color(Entity e) {
        if (rainbow.on()) return Colors.rainbow(e.getId() * 150L) & 0xFFFFFF;
        return e instanceof PlayerEntity ? 0xFF3B3B : 0xFFD23F;
    }
}
