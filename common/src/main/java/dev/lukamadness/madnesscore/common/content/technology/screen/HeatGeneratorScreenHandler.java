package dev.lukamadness.madnesscore.common.content.technology.screen;

import dev.lukamadness.madnesscore.common.registry.menu.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class HeatGeneratorScreenHandler extends AbstractContainerMenu {

    private final Container inventory;
    private final ContainerData containerData;

    public HeatGeneratorScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory,
                resolveInventory(playerInventory, pos),
                new SimpleContainerData(4));
    }

    private static Container resolveInventory(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof Container inv) return inv;
        return new SimpleContainer(1);
    }

    public HeatGeneratorScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData containerData) {
        super(ModMenus.HEAT_GENERATOR.get(), syncId);
        checkContainerSize(inventory, 1);
        checkContainerDataCount(containerData, 4);
        this.inventory = inventory;
        this.containerData = containerData;

        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 80, 37));

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));

        addDataSlots(containerData);
    }

    public int getHeat() { return containerData.get(0); }
    public int getHeatCapacity() { return containerData.get(1); }
    public int getBurnTime() { return containerData.get(2); }
    public int getBurnTimeTotal() { return containerData.get(3); }
    public boolean isBurning() { return getBurnTime() > 0; }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack original = slot.getItem();
            newStack = original.copy();
            if (slotIndex == 0) {
                if (!moveItemStackTo(original, 1, 37, true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(original, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }
}