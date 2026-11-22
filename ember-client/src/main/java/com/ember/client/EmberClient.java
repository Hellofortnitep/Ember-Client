package com.ember.client;

import com.ember.client.config.EmberConfig;
import com.ember.client.emote.Emotes;
import com.ember.client.gui.ClickGui;
import com.ember.client.gui.EmoteWheel;
import com.ember.client.module.ModuleManager;
import com.ember.client.module.render.Fullbright;
import com.ember.client.render.HudRenderer;
import com.ember.client.render.LyricsRenderer;
import com.ember.client.spotify.SpotifyService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmberClient implements ClientModInitializer {
    public static final String MOD_ID = "ember";
    public static final String NAME = "Ember Client";
    public static final String VERSION = "2.0.0";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;
    public static KeyBinding emoteKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        EmberConfig.load();

        menuKey = register("key.ember.menu", GLFW.GLFW_KEY_RIGHT_SHIFT);
        zoomKey = register("key.ember.zoom", GLFW.GLFW_KEY_C);
        emoteKey = register("key.ember.emotes", GLFW.GLFW_KEY_V);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        HudRenderCallback.EVENT.register(HudRenderer::render);
        WorldRenderEvents.LAST.register(LyricsRenderer::render);

        // Custom Ember totem texture, on by default (toggle it in Options > Resource Packs).
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container ->
                ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of(MOD_ID, "ember_totem"),
                        container, Text.literal("Ember Totem"), ResourcePackActivationType.DEFAULT_ENABLED));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ModuleManager.get(Fullbright.class).restore(); // keep options.txt clean
            EmberConfig.save();
            SpotifyService.stop();
        });

        LOG.info("{} {} loaded with {} modules", NAME, VERSION, ModuleManager.all().size());
    }

    private static KeyBinding register(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(id, InputUtil.Type.KEYSYM, key, "category.ember"));
    }

    private void onTick(MinecraftClient client) {
        while (menuKey.wasPressed()) {
            if (client.currentScreen == null) client.setScreen(new ClickGui());
        }
        while (emoteKey.wasPressed()) {
            if (client.currentScreen == null && client.player != null) client.setScreen(new EmoteWheel());
        }
        ModuleManager.tick();
        Emotes.tick(client);
    }
}
