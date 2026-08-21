package dev.lukamadness.madnesscore.neoforge.registry.fluid;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.fluid.FluidEntry;
import dev.lukamadness.madnesscore.common.registry.fluid.FluidRegistryHelper;
import dev.lukamadness.madnesscore.common.registry.fluid.ModFluidProperties;
import dev.lukamadness.madnesscore.common.registry.fluid.MutableSupplier;
import dev.lukamadness.madnesscore.common.registry.fluid.SimpleFlowingFluid;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NeoForgeFluidRegistryHelper implements FluidRegistryHelper {

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, MadnessCoreCommon.MOD_ID);

    /**
     * Se usa desde MadnessCoreNeoForgeClient (RegisterClientExtensionsEvent) para
     * registrar IClientFluidTypeExtensions con las texturas/tinte correctos.
     */
    private static final Map<String, ModFluidProperties> PENDING_CLIENT_EXTENSIONS = new LinkedHashMap<>();

    public static Map<String, ModFluidProperties> getPendingClientExtensions() {
        return PENDING_CLIENT_EXTENSIONS;
    }

    public static Map<String, DeferredHolder<FluidType, FluidType>> getFluidTypes() {
        return FLUID_TYPE_HOLDERS;
    }

    private static final Map<String, DeferredHolder<FluidType, FluidType>> FLUID_TYPE_HOLDERS = new LinkedHashMap<>();

    // Se llama UNA vez desde MadnessCoreNeoForge(IEventBus), junto a NeoForgeRegistryHelper.registerToBus(eventBus)
    public static void registerToBus(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
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

        DeferredHolder<FluidType, FluidType> fluidType = FLUID_TYPES.register(id, () ->
                new FluidType(FluidType.Properties.create()
                        .lightLevel(properties.luminosity())
                        .density(properties.density())
                        .viscosity(properties.viscosity())));
        FLUID_TYPE_HOLDERS.put(id, fluidType);

        SimpleFlowingFluid.Properties fluidProps = new SimpleFlowingFluid.Properties()
                .still(sourceRef)
                .flowing(flowingRef)
                .block(blockRef)
                .bucket(bucketRef);

        DeferredHolder<Fluid, NeoForgeSource> sourceHolder =
                FLUIDS.register(id, () -> new NeoForgeSource(fluidProps, fluidType));
        DeferredHolder<Fluid, NeoForgeFlowing> flowingHolder =
                FLUIDS.register(flowingId, () -> new NeoForgeFlowing(fluidProps, fluidType));

        sourceRef.bind(sourceHolder::get);
        flowingRef.bind(flowingHolder::get);

        BlockBehaviour.Properties blockProperties = blockPropertiesOp.apply(
                BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
        );
        DeferredHolder<Block, LiquidBlock> blockHolder =
                BLOCKS.register(id, () -> new LiquidBlock(sourceHolder.get(), blockProperties));
        blockRef.bind(blockHolder);

        Supplier<Item> bucketSupplier = () -> null;
        if (withBucket) {
            DeferredHolder<Item, BucketItem> bucketHolder = ITEMS.register(id + "_bucket", () -> new BucketItem(
                    sourceHolder.get(),
                    new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)
            ));
            bucketRef.bind(bucketHolder::get);
            bucketSupplier = bucketHolder::get;
        }

        PENDING_CLIENT_EXTENSIONS.put(id, properties);

        Supplier<Item> finalBucketSupplier = bucketSupplier;
        return new FluidEntry<>(
                () -> sourceHolder.get(),
                () -> flowingHolder.get(),
                blockRef,
                finalBucketSupplier
        );
    }

    /**
     * Igual que SimpleFlowingFluid.Source pero pisando getFluidType(), que en
     * NeoForge es un metodo agregado por sus parches ASM sobre Fluid y NO
     * existe en el classpath vanilla del modulo common.
     */
    private static class NeoForgeSource extends SimpleFlowingFluid.Source {
        private final Supplier<FluidType> fluidType;

        NeoForgeSource(Properties properties, Supplier<FluidType> fluidType) {
            super(properties);
            this.fluidType = fluidType;
        }

        @Override
        public FluidType getFluidType() {
            return fluidType.get();
        }
    }

    private static class NeoForgeFlowing extends SimpleFlowingFluid.Flowing {
        private final Supplier<FluidType> fluidType;

        NeoForgeFlowing(Properties properties, Supplier<FluidType> fluidType) {
            super(properties);
            this.fluidType = fluidType;
        }

        @Override
        public FluidType getFluidType() {
            return fluidType.get();
        }
    }
}