package dev.lukamadness.madnesscore.common.slots;

import dev.lukamadness.madnesscore.common.api.slots.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

public final class VanillaEquipmentMirror {
    private static final Map<Integer, EquipmentSlot> VANILLA_ARMOR_SLOT_IDS = Map.of(
            5, EquipmentSlot.HEAD,
            6, EquipmentSlot.CHEST,
            7, EquipmentSlot.LEGS,
            8, EquipmentSlot.FEET
    );

    private VanillaEquipmentMirror() {
    }

    public static ItemStack getMirroredStack(LivingEntity entity, EquipmentSlot equipmentSlot) {
        if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            return ItemStack.EMPTY;
        }

        Optional<SlotComponent> componentOpt = SlotsApi.getSlotComponent(entity);
        if (componentOpt.isEmpty()) {
            return ItemStack.EMPTY;
        }
        SlotComponent component = componentOpt.get();

        for (Map.Entry<String, SlotGroup> groupEntry : component.getGroups().entrySet()) {
            SlotGroup group = groupEntry.getValue();

            if (VANILLA_ARMOR_SLOT_IDS.get(group.getSlotId()) != equipmentSlot) {
                continue;
            }

            Map<String, SlotInventory> groupInventory = component.getInventory().get(groupEntry.getKey());
            if (groupInventory == null) {
                continue;
            }

            for (SlotInventory inv : groupInventory.values()) {
                if (!inv.getSlotType().mirrorsVanillaEquipment()) {
                    continue;
                }
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    Equipable equipable = Equipable.get(stack);
                    if (equipable != null && equipable.getEquipmentSlot() == equipmentSlot) {
                        return stack;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static Optional<EquipmentSlot> resolveEquipmentSlot(SlotComponent component, SlotInventory target) {
        for (Map.Entry<String, SlotGroup> groupEntry : component.getGroups().entrySet()) {
            SlotGroup group = groupEntry.getValue();
            EquipmentSlot equipmentSlot = VANILLA_ARMOR_SLOT_IDS.get(group.getSlotId());
            if (equipmentSlot == null) {
                continue;
            }

            Map<String, SlotInventory> groupInventory = component.getInventory().get(groupEntry.getKey());
            if (groupInventory == null) {
                continue;
            }

            for (SlotInventory inv : groupInventory.values()) {
                if (inv == target) {
                    return Optional.of(equipmentSlot);
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isMirrored(LivingEntity entity, ItemStack stack) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (entity.getItemBySlot(slot) == stack) {
                return true;
            }
        }
        return false;
    }
}