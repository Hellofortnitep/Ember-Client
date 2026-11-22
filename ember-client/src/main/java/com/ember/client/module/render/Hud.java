package com.ember.client.module.render;

import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.setting.BoolSetting;

public class Hud extends Module {
    public final BoolSetting watermark = add(new BoolSetting("Watermark", true));
    public final BoolSetting moduleList = add(new BoolSetting("Module List", true));
    public final BoolSetting fps = add(new BoolSetting("FPS", true));
    public final BoolSetting coords = add(new BoolSetting("Coords", true));
    public final BoolSetting direction = add(new BoolSetting("Direction", true));
    public final BoolSetting ping = add(new BoolSetting("Ping", true));
    public final BoolSetting keystrokes = add(new BoolSetting("Keystrokes", true));
    public final BoolSetting cps = add(new BoolSetting("CPS", true));
    public final BoolSetting armor = add(new BoolSetting("Armor", true));
    public final BoolSetting rainbow = add(new BoolSetting("Rainbow", true));

    public Hud() {
        super("HUD", Category.RENDER, "On-screen info: watermark, module list, FPS, coords, keystrokes, armor.");
        visible = false;
        setEnabled(true);
    }
}
