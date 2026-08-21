package dev.lukamadness.madnesscore.common.api.slots.event;

import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Callback invocado despues de que un item queda equipado en un slot. Registrar listeners con
 * {@link #EVENT}.addListener(...). Portado de dev.emi.trinkets.api.event.TrinketEquipCallback.
 */
@FunctionalInterface
public interface SlotEquipCallback {

    SlotEventBus<SlotEquipCallback> EVENT = new SlotEventBus<>(listeners -> (stack, slot, entity) -> {
        for (SlotEquipCallback listener : listeners) {
            listener.onEquip(stack, slot, entity);
        }
    });

    void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity);
}