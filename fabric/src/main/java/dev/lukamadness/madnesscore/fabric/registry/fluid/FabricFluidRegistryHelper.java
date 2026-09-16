package dev.lukamadness.madnesscore.fabric.registry.fluid;

import dev.lukamadness.madnesscore.common.registry.fluid.FluidEntry;
import dev.lukamadness.madnesscore.common.registry.fluid.FluidRegistryHelper;
import dev.lukamadness.madnesscore.common.registry.fluid.ModFluidProperties;
import dev.lukamadness.madnesscore.common.registry.fluid.MutableSupplier;
import dev.lukamadness.madnesscore.common.registry.fluid.SimpleFlowingFluid;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class FabricFluidRegistryHelper implements FluidRegistryHelper {
    private static final Map<String, RenderEntry> PENDING_RENDER = new LinkedHashMap<>();

    public record RenderEntry(Fluid source, Fluid flowing, ModFluidProperties properties) {
    }

    public static Map<String, RenderEntry> getPendingRenderEntries() {
        return PENDING_RENDER;
    }

    @Override
    public FluidEntry<SimpleFlowingFluid> registerFluid(
            String id,
            ModFluidProperties properties,
            UnaryOperator<BlockBehaviour.Properties> blockPropertiesOp,
            boolean withBucket
    ) {
        String flowingId = "flowing_" + id;

        MutableSupplier<Fluid> sourceRef = new MutableSupplier<>();
        MutableSupplier<Fluid> flowingRef = new MutableSupplier<>();
        MutableSupplier<LiquidBlock> blockRef = new MutableSupplier<>();
        MutableSupplier<Item> bucketRef = new MutableSupplier<>();

        SimpleFlowingFluid.Properties fluidProps = new SimpleFlowingFluid.Properties()
                .still(sourceRef)
                .flowing(flowingRef)
                .block(blockRef)
                .bucket(bucketRef);

        SimpleFlowingFluid.Source source = new SimpleFlowingFluid.Source(fluidProps);
        SimpleFlowingFluid.Flowing flowing = new SimpleFlowingFluid.Flowing(fluidProps);

        Fluid registeredSource = Registry.register(BuiltInRegistries.FLUID, RegistryHelper.id(id), source);
        Fluid registeredFlowing = Registry.register(BuiltInRegistries.FLUID, RegistryHelper.id(flowingId), flowing);
        sourceRef.set(registeredSource);
        flowingRef.set(registeredFlowing);

        BlockBehaviour.Properties blockProperties = blockPropertiesOp.apply(
                BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
        );
        LiquidBlock liquidBlock = new LiquidBlock(source, blockProperties);
        Block registeredBlock = Registry.register(BuiltInRegistries.BLOCK, RegistryHelper.id(id), liquidBlock);
        blockRef.set((LiquidBlock) registeredBlock);

        if (withBucket) {
            BucketItem bucketItem = new BucketItem(
                    registeredSource,
                    new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)
            );
            Item registeredBucket = Registry.register(BuiltInRegistries.ITEM, RegistryHelper.id(id + "_bucket"), bucketItem);
            bucketRef.set(registeredBucket);
        }

        PENDING_RENDER.put(id, new RenderEntry(registeredSource, registeredFlowing, properties));

        return new FluidEntry<>(
                () -> (SimpleFlowingFluid) sourceRef.get(),
                () -> (SimpleFlowingFluid) flowingRef.get(),
                blockRef,
                bucketRef
        );
    }
}
