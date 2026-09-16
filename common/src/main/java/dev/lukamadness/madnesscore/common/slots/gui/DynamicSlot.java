package dev.lukamadness.madnesscore.common.slots.gui;

import dev.lukamadness.madnesscore.common.slots.SlotEquipLogic;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface DynamicSlot {
    ResourceLocation madnesscore$getBackground();

    SlotType madnesscore$getType();

    static boolean canInsert(ItemStack stack, SlotReference ref, LivingEntity entity) {
        return SlotEquipLogic.canInsert(stack, ref, entity);
    }
}
