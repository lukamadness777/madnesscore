package dev.lukamadness.madnesscore.fabric.slots;

import dev.lukamadness.madnesscore.common.platform.services.ISlotAttachment;
import dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class FabricSlotAttachment implements ISlotAttachment {
    @Override
    public LivingEntitySlotComponent getOrCreate(LivingEntity entity) {
        return ((SlotComponentHolder) entity).madnesscore$getSlotComponent();
    }

    @Override
    public Optional<LivingEntitySlotComponent> getIfPresent(LivingEntity entity) {
        return Optional.of(getOrCreate(entity));
    }
}
