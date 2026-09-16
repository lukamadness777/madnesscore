package dev.lukamadness.madnesscore.common.content.technology.heat;

import net.minecraft.nbt.CompoundTag;

public class ModHeatStorage {
    public static final double AMBIENT_TEMPERATURE = 20.0;

    private double temperature;
    private final Runnable onChange;

    public ModHeatStorage(Runnable onChange) {
        this.temperature = AMBIENT_TEMPERATURE;
        this.onChange = onChange;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double value) {
        this.temperature = value;
    }

    public boolean approachTemperature(double targetTemperature, double rate) {
        double diff = targetTemperature - temperature;
        if (Math.abs(diff) < 0.01) return false;
        temperature += diff * rate;
        if (onChange != null) onChange.run();
        return true;
    }

    public boolean addTemperature(double delta, double min, double max) {
        double newTemp = Math.max(min, Math.min(max, temperature + delta));
        if (Math.abs(newTemp - temperature) < 0.0001) return false;
        temperature = newTemp;
        if (onChange != null) onChange.run();
        return true;
    }

    public boolean isAtLeast(double minTemperature) {
        return temperature >= minTemperature;
    }

    public void writeNbt(CompoundTag tag, String key) {
        tag.putDouble(key + "Temperature", temperature);
    }

    public void readNbt(CompoundTag tag, String key) {
        temperature = tag.contains(key + "Temperature") ? tag.getDouble(key + "Temperature") : AMBIENT_TEMPERATURE;
    }
}
