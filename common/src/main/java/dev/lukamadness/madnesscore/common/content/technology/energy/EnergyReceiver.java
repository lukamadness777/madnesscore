// common/tecnology/energy/EnergyReceiver.java
package dev.lukamadness.madnesscore.common.content.technology.energy;

/**
 * Cualquier block entity que pueda RECIBIR Energy implementa esto. Es un recurso
 * separado de Heat — HeatGeneratorBlockEntity NUNCA empuja acá directamente; el
 * futuro Energy Converter es el único puente (implementa HeatReceiver, y por dentro
 * empuja a estos).
 */
public interface EnergyReceiver {
    ModEnergyStorage getEnergyStorage();
}