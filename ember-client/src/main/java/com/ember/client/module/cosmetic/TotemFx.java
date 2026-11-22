package com.ember.client.module.cosmetic;

import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.setting.BoolSetting;
import com.ember.client.util.Colors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Rainbow totem pops, a screen flash, and pop counters for everyone around you. */
public class TotemFx extends Module {
    public final BoolSetting flash = add(new BoolSetting("Screen Flash", true));
    public final BoolSetting rainbowTotem = add(new BoolSetting("Rainbow Totem", true));
    public final BoolSetting alerts = add(new BoolSetting("Pop Alerts", true));

    private final Map<UUID, Integer> pops = new HashMap<>();
    private ClientWorld lastWorld;
    private long flashStart;
    private int lastSelfPops;

    public TotemFx() {
        super("TotemFX", Category.COSMETIC, "Rainbow totem pops, screen flash and pop counters.");
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc.world != lastWorld) {
            pops.clear();
            lastWorld = mc.world;
        }
    }

    /** Called from the network handler on entity status packets. */
    public void onStatus(Entity entity, byte status) {
        if (!isEnabled() || !(entity instanceof PlayerEntity player)) return;

        if (status == 35) { // totem pop
            int count = pops.merge(player.getUuid(), 1, Integer::sum);
            if (player == mc.player) {
                lastSelfPops = count;
                if (flash.on()) flashStart = System.currentTimeMillis();
            } else if (alerts.on()) {
                chat(Text.literal(player.getName().getString()).formatted(Formatting.WHITE)
                        .append(Text.literal(" popped ").formatted(Formatting.GRAY))
                        .append(Text.literal(count + (count == 1 ? " totem" : " totems")).formatted(Formatting.GOLD)));
            }
        } else if (status == 3) { // death
            Integer count = pops.remove(player.getUuid());
            if (count != null && alerts.on() && player != mc.player) {
                chat(Text.literal(player.getName().getString()).formatted(Formatting.WHITE)
                        .append(Text.literal(" died after popping ").formatted(Formatting.GRAY))
                        .append(Text.literal(String.valueOf(count)).formatted(Formatting.RED)));
            }
        }
    }

    private void chat(Text msg) {
        mc.inGameHud.getChatHud().addMessage(
                Text.literal("[Ember] ").styled(s -> s.withColor(Colors.ACCENT & 0xFFFFFF)).append(msg));
    }

    /** 1 right after a pop, fading to 0 over a second. */
    public float flashProgress() {
        if (!isEnabled()) return 0;
        long age = System.currentTimeMillis() - flashStart;
        return age > 1200 ? 0 : 1f - age / 1200f;
    }

    public int selfPops() { return lastSelfPops; }

    public boolean tintFloatingTotem() { return isEnabled() && rainbowTotem.on(); }
}
