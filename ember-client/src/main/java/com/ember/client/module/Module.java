package com.ember.client.module;

import com.ember.client.module.setting.Setting;
import com.ember.client.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting<?>> settings = new ArrayList<>();
    public int key = -1;

    /** Whether the on/off state is remembered between launches. */
    public boolean persistent = true;
    /** Whether it shows in the on-screen module list. */
    public boolean visible = true;

    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean enabled;

    protected Module(String name, Category category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

    protected <S extends Setting<?>> S add(S setting) {
        settings.add(setting);
        return setting;
    }

    public boolean isEnabled() { return enabled; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        try {
            if (value) onEnable(); else onDisable();
        } catch (Exception e) {
            com.ember.client.EmberClient.LOG.error("Module {} failed to toggle", name, e);
        }
    }

    /** Toggle and show a little message above the hotbar. */
    public void toggleWithMessage() {
        toggle();
        if (mc.inGameHud != null) {
            mc.inGameHud.setOverlayMessage(Text.literal(name + " ")
                    .styled(s -> s.withColor(Colors.ACCENT & 0xFFFFFF))
                    .append(Text.literal(enabled ? "ON" : "OFF")
                            .formatted(enabled ? Formatting.GREEN : Formatting.RED)), false);
        }
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
