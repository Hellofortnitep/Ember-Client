package com.ember.client.spotify;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Time-synced lyric lines parsed from LRC format ("[01:23.45] some line"). */
public final class Lyrics {
    public record Line(long timeMs, String text) {}

    public static final Lyrics NONE = new Lyrics(List.of(), false);
    private static final Pattern STAMP = Pattern.compile("\\[(\\d+):(\\d+(?:\\.\\d+)?)]");

    public final List<Line> lines;
    public final boolean instrumental;

    private Lyrics(List<Line> lines, boolean instrumental) {
        this.lines = lines;
        this.instrumental = instrumental;
    }

    public static Lyrics instrumental() { return new Lyrics(List.of(), true); }

    public static Lyrics parse(String lrc) {
        if (lrc == null || lrc.isBlank()) return NONE;
        List<Line> out = new ArrayList<>();
        for (String raw : lrc.split("\\r?\\n")) {
            Matcher m = STAMP.matcher(raw);
            List<Long> times = new ArrayList<>();
            int end = 0;
            while (m.find()) {
                long ms = Long.parseLong(m.group(1)) * 60_000L + Math.round(Double.parseDouble(m.group(2)) * 1000);
                times.add(ms);
                end = m.end();
            }
            if (times.isEmpty()) continue;
            String text = raw.substring(end).trim();
            for (long t : times) out.add(new Line(t, text));
        }
        out.sort(Comparator.comparingLong(Line::timeMs));
        return new Lyrics(List.copyOf(out), false);
    }

    /** Index of the line playing at the given position, or -1 before the first line. */
    public int indexAt(long positionMs) {
        int lo = 0, hi = lines.size() - 1, ans = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (lines.get(mid).timeMs() <= positionMs) { ans = mid; lo = mid + 1; } else hi = mid - 1;
        }
        return ans;
    }

    public String lineAt(int index) {
        return index >= 0 && index < lines.size() ? lines.get(index).text() : null;
    }
}
