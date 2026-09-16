
package dev.lukamadness.madnesscore.common.content.technology.screen;

import dev.lukamadness.madnesscore.common.content.technology.blocks.CompressorBlockEntity;
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

public class CompressorScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData containerData;

    public CompressorScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, resolveInventory(playerInventory, pos), new SimpleContainerData(4));
    }

    private static Container resolveInventory(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof Container inv) return inv;
        return new SimpleContainer(2);
    }

    public CompressorScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData containerData) {
        super(ModMenus.COMPRESSOR.get(), syncId);
        checkContainerSize(inventory, 2);
        checkContainerDataCount(containerData, 4);
        this.inventory = inventory;
        this.containerData = containerData;

        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 45, 37));
        this.addSlot(new Slot(inventory, 1, 93, 37) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));

        addDataSlots(containerData);
    }

    public int getEnergy() { return containerData.get(0); }
    public int getEnergyCapacity() { return containerData.get(1); }
    public int getProgress() { return containerData.get(2); }
    public int getMaxProgress() { return containerData.get(3); }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack original = slot.getItem();
            newStack = original.copy();
            if (slotIndex < 2) {
                if (!moveItemStackTo(original, 2, 38, true)) return ItemStack.EMPTY;
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
