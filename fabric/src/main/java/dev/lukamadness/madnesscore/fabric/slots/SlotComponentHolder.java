package dev.lukamadness.madnesscore.fabric.slots;

import dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent;

/**
 * Interfaz "duck" implementada por el mixin sobre {@code LivingEntity} para exponer el campo de
 * slots inyectado. A diferencia del Trinkets original (que depende de Cardinal Components API,
 * exclusivo de Fabric), esto es un campo plano inyectado directamente: mas simple, y portable en
 * principio a cualquier loader que soporte mixins.
 */
public interface SlotComponentHolder {

    /**
     * @return el componente de slots de esta entidad, creandolo perezosamente si todavia no existe.
     */
    LivingEntitySlotComponent madnesscore$getSlotComponent();

    void madnesscore$setSlotComponent(LivingEntitySlotComponent component);
}
