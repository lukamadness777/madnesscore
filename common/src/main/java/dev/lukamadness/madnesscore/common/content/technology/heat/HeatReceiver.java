package dev.lukamadness.madnesscore.common.content.technology.heat;

/**
 * Cualquier block entity que pueda RECIBIR Heat empuja implementa esto.
 * El Heat Generator busca este interface en sus 6 vecinos cada tick, y SOLO en este
 * interface — un bloque que implementa EnergyReceiver pero no HeatReceiver no recibe
 * nada de un Heat Generator, y viceversa.
 * <p>
 * Algunas mesas que consumen Heat directo (no Energy convertida) exigen además una
 * temperatura mínima para poder funcionar: Compressor 300°C, Alloy Smeltery 800°C. Esa
 * mínima se expresa acá con {@link #getMinTemperature()} — 0 (el default) significa que
 * el receptor no exige ninguna, como el propio Heat Generator o el Energy Converter, que
 * solo necesitan amount, no temperatura.
 */
public interface HeatReceiver {
    ModHeatStorage getHeatStorage();

    /** Temperatura mínima (°C) que este receptor necesita para operar. 0 = sin requisito. */
    default double getMinTemperature() {
        return 0.0;
    }

    /** Atajo: si getHeatStorage() ya alcanzó getMinTemperature(). */
    default boolean isHotEnough() {
        return getHeatStorage().isAtLeast(getMinTemperature());
    }

    /**
     * Si true (default), el emisor que empuja Heat hacia este receptor DRENA esa
     * cantidad de su propio almacén (transferencia normal, ej. Heat Generator ->
     * Alloy Smeltery). Si false, el receptor llena su propio almacén igual, pero el
     * emisor NO pierde nada — el Energy Converter usa esto: "lee"/aprovecha el Heat
     * del vecino sin vaciarlo, en vez de competir por un recurso compartido.
     */
    default boolean drainsSource() {
        return true;
    }
}