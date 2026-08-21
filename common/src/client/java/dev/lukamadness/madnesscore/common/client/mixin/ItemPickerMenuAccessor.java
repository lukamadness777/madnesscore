package dev.lukamadness.madnesscore.common.client.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * CreativeModeInventoryScreen.ItemPickerMenu.items es package-private, así
 * que no se puede acceder directo desde nuestro paquete. Confirmado contra
 * mappings oficiales 1.21.1: el campo se llama "items" y es
 * final NonNullList<ItemStack>.
 */
@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public interface ItemPickerMenuAccessor {

    @Accessor("items")
    NonNullList<ItemStack> madnesscore$getItems();
}
