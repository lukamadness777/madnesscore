package dev.lukamadness.madnesscore.common.registry.entity;

import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Registro multiplataforma de EntityType.
 * <p>
 * Ejemplo de uso:
 * <pre>{@code
 * public static final Supplier<EntityType<ExampleEntity>> EXAMPLE_ENTITY = register(
 *         "example_entity",
 *         ExampleEntity::new,
 *         MobCategory.CREATURE,
 *         builder -> builder.sized(0.9f, 1.4f).clientTrackingRange(8)
 * );
 * }</pre>
 */
public class ModEntities {

    private static <T extends Entity> Supplier<EntityType<T>> register(
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            UnaryOperator<EntityType.Builder<T>> builderOperator
    ) {
        return RegistryHelper.INSTANCE.registerEntity(id, factory, category, builderOperator);
    }

    public static void init() {
    }
}
