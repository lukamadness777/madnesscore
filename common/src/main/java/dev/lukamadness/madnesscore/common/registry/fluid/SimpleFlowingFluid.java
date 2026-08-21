package dev.lukamadness.madnesscore.common.registry.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Base 100% comun (vanilla puro, sin API de Fabric ni de NeoForge) para un
 * fluido custom, modelada sobre WaterFluid/LavaFluid. Toda la data que hace
 * falta (fluido opuesto, bloque, bucket, sonidos) se inyecta via
 * {@link Properties} con Suppliers para poder resolver referencias
 * circulares (source {@code <->} flowing) despues de terminado el registro.
 * <p>
 * NOTA: en NeoForge, {@code Fluid} tiene un metodo adicional
 * {@code getFluidType()} que esta clase NO puede implementar porque ese
 * metodo no existe en el classpath vanilla del modulo common. Por eso
 * NeoForgeFluidRegistryHelper usa subclases propias (NeoForgeSource /
 * NeoForgeFlowing) que extienden Source/Flowing y agregan ese override.
 */
public abstract class SimpleFlowingFluid extends FlowingFluid {

    protected final Properties properties;

    protected SimpleFlowingFluid(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Fluid getFlowing() {
        return properties.flowing.get();
    }

    @Override
    public Fluid getSource() {
        return properties.still.get();
    }

    @Override
    public Item getBucket() {
        return properties.bucket != null ? properties.bucket.get() : Items.AIR;
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return properties.canConvertToSource;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return properties.slopeFindDistance;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return properties.dropOff;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return properties.tickRate;
    }

    @Override
    protected float getExplosionResistance() {
        return properties.explosionResistance;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return properties.block.get().defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == properties.still.get() || fluid == properties.flowing.get();
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !isSame(fluid);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        if (properties.fillSound != null) {
            return Optional.ofNullable(properties.fillSound.get());
        }
        return super.getPickupSound();
    }

    public static class Flowing extends SimpleFlowingFluid {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends SimpleFlowingFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    /**
     * Propiedades de comportamiento del fluido (nada de rendering: eso vive
     * en {@link ModFluidProperties}, que es lo que consume cada
     * FluidRegistryHelper por plataforma).
     */
    public static class Properties {
        Supplier<? extends Fluid> still;
        Supplier<? extends Fluid> flowing;
        Supplier<? extends Item> bucket;
        Supplier<? extends LiquidBlock> block;
        Supplier<SoundEvent> fillSound;
        Supplier<SoundEvent> emptySound;

        int slopeFindDistance = 4;
        int dropOff = 1;
        int tickRate = 5;
        float explosionResistance = 100.0f;
        boolean canConvertToSource = false;

        public Properties still(Supplier<? extends Fluid> still) {
            this.still = still;
            return this;
        }

        public Properties flowing(Supplier<? extends Fluid> flowing) {
            this.flowing = flowing;
            return this;
        }

        public Properties bucket(Supplier<? extends Item> bucket) {
            this.bucket = bucket;
            return this;
        }

        public Properties block(Supplier<? extends LiquidBlock> block) {
            this.block = block;
            return this;
        }

        public Properties fillSound(Supplier<SoundEvent> fillSound) {
            this.fillSound = fillSound;
            return this;
        }

        public Properties emptySound(Supplier<SoundEvent> emptySound) {
            this.emptySound = emptySound;
            return this;
        }

        public Properties slopeFindDistance(int slopeFindDistance) {
            this.slopeFindDistance = slopeFindDistance;
            return this;
        }

        public Properties dropOff(int dropOff) {
            this.dropOff = dropOff;
            return this;
        }

        public Properties tickRate(int tickRate) {
            this.tickRate = tickRate;
            return this;
        }

        public Properties explosionResistance(float explosionResistance) {
            this.explosionResistance = explosionResistance;
            return this;
        }

        public Properties canConvertToSource(boolean canConvertToSource) {
            this.canConvertToSource = canConvertToSource;
            return this;
        }
    }
}