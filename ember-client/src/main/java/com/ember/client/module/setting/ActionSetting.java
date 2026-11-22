package com.ember.client.module.setting;

import com.google.gson.JsonElement;

/** A clickable button inside a module's settings. */
public class ActionSetting extends Setting<Runnable> {
    public ActionSetting(String name, Runnable action) { super(name, action); }

    public void run() { value.run(); }

    @Override public JsonElement save() { return null; }
    @Override public void load(JsonElement json) {}
}
