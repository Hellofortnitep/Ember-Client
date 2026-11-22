package com.ember.client.module.setting;

import com.google.gson.JsonElement;

public abstract class Setting<T> {
    public final String name;
    protected T value;

    protected Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public T get() { return value; }
    public void set(T value) { this.value = value; }

    /** Return null for settings that should not be saved. */
    public abstract JsonElement save();
    public abstract void load(JsonElement json);
}
