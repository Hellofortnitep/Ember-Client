package com.ember.client.config;

import com.ember.client.EmberClient;
import com.ember.client.module.Category;
import com.ember.client.module.Module;
import com.ember.client.module.ModuleManager;
import com.ember.client.module.setting.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/** Saves every module's state, key, and settings plus GUI layout to config/ember-client.json. */
public final class EmberConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("ember-client.json");

    /** Panel layout: x, y, open(1/0). */
    public static final Map<Category, int[]> PANELS = new EnumMap<>(Category.class);

    public static volatile String spotifyClientId = "";
    public static volatile String spotifyRefreshToken = "";

    private EmberConfig() {}

    public static int[] panel(Category c) {
        return PANELS.computeIfAbsent(c, k -> new int[]{10 + k.ordinal() * 122, 10, 1});
    }

    public static void load() {
        if (!Files.exists(FILE)) { save(); return; }
        try (Reader r = Files.newBufferedReader(FILE)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();

            if (root.has("modules")) {
                JsonObject mods = root.getAsJsonObject("modules");
                for (Module m : ModuleManager.all()) {
                    if (!mods.has(m.name)) continue;
                    JsonObject o = mods.getAsJsonObject(m.name);
                    if (o.has("key")) m.key = o.get("key").getAsInt();
                    if (o.has("settings")) {
                        JsonObject s = o.getAsJsonObject("settings");
                        for (Setting<?> setting : m.settings) {
                            if (s.has(setting.name)) {
                                try { setting.load(s.get(setting.name)); } catch (Exception ignored) {}
                            }
                        }
                    }
                    if (m.persistent && o.has("enabled")) m.setEnabled(o.get("enabled").getAsBoolean());
                }
            }

            if (root.has("panels")) {
                JsonObject p = root.getAsJsonObject("panels");
                for (Category c : Category.values()) {
                    if (!p.has(c.name())) continue;
                    JsonArray a = p.getAsJsonArray(c.name());
                    PANELS.put(c, new int[]{a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt()});
                }
            }

            if (root.has("spotify")) {
                JsonObject s = root.getAsJsonObject("spotify");
                if (s.has("clientId")) spotifyClientId = s.get("clientId").getAsString();
                if (s.has("refreshToken")) spotifyRefreshToken = s.get("refreshToken").getAsString();
            }
        } catch (Exception e) {
            EmberClient.LOG.warn("Could not read Ember config, using defaults", e);
        }
    }

    public static synchronized void save() {
        JsonObject root = new JsonObject();

        JsonObject mods = new JsonObject();
        for (Module m : ModuleManager.all()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.isEnabled());
            o.addProperty("key", m.key);
            JsonObject s = new JsonObject();
            for (Setting<?> setting : m.settings) {
                JsonElement e = setting.save();
                if (e != null) s.add(setting.name, e);
            }
            o.add("settings", s);
            mods.add(m.name, o);
        }
        root.add("modules", mods);

        JsonObject panels = new JsonObject();
        for (Category c : Category.values()) {
            int[] v = panel(c);
            JsonArray a = new JsonArray();
            for (int i : v) a.add(i);
            panels.add(c.name(), a);
        }
        root.add("panels", panels);

        JsonObject spotify = new JsonObject();
        spotify.addProperty("clientId", spotifyClientId);
        spotify.addProperty("refreshToken", spotifyRefreshToken);
        root.add("spotify", spotify);

        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) { GSON.toJson(root, w); }
        } catch (Exception e) {
            EmberClient.LOG.warn("Could not save Ember config", e);
        }
    }
}
