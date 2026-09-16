package dev.lukamadness.madnesscore.common.content.technology.blocks;

import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import dev.lukamadness.madnesscore.common.content.technology.energy.EnergyReceiver;
import dev.lukamadness.madnesscore.common.content.technology.energy.ModEnergyStorage;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatConduction;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatEnvironment;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFuelRegistry;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatReceiver;
import dev.lukamadness.madnesscore.common.content.technology.heat.ModHeatStorage;
import dev.lukamadness.madnesscore.common.content.technology.screen.EnergyConverterScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class EnergyConverterBlockEntity extends BlockEntity implements HeatReceiver, EnergyReceiver, MenuProvider {
    public static final int ENERGY_CAPACITY = 4_000;

    private static final int MAX_ENERGY_TRANSFER = 4;

    private static final double ENERGY_PER_DEGREE = 0.5;

    private final ModHeatStorage heatStorage = new ModHeatStorage(this::setChanged);
    private final ModEnergyStorage energyStorage =
            new ModEnergyStorage(ENERGY_CAPACITY, 0, MAX_ENERGY_TRANSFER, this::setChanged);

    private double energyAccumulator;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) Math.round(heatStorage.getTemperature());
                case 1 -> (int) Math.round(HeatFuelRegistry.getMaxHeatTemperature(level));
                case 2 -> energyStorage.getEnergy();
                case 3 -> energyStorage.getCapacity();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> heatStorage.setTemperature(value);
                case 2 -> energyStorage.setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public EnergyConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CONVERTER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergyConverterBlockEntity entity) {
        if (level.isClientSide) return;

        boolean dirty = false;

        double heatFlow = HeatConduction.gatherFromHotterNeighbors(level, pos, entity.heatStorage);
        if (heatFlow > 0) {
            heatFlow *= HeatEnvironment.heatGainMultiplier(level, pos);
            double maxTemperature = HeatFuelRegistry.getMaxHeatTemperature(level);
            if (entity.heatStorage.addTemperature(heatFlow, ModHeatStorage.AMBIENT_TEMPERATURE, maxTemperature)) dirty = true;

            entity.energyAccumulator += heatFlow * ENERGY_PER_DEGREE;
            int wholeEnergy = (int) entity.energyAccumulator;
            if (wholeEnergy > 0) {
                entity.energyAccumulator -= wholeEnergy;
                entity.energyStorage.generate(wholeEnergy);
                dirty = true;
            }
        } else {
            double lossMultiplier = HeatEnvironment.lossMultiplier(level, pos);
            if (entity.heatStorage.approachTemperature(ModHeatStorage.AMBIENT_TEMPERATURE,
                    HeatEnvironment.BASE_TEMPERATURE_LOSS_RATE * lossMultiplier)) dirty = true;
        }

        if (entity.energyStorage.getEnergy() > 0) {
            for (Direction direction : Direction.values()) {
                if (entity.energyStorage.getEnergy() <= 0) break;
                BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
                if (neighbor instanceof EnergyReceiver receiver) {
                    ModEnergyStorage neighborStorage = receiver.getEnergyStorage();
                    int toSend = Math.min(entity.energyStorage.getEnergy(), entity.energyStorage.getMaxExtract());
                    int accepted = neighborStorage.insert(toSend, true);
                    if (accepted > 0) {
                        neighborStorage.insert(accepted, false);
                        entity.energyStorage.extract(accepted, false);
                        dirty = true;
                    }
                }
            }
        }

        if (dirty) entity.setChanged();

        boolean shouldBeLit = entity.energyStorage.getEnergy() > 0;
        if (state.getValue(EnergyConverterBlock.LIT) != shouldBeLit) {
            level.setBlock(pos, state.setValue(EnergyConverterBlock.LIT, shouldBeLit), 3);
        }
    }

    @Override
    public ModHeatStorage getHeatStorage() {
        return heatStorage;
    }

    @Override
    public ModEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        heatStorage.writeNbt(tag, "Heat");
        energyStorage.writeNbt(tag, "Energy");
        tag.putDouble("EnergyAccumulator", energyAccumulator);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heatStorage.readNbt(tag, "Heat");
        energyStorage.readNbt(tag, "Energy");
        energyAccumulator = tag.getDouble("EnergyAccumulator");
    }

    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return getBlockPos();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new EnergyConverterScreenHandler(syncId, inv, level, getBlockPos(), containerData);
    }
}
