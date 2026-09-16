package dev.lukamadness.madnesscore.common.platform.services;

import dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public interface ISlotAttachment {
    LivingEntitySlotComponent getOrCreate(LivingEntity entity);

    Optional<LivingEntitySlotComponent> getIfPresent(LivingEntity entity);
}
