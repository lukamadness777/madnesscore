package dev.lukamadness.madnesscore.common.content.technology.heat;

public interface HeatReceiver {
    ModHeatStorage getHeatStorage();

    default double getMinTemperature() {
        return 0.0;
    }

    default boolean isHotEnough() {
        return getHeatStorage().isAtLeast(getMinTemperature());
    }
}
