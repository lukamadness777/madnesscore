package dev.lukamadness.madnesscore.common.content.technology.heat;

import java.util.Locale;

public final class HeatFormat {

    private HeatFormat() {}

    public static String format(long value) {
        if (value < 1_000) return String.valueOf(value);
        if (value < 1_000_000) return scale(value, 1_000, "k");
        if (value < 1_000_000_000) return scale(value, 1_000_000, "M");
        return scale(value, 1_000_000_000, "G");
    }

    private static String scale(long value, long unit, String suffix) {
        double scaled = value / (double) unit;
        String number = (scaled == Math.floor(scaled))
                ? String.valueOf((long) scaled)
                : String.format(Locale.ROOT, "%.1f", scaled);
        return number + suffix;
    }
}
