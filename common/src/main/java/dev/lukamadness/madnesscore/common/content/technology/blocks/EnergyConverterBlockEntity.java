package dev.lukamadness.madnesscore.common.content.technology.blocks;

import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import dev.lukamadness.madnesscore.common.content.technology.energy.EnergyReceiver;
import dev.lukamadness.madnesscore.common.content.technology.energy.ModEnergyStorage;
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

/**
 * El puente entre Heat y Energy del diseño:
 * <pre>
 *   Heat Generator -> Energy Converter -> Energy -> Mesas
 * </pre>
 * Recibe Heat de cualquier vecino que lo empuje (implementa HeatReceiver, igual que
 * cualquier mesa que consuma Heat directamente), lo convierte a un ritmo fijo en Energy,
 * y empuja esa Energy a los vecinos que implementen EnergyReceiver — el mismo patrón
 * exacto que usa HeatGeneratorBlockEntity para empujar Heat.
 * <p>
 * No tiene inventario (no craftea, no inserta ni saca items): es un bloque puente, no
 * una mesa de crafteo. Sí tiene una pantalla puramente informativa (ver
 * EnergyConverterScreenHandler) que muestra Heat y Energy actuales, sin slots. No exige
 * temperatura mínima para convertir — eso lo decide cada mesa consumidora de Heat
 * directo (ej. el futuro Alloy Smeltery a 800°C, Compressor a 300°C), no el conversor.
 */
public class EnergyConverterBlockEntity extends BlockEntity implements HeatReceiver, EnergyReceiver, MenuProvider {

    public static final int HEAT_CAPACITY = 4_000;
    public static final int ENERGY_CAPACITY = 4_000;

    /** Cuánto Heat consume como máximo por tick. */
    public static final int MAX_HEAT_CONSUMED_PER_TICK = 8;

    /** Ritmo de conversión: 2 Heat -> 1 Energy. */
    public static final int HEAT_TO_ENERGY_RATIO = 2;

    private final ModHeatStorage heatStorage =
            new ModHeatStorage(HEAT_CAPACITY, MAX_HEAT_CONSUMED_PER_TICK, 0, this::setChanged);
    private final ModEnergyStorage energyStorage =
            new ModEnergyStorage(ENERGY_CAPACITY, 0, MAX_HEAT_CONSUMED_PER_TICK / HEAT_TO_ENERGY_RATIO, this::setChanged);

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> heatStorage.getHeat();
                case 1 -> heatStorage.getCapacity();
                case 2 -> energyStorage.getEnergy();
                case 3 -> energyStorage.getCapacity();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> heatStorage.setHeat(value);
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

        // Heat -> Energy: convierte todo lo que pueda, limitado por MAX_HEAT_CONSUMED_PER_TICK
        // y por cuánto lugar quede libre en el almacén de Energy.
        int energyRoom = entity.energyStorage.getCapacity() - entity.energyStorage.getEnergy();
        int maxByRoom = energyRoom * HEAT_TO_ENERGY_RATIO;
        int heatToConvert = Math.min(entity.heatStorage.getHeat(), MAX_HEAT_CONSUMED_PER_TICK);
        heatToConvert = Math.min(heatToConvert, maxByRoom);
        heatToConvert -= heatToConvert % HEAT_TO_ENERGY_RATIO; // solo convierte pares completos

        if (heatToConvert > 0) {
            entity.heatStorage.drain(heatToConvert);
            entity.energyStorage.generate(heatToConvert / HEAT_TO_ENERGY_RATIO);
            dirty = true;
        }

        // Empuja Energy SOLO a vecinos que implementen EnergyReceiver — mismo patrón que
        // HeatGeneratorBlockEntity usa para empujar Heat.
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
    }

    @Override
    public ModHeatStorage getHeatStorage() {
        return heatStorage;
    }

    // No drena a quien le empuja Heat: el Energy Converter llena su propia barra y
    // convierte a Energy "aprovechando" el Heat del vecino, sin competir por su reserva
    // (a diferencia de una mesa normal como el Alloy Smeltery, que sí la vacía).
    @Override
    public boolean drainsSource() {
        return false;
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heatStorage.readNbt(tag, "Heat");
        energyStorage.readNbt(tag, "Energy");
    }

    // Llamado por el wrapper de cada plataforma para saber qué BlockPos sincronizar
    // al abrir la pantalla (ver nota de clase en HeatGeneratorBlockEntity).
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