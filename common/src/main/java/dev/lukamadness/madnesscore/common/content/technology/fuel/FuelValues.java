package dev.lukamadness.madnesscore.common.content.technology.fuel;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Reemplazo 100% común (no-Fabric) del viejo {@code FuelRegistry.INSTANCE} que usaba
 * CoalGeneratorBlockEntity. FuelRegistry es una API de Fabric API, no existe en el
 * módulo common (que también compila para NeoForge), así que acá tenemos nuestra propia
 * tabla de "cuántos ticks de burnTime da este item".
 * <p>
 * HeatGeneratorBlockEntity solo consulta esto para items que ya pasaron el filtro
 * {@code ItemTags.COALS}, así que por ahora alcanza con precargar los vanilla (coal,
 * charcoal). Si en el futuro se agregan combustibles moddeados a ese tag (o se quiere
 * aceptar otros items fuera del tag), se registran acá con {@link #register(Item, int)}.
 */
public final class FuelValues {

    public static final FuelValues INSTANCE = new FuelValues();

    /** Mismo burnTime que usa el horno vanilla para coal/charcoal (1600 ticks = 80s). */
    public static final int COAL_BURN_TIME = 1600;

    private final Map<Item, Integer> values = new IdentityHashMap<>();

    private FuelValues() {
        register(Items.COAL, COAL_BURN_TIME);
        register(Items.CHARCOAL, COAL_BURN_TIME);
        register(Items.COAL_BLOCK, COAL_BURN_TIME * 10);
    }

    /**
     * Registra (o sobreescribe) cuántos ticks de burnTime da un item al quemarse en un
     * generador. Pensado para que otros mods/addons (o el propio Madness Core más
     * adelante) puedan sumar combustibles moddeados sin tocar esta clase.
     */
    public void register(Item item, int burnTimeTicks) {
        values.put(item, burnTimeTicks);
    }

    /**
     * Devuelve el burnTime en ticks para el item dado, o 0 si no está registrado
     * (equivalente al {@code null} que devolvía el viejo FuelRegistry.INSTANCE.get()).
     */
    public int get(Item item) {
        return values.getOrDefault(item, 0);
    }

    public boolean has(Item item) {
        return values.containsKey(item);
    }
}