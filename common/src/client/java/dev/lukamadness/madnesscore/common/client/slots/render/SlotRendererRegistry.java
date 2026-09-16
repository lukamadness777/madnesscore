package dev.lukamadness.madnesscore.common.client.slots.render;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
