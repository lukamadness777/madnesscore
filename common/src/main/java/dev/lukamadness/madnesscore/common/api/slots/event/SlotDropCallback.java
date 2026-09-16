package dev.lukamadness.madnesscore.common.api.slots.event;

import dev.lukamadness.madnesscore.common.api.slots.DropRule;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface SlotDropCallback {
    SlotEventBus<SlotDropCallback> EVENT = new SlotEventBus<>(listeners -> (rule, stack, slot, entity) -> {
        DropRule result = rule;
        for (SlotDropCallback listener : listeners) {
            result = listener.drop(result, stack, slot, entity);
        }
        return result;
    });

    DropRule drop(DropRule currentRule, ItemStack stack, SlotReference slot, LivingEntity entity);
}
