package dev.lukamadness.madnesscore.common.mixin.accessor;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerMenu.class)
public interface ContainerMenuAccessor {
    @Accessor("lastSlots")
    NonNullList<ItemStack> madnesscore$getLastSlots();
}
