package com.ember.client.module.music;

import com.ember.client.gui.SpotifySetupScreen;
import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.setting.ActionSetting;
import com.ember.client.module.setting.BoolSetting;
import com.ember.client.module.setting.NumberSetting;
import com.ember.client.spotify.SpotifyService;

/** Shows what you're playing on Spotify, with synced lyrics floating in the sky. */
public class Spotify extends Module {
    public final BoolSetting skyLyrics = add(new BoolSetting("Sky Lyrics", true));
    public final BoolSetting hudWidget = add(new BoolSetting("HUD Widget", true));
    public final BoolSetting rainbow = add(new BoolSetting("Rainbow", true));
    public final NumberSetting height = add(new NumberSetting("Height", 22, 0, 80, 1));
    public final NumberSetting distance = add(new NumberSetting("Distance", 45, 15, 120, 1));
    public final NumberSetting size = add(new NumberSetting("Size", 5, 1, 15, 0.5));
    public final BoolSetting follow = add(new BoolSetting("Follow View", true));
    public final ActionSetting setup = add(new ActionSetting("Connect Spotify",
            () -> mc.setScreen(new SpotifySetupScreen(mc.currentScreen))));

    public Spotify() {
        super("Spotify", Category.MUSIC, "Now playing + synced lyrics in the sky.");
    }

    @Override public void onEnable() { SpotifyService.start(); }
    @Override public void onDisable() { SpotifyService.stop(); }
}
