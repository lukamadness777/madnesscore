package dev.lukamadness.madnesscore.common.client.slots;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registro de {@link SlotRenderer} por item (Fase 4). Portado de
 * dev.emi.trinkets.api.client.TrinketRendererRegistry. Llamar {@link #registerRenderer} desde
 * codigo de cliente (ej: {@code MadnessCoreCommonClient#init()} de tu addon/mod).
 */
public final class SlotRendererRegistry {

    private static final Map<Item, SlotRenderer<?>> RENDERERS = new HashMap<>();

    private SlotRendererRegistry() {
    }

    public static void registerRenderer(Item item, SlotRenderer<?> renderer) {
        RENDERERS.put(item, renderer);
    }

    public static Optional<SlotRenderer<?>> getRenderer(Item item) {
        return Optional.ofNullable(RENDERERS.get(item));
    }
}