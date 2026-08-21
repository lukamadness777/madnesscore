package dev.lukamadness.madnesscore.common.api.slots.event;

import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Callback invocado despues de que un item queda desequipado de un slot.
 * Portado de dev.emi.trinkets.api.event.TrinketUnequipCallback.
 */
@FunctionalInterface
public interface SlotUnequipCallback {

    SlotEventBus<SlotUnequipCallback> EVENT = new SlotEventBus<>(listeners -> (stack, slot, entity) -> {
        for (SlotUnequipCallback listener : listeners) {
            listener.onUnequip(stack, slot, entity);
        }
    });

    void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity);
}