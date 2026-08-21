package dev.lukamadness.madnesscore.common.client.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Confirmado contra mappings oficiales 1.21.1: el campo estático que guarda
 * el CreativeModeTab actualmente seleccionado se llama "selectedTab".
 * Si en otra versión de Minecraft el nombre cambia, este es el único lugar
 * a tocar.
 */
@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenAccessor {

    @Accessor("selectedTab")
    static CreativeModeTab madnesscore$getSelectedTab() {
        throw new AssertionError();
    }
}
