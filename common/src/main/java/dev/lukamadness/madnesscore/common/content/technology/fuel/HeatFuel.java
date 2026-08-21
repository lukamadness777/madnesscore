package dev.lukamadness.madnesscore.common.content.technology.fuel;

/**
 * Perfil de un combustible del Heat Generator: cuánto Heat (amount) produce por tick,
 * hasta qué temperatura puede llevar el generador, y qué tan rápido llega a esa
 * temperatura. Tal como se definió en el diseño: Carbón genera Heat lentamente y sube
 * la temperatura de a poco; Lava genera mucho Heat y la sube rápido.
 * <p>
 * burnTime (cuánto dura el combustible) sigue viniendo de otro lado: FuelValues para
 * items del tag ItemTags.COALS, o LAVA_BURN_TIME acá abajo para el balde de lava.
 */
public enum HeatFuel {

    /** Carbón, carbón vegetal, bloque de carbón — vía FuelValues + ItemTags.COALS. */
    COAL(1, 600.0, 0.5),

    /** Balde de lava — combustible de alto nivel: mucho Heat y sube la temperatura rápido. */
    LAVA(4, 1200.0, 2.5);

    /** Ticks de burnTime que da un balde de lava (20 000 ticks = 1000s). */
    public static final int LAVA_BURN_TIME = 20_000;

    private final int heatPerTick;
    private final double maxTemperature;
    private final double temperatureRisePerTick;

    HeatFuel(int heatPerTick, double maxTemperature, double temperatureRisePerTick) {
        this.heatPerTick = heatPerTick;
        this.maxTemperature = maxTemperature;
        this.temperatureRisePerTick = temperatureRisePerTick;
    }

    /** Heat (amount) generado por tick mientras este combustible está quemando. */
    public int getHeatPerTick() {
        return heatPerTick;
    }

    /** Temperatura máxima (°C) a la que este combustible puede llevar al generador. */
    public double getMaxTemperature() {
        return maxTemperature;
    }

    /** Qué tan rápido (°C/tick) sube la temperatura mientras quema. */
    public double getTemperatureRisePerTick() {
        return temperatureRisePerTick;
    }
}