package dev.lukamadness.madnesscore.common.client.gui.options;

public enum Toggle {
    OFF,
    ON;

    public static Toggle fromBoolean(boolean value) {
        return value ? ON : OFF;
    }

    public boolean toBoolean() {
        return this == ON;
    }
}
