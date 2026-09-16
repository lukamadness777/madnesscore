package dev.lukamadness.madnesscore.common.content.technology.fuel;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.IdentityHashMap;
import java.util.Map;

public final class FuelValues {
    public static final FuelValues INSTANCE = new FuelValues();

    public static final int COAL_BURN_TIME = 1600;

    private final Map<Item, Integer> values = new IdentityHashMap<>();

    private FuelValues() {
        register(Items.COAL, COAL_BURN_TIME);
        register(Items.CHARCOAL, COAL_BURN_TIME);
        register(Items.COAL_BLOCK, COAL_BURN_TIME * 10);
    }

    public void register(Item item, int burnTimeTicks) {
        values.put(item, burnTimeTicks);
    }

    public int get(Item item) {
        return values.getOrDefault(item, 0);
    }

    public boolean has(Item item) {
        return values.containsKey(item);
    }
}
