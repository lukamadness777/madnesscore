package dev.lukamadness.madnesscore.common.registry.fluid;

import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ServiceLoader;
import java.util.function.UnaryOperator;

public interface FluidRegistryHelper {
    FluidRegistryHelper INSTANCE = ServiceLoader.load(FluidRegistryHelper.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                    "cant find implementation FluidRegistryHelper (missing META-INF/services)"));

    FluidEntry<SimpleFlowingFluid> registerFluid(
            String id,
            ModFluidProperties properties,
            UnaryOperator<BlockBehaviour.Properties> blockPropertiesOp,
            boolean withBucket
    );

    default FluidEntry<SimpleFlowingFluid> registerFluid(String id, ModFluidProperties properties) {
        return registerFluid(id, properties, UnaryOperator.identity(), true);
    }

    default FluidEntry<SimpleFlowingFluid> registerFluidNoBucket(String id, ModFluidProperties properties) {
        return registerFluid(id, properties, UnaryOperator.identity(), false);
    }
}
