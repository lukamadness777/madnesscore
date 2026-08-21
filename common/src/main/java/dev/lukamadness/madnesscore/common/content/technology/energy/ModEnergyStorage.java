// common/tecnology/energy/ModEnergyStorage.java
package dev.lukamadness.madnesscore.common.content.technology.energy;

import net.minecraft.nbt.CompoundTag;

/**
 * Almacén de Energy. Misma forma que ModHeatStorage pero para un recurso DISTINTO —
 * ver la nota en ModHeatStorage.
 */
public class ModEnergyStorage {

    private int energy;
    private final int capacity;
    private final int maxInsert;
    private final int maxExtract;
    private final Runnable onChange;

    public ModEnergyStorage(int capacity, int maxInsert, int maxExtract, Runnable onChange) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.onChange = onChange;
    }

    public void generate(int amount) {
        int accepted = Math.min(amount, capacity - energy);
        if (accepted > 0) {
            energy += accepted;
            if (onChange != null) onChange.run();
        }
    }

    public int insert(int amount, boolean simulate) {
        int accepted = Math.min(maxInsert, Math.min(amount, capacity - energy));
        if (!simulate && accepted > 0) {
            energy += accepted;
            if (onChange != null) onChange.run();
        }
        return accepted;
    }

    public int extract(int amount, boolean simulate) {
        int removed = Math.min(maxExtract, Math.min(amount, energy));
        if (!simulate && removed > 0) {
            energy -= removed;
            if (onChange != null) onChange.run();
        }
        return removed;
    }

    public int getEnergy() { return energy; }
    public void setEnergy(int value) { this.energy = Math.max(0, Math.min(capacity, value)); }
    public int getCapacity() { return capacity; }
    public int getMaxInsert() { return maxInsert; }
    public int getMaxExtract() { return maxExtract; }

    public void writeNbt(CompoundTag tag, String key) { tag.putInt(key, energy); }
    public void readNbt(CompoundTag tag, String key) { setEnergy(tag.getInt(key)); }
}