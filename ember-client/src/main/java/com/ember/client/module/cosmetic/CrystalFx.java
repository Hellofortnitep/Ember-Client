package com.ember.client.module.cosmetic;

import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.setting.BoolSetting;
import com.ember.client.module.setting.NumberSetting;

/** Makes end crystals look sick: rainbow, bigger, bobbing, orbiting mini-cores, glow. */
public class CrystalFx extends Module {
    public final NumberSetting scale = add(new NumberSetting("Scale", 1.0, 0.3, 3.0, 0.05));
    public final BoolSetting rainbow = add(new BoolSetting("Rainbow", true));
    public final BoolSetting orbit = add(new BoolSetting("Orbit Cores", true));
    public final NumberSetting orbitCount = add(new NumberSetting("Orbit Count", 3, 1, 8, 1));
    public final BoolSetting bob = add(new BoolSetting("Extra Bob", true));
    public final BoolSetting glow = add(new BoolSetting("Glow", true));

    public CrystalFx() {
        super("CrystalFX", Category.COSMETIC, "3D rainbow end crystals with orbiting cores.");
        setEnabled(true);
    }
}
