package dev.lukamadness.madnesscore.fabric.slots;

import dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent;

public interface SlotComponentHolder {
    LivingEntitySlotComponent madnesscore$getSlotComponent();

    void madnesscore$setSlotComponent(LivingEntitySlotComponent component);
}
