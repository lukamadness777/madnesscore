package dev.lukamadness.madnesscore.common.slots.gui;

import dev.lukamadness.madnesscore.common.slots.SlotEquipLogic;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PlayerDynamicSlot extends Slot implements DynamicSlot {
    private final SlotGroup group;
    private final SlotType type;
    private final int offset;
    private final boolean anchor;
    private final SlotInventory slotInventory;

    public PlayerDynamicSlot(SlotInventory inventory, int index, int x, int y, SlotGroup group, SlotType type,
                              int offset, boolean anchor) {
        super(inventory, index, x, y);
        this.group = group;
        this.type = type;
        this.offset = offset;
        this.anchor = anchor;
        this.slotInventory = inventory;
    }

    private SlotReference reference() {
        return new SlotReference(this.slotInventory, this.offset);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return SlotEquipLogic.canInsert(stack, reference(), this.slotInventory.getComponent().getEntity());
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack stack = this.getItem();
        if (stack.isEmpty()) {
            return true;
        }
        return SlotsApi.getSlottable(stack.getItem()).canUnequip(stack, reference(), player);
    }

    public SlotReference madnesscore$getReference() {
        return reference();
    }

    public SlotGroup madnesscore$getGroup() {
        return this.group;
    }

    public int madnesscore$getOffset() {
        return this.offset;
    }

    public boolean madnesscore$isAnchor() {
        return this.anchor;
    }

    @Override
    public SlotType madnesscore$getType() {
        return this.type;
    }

    @Override
    public ResourceLocation madnesscore$getBackground() {
        return this.type.getIcon();
    }
}
