package dev.lukamadness.madnesscore.fabric.slots;

import dev.lukamadness.madnesscore.common.platform.services.ISlotAttachment;
import dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * Implementacion Fabric de {@link ISlotAttachment}: delega en la interfaz duck
 * ({@link SlotComponentHolder}) que {@code MixinLivingEntitySlots} inyecta sobre toda
 * {@link LivingEntity}.
 */
public class FabricSlotAttachment implements ISlotAttachment {

    @Override
    public LivingEntitySlotComponent getOrCreate(LivingEntity entity) {
        return ((SlotComponentHolder) entity).madnesscore$getSlotComponent();
    }

    @Override
    public Optional<LivingEntitySlotComponent> getIfPresent(LivingEntity entity) {
        // El campo se crea perezosamente pero siempre queda disponible una vez pedido; como el
        // mixin esta en toda LivingEntity, "presente" aca simplemente significa "invocable".
        return Optional.of(getOrCreate(entity));
    }
}
