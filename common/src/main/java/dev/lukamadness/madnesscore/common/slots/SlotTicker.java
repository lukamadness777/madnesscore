package dev.lukamadness.madnesscore.common.slots;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.lukamadness.madnesscore.common.api.slots.*;
import dev.lukamadness.madnesscore.common.api.slots.event.SlotEquipCallback;
import dev.lukamadness.madnesscore.common.api.slots.event.SlotUnequipCallback;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SlotTicker {
    private SlotTicker() {
    }

    public static void tick(LivingEntity entity) {
        if (entity.isRemoved()) {
            return;
        }

        SlotsApi.getSlotComponent(entity).ifPresent(component -> {
            if (!(component instanceof LivingEntitySlotComponent slots)) {
                return;
            }

            Map<String, ItemStack> newlyEquipped = new HashMap<>();
            boolean[] changedOnServer = {false};
            boolean resyncFromLoad = slots.justLoaded;
            slots.justLoaded = false;

            slots.forEach((ref, newStack) -> {
                SlotInventory inventory = ref.inventory();
                SlotType slotType = inventory.getSlotType();
                int index = ref.index();
                String key = slotType.getGroup() + "/" + slotType.getName() + "/" + index;
                ItemStack oldStack = slots.lastEquipped.getOrDefault(key, ItemStack.EMPTY);
                ItemStack newStackCopy = newStack.copy();

                if (!ItemStack.matches(newStack, oldStack)) {
                    // Un ítem equipado que muta sus propios components tick a tick (ej. el gas
                    // del ODM) hace que ItemStack.matches falle en cada tick sin que haya habido
                    // un equip/unequip real. La rama de arriba (onEquip/onUnequip, modifiers)
                    // sigue disparando en cualquier mutación -- eso ya funcionaba así -- pero el
                    // sonido de equipar debe sonar solo cuando el ítem realmente cambió, no en
                    // cada mutación de sus components.
                    boolean realEquipChange = oldStack.isEmpty() != newStack.isEmpty()
                            || oldStack.getItem() != newStack.getItem();

                    SlotsApi.getSlottable(oldStack.getItem()).onUnequip(oldStack, ref, entity);
                    SlotUnequipCallback.EVENT.invoker().onUnequip(oldStack, ref, entity);
                    SlotsApi.getSlottable(newStack.getItem()).onEquip(newStack, ref, entity);
                    SlotEquipCallback.EVENT.invoker().onEquip(newStack, ref, entity);

                    if (!entity.level().isClientSide()) {
                        boolean oldMirrored = !oldStack.isEmpty() && isMirroredToVanilla(entity, oldStack);
                        boolean newMirrored = !newStack.isEmpty() && isMirroredToVanilla(entity, newStack);

                        if (!oldStack.isEmpty() && !oldMirrored) {
                            removeSlotModifiers(entity, slots, oldStack, ref);
                        }
                        if (!newStack.isEmpty() && !newMirrored) {
                            addSlotModifiers(entity, slots, newStack, ref);
                        }
                        if (!newStack.isEmpty() && !resyncFromLoad && realEquipChange) {
                            SlotsApi.getSlottable(newStack.getItem()).getEquipSound(newStack, ref, entity)
                                    .ifPresent(sound -> entity.level().playSound(null,
                                            entity.getX(), entity.getY(), entity.getZ(),
                                            sound.value(), entity.getSoundSource(), 1.0F, 1.0F));
                        }

                        changedOnServer[0] = true;
                    }
                }

                SlotsApi.getSlottable(newStack.getItem()).tick(newStack, ref, entity);

                ItemStack tickedStack = inventory.getItem(index);
                newlyEquipped.put(key, tickedStack.getItem() == newStackCopy.getItem() ? tickedStack.copy() : newStackCopy);
            });

            slots.lastEquipped.clear();
            slots.lastEquipped.putAll(newlyEquipped);

            if (changedOnServer[0]) {
                SlotNetworking.syncToTrackers(entity);
            }
        });
    }

    private static boolean isMirroredToVanilla(LivingEntity entity, ItemStack stack) {
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            if (entity.getItemBySlot(slot) == stack) {
                return true;
            }
        }
        return false;
    }

    private static void removeSlotModifiers(LivingEntity entity, LivingEntitySlotComponent slots, ItemStack stack, SlotReference ref) {
        Multimap<Holder<Attribute>, AttributeModifier> map = SlotEquipLogic.getModifiers(stack, ref, entity);
        Multimap<String, AttributeModifier> slotMap = extractSlotCountModifiers(map);

        AttributeMap attributes = entity.getAttributes();
        map.asMap().forEach((attribute, modifiers) -> {
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance != null) {
                modifiers.forEach(modifier -> instance.removeModifier(modifier.id()));
            }
        });

        slots.removeModifiers(slotMap);
    }

    private static void addSlotModifiers(LivingEntity entity, LivingEntitySlotComponent slots, ItemStack stack, SlotReference ref) {
        Multimap<Holder<Attribute>, AttributeModifier> map = SlotEquipLogic.getModifiers(stack, ref, entity);
        Multimap<String, AttributeModifier> slotMap = extractSlotCountModifiers(map);

        AttributeMap attributes = entity.getAttributes();
        map.forEach((attribute, modifier) -> {
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance != null) {
                instance.removeModifier(modifier.id());
                instance.addTransientModifier(modifier);
            }
        });

        slots.addTemporaryModifiers(slotMap);
    }

    private static Multimap<String, AttributeModifier> extractSlotCountModifiers(Multimap<Holder<Attribute>, AttributeModifier> map) {
        Multimap<String, AttributeModifier> slotMap = HashMultimap.create();
        Set<Holder<Attribute>> toRemove = new HashSet<>();
        for (Holder<Attribute> attribute : map.keySet()) {
            if (attribute.value() instanceof SlotAttributes.SlotEntityAttribute slotAttribute) {
                slotMap.putAll(slotAttribute.slot, map.get(attribute));
                toRemove.add(attribute);
            }
        }
        for (Holder<Attribute> attribute : toRemove) {
            map.removeAll(attribute);
        }
        return slotMap;
    }
}
