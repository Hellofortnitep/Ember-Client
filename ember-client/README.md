# Ember Client 2.0

A Fabric client for Minecraft Java **1.21.1**.

## Controls
| Key | What it does |
|---|---|
| **Right Shift** | ClickGUI |
| **C** (hold) | Zoom. Scroll while holding to zoom more or less |
| **V** | Emote wheel |
| *(your choice)* | Any module. Open its settings in the ClickGUI and click **Bind** |

In the ClickGUI: **left-click** a module to toggle it, **right-click** it for its settings. Drag a panel by its header, and right-click the header to collapse it.

## Modules
**Combat:** AutoTotem (keeps a totem in your offhand), ESP (rainbow outlines on players and items through walls)
**Movement:** Fly, NoFall, Sprint
**Render:** HUD (watermark, module list, FPS, coords, ping, keystrokes, CPS, armor), Zoom, Fullbright, Freecam
**Cosmetic:** CrystalFX (rainbow 3D crystals with orbiting cores, bob and glow), TotemFX (rainbow pop animation, screen flash, pop counter and pop alerts)
**Music:** Spotify (now-playing widget and synced lyrics floating in the sky)

There's also a custom **Ember totem texture**. It's a built-in resource pack that's on by default. You can turn it off in Options → Resource Packs.

Emotes: 6 7, Wave, Dab, Floss, Clap, Dance, T-Pose and Facepalm. They switch you to third person, and moving cancels them. Only you can see them, because servers don't know about them.

> Fly, NoFall, ESP and AutoTotem are cheats. Only use them in singleplayer or on servers that allow them (anarchy servers). Normal servers' anticheats will ban you.

## Spotify lyrics setup (one time)
1. Go to https://developer.spotify.com/dashboard and click **Create app**.
2. Set the Redirect URI to `http://127.0.0.1:8888/callback`, tick **Web API**, and save.
3. Copy the app's **Client ID**.
4. In game, open the ClickGUI → Music → right-click **Spotify** → **Connect Spotify**. Paste the ID and click **Connect**.
5. Approve in your browser, then turn the Spotify module on and play a song.

Lyrics come from [LRCLIB](https://lrclib.net). Some songs don't have synced lyrics, and then the song title shows instead.

## Build
Needs **Java 21** (JDK).
```
./gradlew build          # Windows: gradlew.bat build
./gradlew runClient      # launch a test game with the mod
```
The jar goes to `build/libs/ember-client-2.0.0.jar`.

## Install
1. Install Fabric Loader for 1.21.1.
2. Put `ember-client-2.0.0.jar` and **Fabric API** (1.21.1) in `.minecraft/mods`.

Settings save to `.minecraft/config/ember-client.json`.

## Adding a module
1. Make a class in `module/<category>/` that extends `Module`. Add settings with `add(new BoolSetting(...))`.
2. Override `onTick()`, `onEnable()` and `onDisable()` as needed.
3. Register it in `ModuleManager.init()`. It shows up in the ClickGUI automatically.
