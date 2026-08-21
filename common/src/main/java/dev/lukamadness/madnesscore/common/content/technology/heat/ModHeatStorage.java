package dev.lukamadness.madnesscore.common.content.technology.heat;

import net.minecraft.nbt.CompoundTag;

/**
 * Almacén de Heat (calor). Misma forma que ModEnergyStorage pero para un recurso
 * DISTINTO — Heat y Energy (Coal Energy) NO son intercambiables. Un bloque que solo
 * implementa HeatReceiver no puede recibir Energy, y viceversa.
 * <p>
 * El puente entre ambos recursos es el "Energy Converter" (EnergyConverterBlockEntity):
 * implementa HeatReceiver (para recibir Heat, igual que el futuro Compressor implementa
 * EnergyReceiver) y por dentro convierte ese Heat en Energy, empujándola a los vecinos
 * que implementen EnergyReceiver (mismo patrón que usa HeatGeneratorBlockEntity para
 * empujar Heat).
 */
public class ModHeatStorage {

    /** Temperatura ambiente por defecto (°C) a la que decae el Heat cuando no se alimenta. */
    public static final double AMBIENT_TEMPERATURE = 20.0;

    private int heat;
    private final int capacity;
    private final int maxInsert;
    private final int maxExtract;
    private final Runnable onChange;

    /**
     * Temperatura actual (°C), separada de "amount" (heat). El amount es cuanto Heat hay
     * almacenado; la temperatura es que tan caliente esta el sistema ahora mismo, y es lo
     * que consultan las mesas con requisito minimo (ej: Alloy Smeltery 800°C). Sube con
     * heatUp() (llamado por el generador mientras quema combustible) y baja con
     * coolTowards() (perdida ambiental, ver HeatEnvironment).
     */
    private double temperature = AMBIENT_TEMPERATURE;

    public ModHeatStorage(int capacity, int maxInsert, int maxExtract, Runnable onChange) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.onChange = onChange;
    }

    public void generate(int amount) {
        int accepted = Math.min(amount, capacity - heat);
        if (accepted > 0) {
            heat += accepted;
            if (onChange != null) onChange.run();
        }
    }

    public int insert(int amount, boolean simulate) {
        int accepted = Math.min(maxInsert, Math.min(amount, capacity - heat));
        if (!simulate && accepted > 0) {
            heat += accepted;
            if (onChange != null) onChange.run();
        }
        return accepted;
    }

    public int extract(int amount, boolean simulate) {
        int removed = Math.min(maxExtract, Math.min(amount, heat));
        if (!simulate && removed > 0) {
            heat -= removed;
            if (onChange != null) onChange.run();
        }
        return removed;
    }

    /**
     * Resta amount del heat almacenado (piso 0) SIN pasar por maxExtract -- a diferencia
     * de extract(), que es para transferencia entre bloques, drain() es para perdida
     * ambiental interna (ver HeatGeneratorBlockEntity) o para que el Energy Converter
     * consuma Heat y lo transforme en Energy.
     */
    public void drain(int amount) {
        int removed = Math.min(amount, heat);
        if (removed > 0) {
            heat -= removed;
            if (onChange != null) onChange.run();
        }
    }

    /** Empuja la temperatura hacia targetTemp a razon de ratePerTick. Solo sube. */
    public void heatUp(double targetTemp, double ratePerTick) {
        if (temperature < targetTemp) {
            temperature = Math.min(targetTemp, temperature + ratePerTick);
            if (onChange != null) onChange.run();
        }
    }

    /** Deja que la temperatura decaiga hacia ambientTemp a razon de ratePerTick. Solo baja. */
    public void coolTowards(double ambientTemp, double ratePerTick) {
        if (temperature > ambientTemp) {
            temperature = Math.max(ambientTemp, temperature - ratePerTick);
            if (onChange != null) onChange.run();
        }
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double value) {
        this.temperature = value;
    }

    /** Si la temperatura actual alcanza el minimo que pide una mesa (ej: 300°C, 800°C). */
    public boolean isAtLeast(double minTemperature) {
        return temperature >= minTemperature;
    }

    public int getHeat() {
        return heat;
    }

    public void setHeat(int value) {
        this.heat = Math.max(0, Math.min(capacity, value));
    }

    public int getCapacity() {
        return capacity;
    }

    public int getMaxInsert() {
        return maxInsert;
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    public void writeNbt(CompoundTag tag, String key) {
        tag.putInt(key, heat);
        tag.putDouble(key + "Temperature", temperature);
    }
    public void readNbt(CompoundTag tag, String key) {
        setHeat(tag.getInt(key));
        temperature = tag.contains(key + "Temperature") ? tag.getDouble(key + "Temperature") : AMBIENT_TEMPERATURE;
    }
}