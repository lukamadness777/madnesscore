package dev.lukamadness.madnesscore.common.content.technology.heat;

public final class HeatFormat {
    private HeatFormat() {}

    public static String formatTemperature(double celsius) {
        return Math.round(celsius) + "\u00B0C";
    }
}
