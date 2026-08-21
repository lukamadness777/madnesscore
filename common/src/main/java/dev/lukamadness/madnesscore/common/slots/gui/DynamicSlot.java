package dev.lukamadness.madnesscore.common.slots.gui;

import dev.lukamadness.madnesscore.common.slots.SlotEquipLogic;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Marca un {@link net.minecraft.world.inventory.Slot} de GUI como un slot dinámico del sistema de
 * Madness Core (en contraposición a los slots vainilla de siempre-visibles). Implementado por
 * {@link PlayerDynamicSlot}. Portado de dev.emi.trinkets.TrinketSlot.
 */
public interface DynamicSlot {

    ResourceLocation madnesscore$getBackground();

    SlotType madnesscore$getType();

    static boolean canInsert(ItemStack stack, SlotReference ref, LivingEntity entity) {
        return SlotEquipLogic.canInsert(stack, ref, entity);
    }
}
