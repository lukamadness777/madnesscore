package dev.lukamadness.madnesscore.common.registry.component;

import dev.lukamadness.madnesscore.common.api.slots.SlotAttributeModifiersComponent;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalColors;
import dev.lukamadness.madnesscore.common.content.tailoring.TailoringColors;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitItem;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import net.minecraft.core.component.DataComponentType;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final Supplier<DataComponentType<SlotAttributeModifiersComponent>> SLOT_ATTRIBUTE_MODIFIERS = register(
            "slot_attribute_modifiers",
            builder -> builder
                    .persistent(SlotAttributeModifiersComponent.CODEC)
                    .networkSynchronized(SlotAttributeModifiersComponent.STREAM_CODEC)
    );

    public static final Supplier<DataComponentType<TailoringColors>> TAILORING_COLORS = register(
            "tailoring_colors",
            builder -> builder
                    .persistent(TailoringColors.CODEC)
                    .networkSynchronized(TailoringColors.STREAM_CODEC)
    );

    public static final Supplier<DataComponentType<FormalColors>> FORMAL_COLORS = register(
            "formal_colors",
            builder -> builder
                    .persistent(FormalColors.CODEC)
                    .networkSynchronized(FormalColors.STREAM_CODEC)
    );

    public static <T> Supplier<DataComponentType<T>> register(
            String id,
            java.util.function.UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        return RegistryHelperLoader.INSTANCE.registerDataComponentType(id, builderOperator);
    }

    public static <T> Supplier<DataComponentType<T>> register(
            String namespace,
            String id,
            java.util.function.UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        return RegistryHelperLoader.INSTANCE.registerDataComponentType(namespace, id, builderOperator);
    }

    public static void init() {
    }
}
