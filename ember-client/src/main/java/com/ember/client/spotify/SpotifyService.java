package com.ember.client.spotify;

import com.ember.client.EmberClient;
import com.ember.client.config.EmberConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Talks to Spotify (what's playing) and LRCLIB (synced lyrics).
 * Login uses the PKCE flow, so only a Client ID is needed (no secret).
 */
public final class SpotifyService {
    public static final int PORT = 8888;
    public static final String REDIRECT_URI = "http://127.0.0.1:" + PORT + "/callback";
    private static final String SCOPES = "user-read-currently-playing user-read-playback-state";
    private static final String USER_AGENT = "EmberClient/2.0 (Minecraft mod)";

    public record Track(String id, String name, String artist, String album, long durationMs) {}

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static ScheduledExecutorService exec;
    private static ServerSocket callbackServer;
    private static String verifier;

    private static volatile String accessToken;
    private static volatile long accessExpiry;
    private static volatile String status = "Not connected";

    private static volatile Track track;
    private static volatile boolean playing;
    private static volatile long progressMs, progressAt;
    private static volatile Lyrics lyrics = Lyrics.NONE;
    private static volatile String lyricsFor;

    private SpotifyService() {}

    // ---------- public state ----------

    public static String status() { return status; }
    public static Track track() { return track; }
    public static boolean isPlaying() { return playing; }
    public static Lyrics lyrics() { return lyrics; }
    public static boolean isConnected() { return !EmberConfig.spotifyRefreshToken.isBlank(); }

    /** Estimated playback position, smoothed between polls. */
    public static long position() {
        long p = progressMs + (playing ? System.currentTimeMillis() - progressAt : 0);
        Track t = track;
        return t == null ? p : Math.min(p, t.durationMs());
    }

    // ---------- polling ----------

    public static synchronized void start() {
        if (exec != null) return;
        exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Ember-Spotify");
            t.setDaemon(true);
            return t;
        });
        exec.scheduleWithFixedDelay(SpotifyService::poll, 0, 2, TimeUnit.SECONDS);
    }

    public static synchronized void stop() {
        if (exec != null) exec.shutdownNow();
        exec = null;
        track = null;
        playing = false;
    }

    private static void poll() {
        try {
            if (!isConnected()) { status = "Not connected"; track = null; return; }
            ensureToken();

            HttpResponse<String> res = HTTP.send(HttpRequest.newBuilder(
                            URI.create("https://api.spotify.com/v1/me/player/currently-playing"))
                    .header("Authorization", "Bearer " + accessToken)
                    .timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 401) { accessToken = null; return; }
            if (res.statusCode() == 204 || res.body().isBlank()) {
                track = null; playing = false; status = "Nothing playing";
                return;
            }
            if (res.statusCode() != 200) { status = "Spotify error " + res.statusCode(); return; }

            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            JsonElement itemEl = root.get("item");
            if (itemEl == null || itemEl.isJsonNull()) { track = null; status = "Playing (no track info)"; return; }
            JsonObject item = itemEl.getAsJsonObject();

            List<String> artists = new ArrayList<>();
            if (item.has("artists")) {
                for (JsonElement a : item.getAsJsonArray("artists")) artists.add(a.getAsJsonObject().get("name").getAsString());
            }
            String album = item.has("album") ? item.getAsJsonObject("album").get("name").getAsString() : "";
            Track t = new Track(str(item, "id"), str(item, "name"), String.join(", ", artists), album,
                    item.get("duration_ms").getAsLong());

            progressMs = root.has("progress_ms") && !root.get("progress_ms").isJsonNull() ? root.get("progress_ms").getAsLong() : 0;
            progressAt = System.currentTimeMillis();
            playing = root.has("is_playing") && root.get("is_playing").getAsBoolean();
            track = t;
            status = playing ? "Playing" : "Paused";

            if (!t.id().equals(lyricsFor)) {
                lyricsFor = t.id();
                lyrics = Lyrics.NONE;
                lyrics = fetchLyrics(t, artists.isEmpty() ? "" : artists.get(0));
            }
        } catch (Exception e) {
            status = "Error: " + e.getMessage();
        }
    }

    private static Lyrics fetchLyrics(Track t, String mainArtist) {
        try {
            Map<String, String> q = new LinkedHashMap<>();
            q.put("track_name", t.name());
            q.put("artist_name", mainArtist);
            q.put("album_name", t.album());
            q.put("duration", String.valueOf(Math.round(t.durationMs() / 1000.0)));
            HttpResponse<String> res = HTTP.send(HttpRequest.newBuilder(
                            URI.create("https://lrclib.net/api/get?" + form(q)))
                    .header("User-Agent", USER_AGENT).timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 404) {
                // Fall back to a looser search.
                Map<String, String> s = new LinkedHashMap<>();
                s.put("track_name", t.name());
                s.put("artist_name", mainArtist);
                res = HTTP.send(HttpRequest.newBuilder(URI.create("https://lrclib.net/api/search?" + form(s)))
                        .header("User-Agent", USER_AGENT).timeout(Duration.ofSeconds(10)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() != 200) return Lyrics.NONE;
                JsonArray arr = JsonParser.parseString(res.body()).getAsJsonArray();
                for (JsonElement el : arr) {
                    Lyrics l = fromLrclib(el.getAsJsonObject());
                    if (l != Lyrics.NONE) return l;
                }
                return Lyrics.NONE;
            }
            if (res.statusCode() != 200) return Lyrics.NONE;
            return fromLrclib(JsonParser.parseString(res.body()).getAsJsonObject());
        } catch (Exception e) {
            EmberClient.LOG.warn("Lyrics lookup failed", e);
            return Lyrics.NONE;
        }
    }

    private static Lyrics fromLrclib(JsonObject o) {
        if (o.has("instrumental") && o.get("instrumental").getAsBoolean()) return Lyrics.instrumental();
        JsonElement synced = o.get("syncedLyrics");
        if (synced == null || synced.isJsonNull()) return Lyrics.NONE;
        return Lyrics.parse(synced.getAsString());
    }

    // ---------- auth (PKCE) ----------

    /** Starts the login: opens the browser and waits for Spotify to redirect back. */
    public static synchronized void connect() {
        String clientId = EmberConfig.spotifyClientId.trim();
        if (clientId.isEmpty()) { status = "Enter your Client ID first"; return; }
        try {
            closeCallbackServer();

            byte[] bytes = new byte[48];
            new SecureRandom().nextBytes(bytes);
            verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));

            ServerSocket server = new ServerSocket(PORT, 5, InetAddress.getByName("127.0.0.1"));
            server.setSoTimeout(5 * 60 * 1000); // give up after 5 minutes
            callbackServer = server;
            Thread t = new Thread(() -> waitForCallback(server, clientId), "Ember-Spotify-Login");
            t.setDaemon(true);
            t.start();

            Map<String, String> q = new LinkedHashMap<>();
            q.put("client_id", clientId);
            q.put("response_type", "code");
            q.put("redirect_uri", REDIRECT_URI);
            q.put("code_challenge_method", "S256");
            q.put("code_challenge", challenge);
            q.put("scope", SCOPES);
            Util.getOperatingSystem().open(URI.create("https://accounts.spotify.com/authorize?" + form(q)));
            status = "Waiting for you to approve in the browser...";
        } catch (Exception e) {
            status = "Couldn't start login: " + e.getMessage();
            EmberClient.LOG.error("Spotify login failed", e);
        }
    }

    /** Tiny one-shot HTTP listener for Spotify's redirect back to us. */
    private static void waitForCallback(ServerSocket server, String clientId) {
        try (server) {
            while (true) {
                try (Socket sock = server.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
                    String requestLine = in.readLine(); // e.g. GET /callback?code=... HTTP/1.1
                    if (requestLine == null) continue;
                    String[] parts = requestLine.split(" ");
                    String target = parts.length > 1 ? parts[1] : "/";
                    if (!target.startsWith("/callback")) { respond(sock, 404, "Not found"); continue; }

                    int qi = target.indexOf('?');
                    Map<String, String> params = parseQuery(qi >= 0 ? target.substring(qi + 1) : null);
                    String page;
                    if (params.containsKey("code")) {
                        boolean ok = exchangeCode(params.get("code"), clientId);
                        page = ok ? "<h2>Ember Client is connected to Spotify!</h2><p>You can close this tab and go back to Minecraft.</p>"
                                  : "<h2>Login failed.</h2><p>" + status + "</p>";
                    } else {
                        status = "Login cancelled";
                        page = "<h2>Login cancelled.</h2>";
                    }
                    respond(sock, 200, "<html><body style='font-family:sans-serif;background:#111;color:#ff7a2f;"
                            + "text-align:center;padding-top:80px'>" + page + "</body></html>");
                    return;
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            status = "Login timed out, try again";
        } catch (Exception e) {
            if (callbackServer == server) status = "Login error: " + e.getMessage();
        } finally {
            if (callbackServer == server) callbackServer = null;
        }
    }

    private static void respond(Socket sock, int code, String html) throws java.io.IOException {
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        String head = "HTTP/1.1 " + code + (code == 200 ? " OK" : " Not Found") + "\r\n"
                + "Content-Type: text/html; charset=utf-8\r\nContent-Length: " + body.length
                + "\r\nConnection: close\r\n\r\n";
        OutputStream out = sock.getOutputStream();
        out.write(head.getBytes(StandardCharsets.US_ASCII));
        out.write(body);
        out.flush();
    }

    private static void closeCallbackServer() {
        try { if (callbackServer != null) callbackServer.close(); } catch (Exception ignored) {}
        callbackServer = null;
    }

    public static void disconnect() {
        EmberConfig.spotifyRefreshToken = "";
        accessToken = null;
        track = null;
        status = "Not connected";
        EmberConfig.save();
    }

    private static boolean exchangeCode(String code, String clientId) {
        Map<String, String> f = new LinkedHashMap<>();
        f.put("grant_type", "authorization_code");
        f.put("code", code);
        f.put("redirect_uri", REDIRECT_URI);
        f.put("client_id", clientId);
        f.put("code_verifier", verifier);
        boolean ok = tokenRequest(f);
        if (ok) status = "Connected!";
        return ok;
    }

    private static void ensureToken() {
        if (accessToken != null && System.currentTimeMillis() < accessExpiry - 30_000) return;
        Map<String, String> f = new LinkedHashMap<>();
        f.put("grant_type", "refresh_token");
        f.put("refresh_token", EmberConfig.spotifyRefreshToken);
        f.put("client_id", EmberConfig.spotifyClientId.trim());
        if (!tokenRequest(f)) throw new IllegalStateException("token refresh failed, reconnect Spotify");
    }

    private static boolean tokenRequest(Map<String, String> formData) {
        try {
            HttpResponse<String> res = HTTP.send(HttpRequest.newBuilder(URI.create("https://accounts.spotify.com/api/token"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .timeout(Duration.ofSeconds(10))
                            .POST(HttpRequest.BodyPublishers.ofString(form(formData))).build(),
                    HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                status = "Spotify login error " + res.statusCode();
                EmberClient.LOG.warn("Spotify token error: {}", res.body());
                return false;
            }
            JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
            accessToken = o.get("access_token").getAsString();
            accessExpiry = System.currentTimeMillis() + o.get("expires_in").getAsLong() * 1000;
            if (o.has("refresh_token")) {
                EmberConfig.spotifyRefreshToken = o.get("refresh_token").getAsString();
                EmberConfig.save();
            }
            return true;
        } catch (Exception e) {
            status = "Login error: " + e.getMessage();
            return false;
        }
    }

    // ---------- helpers ----------

    private static String str(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    private static String form(Map<String, String> m) {
        return m.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> m = new LinkedHashMap<>();
        if (raw == null) return m;
        for (String pair : raw.split("&")) {
            int i = pair.indexOf('=');
            if (i < 0) continue;
            m.put(java.net.URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8),
                  java.net.URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
        }
        return m;
    }
}
