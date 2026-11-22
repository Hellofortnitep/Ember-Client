package com.ember.client.module;

import com.ember.client.EmberClient;
import com.ember.client.module.combat.AutoTotem;
import com.ember.client.module.combat.Esp;
import com.ember.client.module.cosmetic.CrystalFx;
import com.ember.client.module.cosmetic.TotemFx;
import com.ember.client.module.movement.Fly;
import com.ember.client.module.movement.NoFall;
import com.ember.client.module.movement.Sprint;
import com.ember.client.module.music.Spotify;
import com.ember.client.module.render.Freecam;
import com.ember.client.module.render.Fullbright;
import com.ember.client.module.render.Hud;
import com.ember.client.module.render.Zoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    private static final Map<Class<?>, Module> BY_CLASS = new HashMap<>();

    private ModuleManager() {}

    public static void init() {
        // Combat
        add(new AutoTotem());
        add(new Esp());
        // Movement
        add(new Fly());
        add(new NoFall());
        add(new Sprint());
        // Render
        add(new Hud());
        add(new Zoom());
        add(new Fullbright());
        add(new Freecam());
        // Cosmetic
        add(new CrystalFx());
        add(new TotemFx());
        // Music
        add(new Spotify());
    }

    private static void add(Module m) {
        MODULES.add(m);
        BY_CLASS.put(m.getClass(), m);
    }

    public static List<Module> all() { return MODULES; }

    public static List<Module> in(Category category) {
        return MODULES.stream().filter(m -> m.category == category).toList();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> type) {
        return (T) BY_CLASS.get(type);
    }

    public static Module byName(String name) {
        for (Module m : MODULES) if (m.name.equalsIgnoreCase(name)) return m;
        return null;
    }

    public static void tick() {
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            try {
                m.onTick();
            } catch (Exception e) {
                EmberClient.LOG.error("Module {} crashed while ticking, turning it off", m.name, e);
                m.setEnabled(false);
            }
        }
    }

    public static void onKey(int key) {
        for (Module m : MODULES) if (m.key == key) m.toggleWithMessage();
    }
}
