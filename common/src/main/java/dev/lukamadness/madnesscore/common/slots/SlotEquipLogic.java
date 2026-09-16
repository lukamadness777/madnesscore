package dev.lukamadness.madnesscore.common.slots;

import com.google.common.collect.Multimap;
import dev.lukamadness.madnesscore.common.api.slots.*;
import dev.lukamadness.madnesscore.common.registry.component.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public final class SlotEquipLogic {
    private SlotEquipLogic() {
    }

    public static boolean canInsert(ItemStack stack, SlotReference slotRef, LivingEntity entity) {
        if (!madnesscore$passesVanillaMirrorExclusivity(stack, slotRef, entity)) {
            return false;
        }

        boolean predicateResult = SlotsApi.evaluatePredicateSet(
                slotRef.inventory().getSlotType().getValidatorPredicates(), stack, slotRef, entity);

        if (predicateResult) {
            return SlotsApi.getSlottable(stack.getItem()).canEquip(stack, slotRef, entity);
        }

        return false;
    }

    /**
     * Si este slot espeja un EquipmentSlot vanilla (mirror_vanilla_equipment: true), no deja
     * insertar acá mientras el slot vanilla real ya tenga puesta otra cosa distinta a lo que ya
     * hay en este slot MC. Así se evita terminar con una pieza en el slot vanilla real y otra
     * distinta acá al mismo tiempo.
     */
    private static boolean madnesscore$passesVanillaMirrorExclusivity(ItemStack stack, SlotReference slotRef, LivingEntity entity) {
        SlotInventory inv = slotRef.inventory();
        if (!inv.getSlotType().mirrorsVanillaEquipment()) {
            return true;
        }

        return SlotsApi.getSlotComponent(entity)
                .flatMap(component -> VanillaEquipmentMirror.resolveEquipmentSlot(component, inv))
                .map(equipmentSlot -> {
                    ItemStack current = inv.getItem(slotRef.index());
                    ItemStack merged = entity.getItemBySlot(equipmentSlot);
                    return merged.isEmpty() || ItemStack.matches(merged, current);
                })
                .orElse(true);
    }

    public static Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity) {
        Multimap<Holder<Attribute>, AttributeModifier> map = SlotsApi.getSlottable(stack.getItem())
                .getModifiers(stack, slot, entity, SlotAttributes.getIdentifier(slot));

        if (stack.has(ModDataComponents.SLOT_ATTRIBUTE_MODIFIERS.get())) {
            String slotId = slot.inventory().getSlotType().getId();
            SlotAttributeModifiersComponent component = stack.getOrDefault(
                    ModDataComponents.SLOT_ATTRIBUTE_MODIFIERS.get(), SlotAttributeModifiersComponent.DEFAULT);

            for (SlotAttributeModifiersComponent.Entry entry : component.modifiers()) {
                if (entry.slot().isEmpty() || entry.slot().get().equals(slotId)) {
                    map.put(entry.attribute(), entry.modifier());
                }
            }
        }

        return map;
    }
}
