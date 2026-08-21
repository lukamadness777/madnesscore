package dev.lukamadness.madnesscore.neoforge.slots;

import dev.lukamadness.madnesscore.common.platform.services.ISlotAttachment;
import dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * Implementacion NeoForge de {@link ISlotAttachment} sobre {@link SlotAttachments#SLOT_COMPONENT}.
 * {@code getData} ya crea el valor por defecto (via el constructor pasado a
 * {@code AttachmentType.builder}) la primera vez que se pide, asi que "getOrCreate" es directo.
 */
public class NeoForgeSlotAttachment implements ISlotAttachment {

    @Override
    public LivingEntitySlotComponent getOrCreate(LivingEntity entity) {
        return entity.getData(SlotAttachments.SLOT_COMPONENT.get());
    }

    @Override
    public Optional<LivingEntitySlotComponent> getIfPresent(LivingEntity entity) {
        if (entity.hasData(SlotAttachments.SLOT_COMPONENT.get())) {
            return Optional.of(entity.getData(SlotAttachments.SLOT_COMPONENT.get()));
        }
        return Optional.empty();
    }
}
