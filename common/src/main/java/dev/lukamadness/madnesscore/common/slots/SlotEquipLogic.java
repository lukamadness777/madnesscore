package dev.lukamadness.madnesscore.common.slots;

import com.google.common.collect.Multimap;
import dev.lukamadness.madnesscore.common.api.slots.SlotAttributes;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import dev.lukamadness.madnesscore.common.api.slots.Slottable;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * Logica compartida de validacion y calculo de modificadores para equipar un item en un slot.
 * Portado de dev.emi.trinkets.TrinketSlot#canInsert y dev.emi.trinkets.TrinketModifiers.
 */
public final class SlotEquipLogic {

    private SlotEquipLogic() {
    }

    /**
     * @return si el stack puede insertarse en el slot indicado: primero se evaluan los
     * "validator_predicates" data-driven del {@link SlotType},
     * y si aprueban, se consulta ademas {@link Slottable#canEquip}.
     */
    public static boolean canInsert(ItemStack stack, SlotReference slotRef, LivingEntity entity) {
        boolean predicateResult = SlotsApi.evaluatePredicateSet(
                slotRef.inventory().getSlotType().getValidatorPredicates(), stack, slotRef, entity);

        if (predicateResult) {
            return SlotsApi.getSlottable(stack.getItem()).canEquip(stack, slotRef, entity);
        }

        return false;
    }

    /**
     * @return los modificadores de atributo que aporta un stack equipado en un slot (delega en el
     * {@link Slottable} registrado para el item).
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return SlotsApi.getSlottable(stack.getItem()).getModifiers(stack, slot, entity, SlotAttributes.getIdentifier(slot));
    }
}