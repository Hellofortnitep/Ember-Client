package com.ember.client.gui;

import com.ember.client.config.EmberConfig;
import com.ember.client.spotify.SpotifyService;
import com.ember.client.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.URI;

/** Where you paste your Spotify app's Client ID and log in. */
public class SpotifySetupScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget clientId;

    private static final String[] STEPS = {
            "1. Open the Spotify Developer Dashboard and click Create app.",
            "2. Add this Redirect URI:  " + SpotifyService.REDIRECT_URI,
            "3. Tick \"Web API\", save, then copy the Client ID into the box below.",
            "4. Click Connect and approve in your browser. Then play a song!",
    };

    public SpotifySetupScreen(Screen parent) {
        super(Text.literal("Connect Spotify"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = 40 + STEPS.length * 12 + 12;

        clientId = new TextFieldWidget(textRenderer, cx - 150, y, 300, 20, Text.literal("Client ID"));
        clientId.setMaxLength(64);
        clientId.setText(EmberConfig.spotifyClientId);
        clientId.setPlaceholder(Text.literal("Paste your Client ID here"));
        addDrawableChild(clientId);
        setInitialFocus(clientId);

        y += 28;
        addDrawableChild(ButtonWidget.builder(Text.literal("Open Dashboard"), b ->
                Util.getOperatingSystem().open(URI.create("https://developer.spotify.com/dashboard")))
                .dimensions(cx - 150, y, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Connect"), b -> {
            EmberConfig.spotifyClientId = clientId.getText().trim();
            EmberConfig.save();
            SpotifyService.connect();
        }).dimensions(cx - 48, y, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Disconnect"), b -> SpotifyService.disconnect())
                .dimensions(cx + 54, y, 96, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx - 50, y + 50, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "Connect Spotify", width / 2, 16, 0xFF1DB954);
        int y = 40;
        for (String s : STEPS) {
            ctx.drawTextWithShadow(textRenderer, s, width / 2 - 150, y, 0xFFDDDDDD);
            y += 12;
        }
        String status = "Status: " + SpotifyService.status()
                + (SpotifyService.isConnected() ? "  (logged in)" : "");
        ctx.drawCenteredTextWithShadow(textRenderer, status, width / 2, y + 66, Colors.ACCENT);
    }

    @Override
    public void close() {
        EmberConfig.spotifyClientId = clientId.getText().trim();
        EmberConfig.save();
        if (client != null) client.setScreen(parent);
    }
}
