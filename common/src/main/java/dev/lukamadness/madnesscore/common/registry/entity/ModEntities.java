package dev.lukamadness.madnesscore.common.registry.entity;

import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ModEntities {
    public static <T extends Entity> Supplier<EntityType<T>> register(
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            UnaryOperator<EntityType.Builder<T>> builderOperator
    ) {
        return RegistryHelperLoader.INSTANCE.registerEntity(id, factory, category, builderOperator);
    }

    public static <T extends Entity> Supplier<EntityType<T>> register(
            String namespace,
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            UnaryOperator<EntityType.Builder<T>> builderOperator
    ) {
        return RegistryHelperLoader.INSTANCE.registerEntity(namespace, id, factory, category, builderOperator);
    }

    public static void init() {
    }
}
