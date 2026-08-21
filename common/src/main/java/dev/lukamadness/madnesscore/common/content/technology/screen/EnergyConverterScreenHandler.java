package dev.lukamadness.madnesscore.common.content.technology.screen;

import dev.lukamadness.madnesscore.common.registry.menu.ModMenus;
import dev.lukamadness.madnesscore.common.content.technology.blocks.EnergyConverterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/**
 * Pantalla puramente informativa del Energy Converter: no tiene slots (ni inventario
 * propio, ni siquiera el inventario del jugador) — solo sincroniza Heat y Energy para
 * que la pantalla muestre las dos barras. No hay nada que craftear, insertar ni sacar
 * acá, es sólo un "medidor" del bloque puente Heat -> Energy.
 */
public class EnergyConverterScreenHandler extends AbstractContainerMenu {

    private final ContainerData containerData;

    @Nullable
    private final Level level;
    @Nullable
    private final BlockPos pos;

    public EnergyConverterScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, null, null, new SimpleContainerData(4));
    }

    public EnergyConverterScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, playerInventory.player.level(), pos, new SimpleContainerData(4));
    }

    public EnergyConverterScreenHandler(int syncId, Inventory playerInventory, @Nullable Level level,
                                        @Nullable BlockPos pos, ContainerData containerData) {
        super(ModMenus.ENERGY_CONVERTER.get(), syncId);
        checkContainerDataCount(containerData, 4);
        this.level = level;
        this.pos = pos;
        this.containerData = containerData;

        addDataSlots(containerData);
    }

    public int getHeat() { return containerData.get(0); }
    public int getHeatCapacity() { return containerData.get(1); }
    public int getEnergy() { return containerData.get(2); }
    public int getEnergyCapacity() { return containerData.get(3); }

    // Sin slots: no hay nada que mover con shift-click.
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || pos == null) return true; // menú client-side "dummy", nunca se usa para validar
        return level.getBlockEntity(pos) instanceof EnergyConverterBlockEntity
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}