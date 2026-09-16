
package dev.lukamadness.madnesscore.common.content.technology.screen;

import dev.lukamadness.madnesscore.common.registry.menu.ModMenus;
import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlockEntity;
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

public class AlloySmelteryScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData containerData;

    private static final int INPUT_SLOTS = AlloySmelteryBlockEntity.INPUT_SLOTS;
    private static final int OUTPUT_SLOTS = AlloySmelteryBlockEntity.OUTPUT_SLOTS;
    private static final int TOTAL_SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;

    public AlloySmelteryScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, resolveInventory(playerInventory, pos), new SimpleContainerData(4));
    }

    private static Container resolveInventory(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof Container inv) return inv;
        return new SimpleContainer(TOTAL_SLOTS);
    }

    public AlloySmelteryScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData containerData) {
        super(ModMenus.ALLOY_SMELTERY.get(), syncId);
        checkContainerSize(inventory, TOTAL_SLOTS);
        checkContainerDataCount(containerData, 4);
        this.inventory = inventory;
        this.containerData = containerData;

        inventory.startOpen(playerInventory.player);

        int[][] inputPos = { {13, 26}, {31, 26}, {13, 44}, {31, 44} };
        for (int i = 0; i < INPUT_SLOTS; i++) {
            this.addSlot(new Slot(inventory, i, inputPos[i][0], inputPos[i][1]));
        }

        int[][] outputPos = { {95, 26}, {113, 26}, {95, 44}, {113, 44} };
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            final int slotIndex = INPUT_SLOTS + i;
            this.addSlot(new Slot(inventory, slotIndex, outputPos[i][0], outputPos[i][1]) {
                @Override
                public boolean mayPlace(ItemStack stack) { return false; }
            });
        }

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));

        addDataSlots(containerData);
    }

    public int getTemperature() { return containerData.get(0); }
    public int getMaxTemperature() { return containerData.get(1); }
    public int getProgress() { return containerData.get(2); }
    public int getMaxProgress() { return containerData.get(3); }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack original = slot.getItem();
            newStack = original.copy();
            if (slotIndex < TOTAL_SLOTS) {
                if (!moveItemStackTo(original, TOTAL_SLOTS, TOTAL_SLOTS + 36, true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(original, 0, INPUT_SLOTS, false)) {
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
