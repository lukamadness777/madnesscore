
package dev.lukamadness.madnesscore.common.content.technology.energy;

import java.util.Locale;

public final class EnergyFormat {
    private EnergyFormat() {}

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

    public static String formatExact(long value) {
        String digits = Long.toString(Math.abs(value));
        StringBuilder grouped = new StringBuilder();
        int count = 0;
        for (int i = digits.length() - 1; i >= 0; i--) {
            grouped.append(digits.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) grouped.append('.');
        }
        return (value < 0 ? "-" : "") + grouped.reverse();
    }
}
