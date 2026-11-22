package com.ember.client.module;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    COSMETIC("Cosmetic"),
    MUSIC("Music");

    public final String title;

    Category(String title) { this.title = title; }
}
