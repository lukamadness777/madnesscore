package dev.lukamadness.madnesscore.common.api.identity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Guarda/lee/alterna un flag booleano simple dentro del {@link DataComponents#CUSTOM_DATA} de un
 * ItemStack. Pensado para cosas como el "¿tengo la capucha puesta?" de una prenda: en vez de que cada
 * item reimplemente a mano el get/set de NBT (como hacía {@code HoodieItem} y {@code GreenCloakItem}
 * en el beyond-the-sea viejo, con código duplicado), se centraliza acá una sola vez.
 */
public final class ItemToggleFlag {
    private ItemToggleFlag() {
    }

    public static boolean get(ItemStack stack, String key) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBoolean(key);
    }

    public static void set(ItemStack stack, String key, boolean value) {
        CompoundTag tag = stack.has(DataComponents.CUSTOM_DATA)
                ? stack.get(DataComponents.CUSTOM_DATA).copyTag()
                : new CompoundTag();
        tag.putBoolean(key, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void toggle(ItemStack stack, String key) {
        set(stack, key, !get(stack, key));
    }
}
