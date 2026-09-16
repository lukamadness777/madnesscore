package dev.lukamadness.madnesscore.common.client.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int madnesscore$getLeftPos();

    @Accessor("topPos")
    int madnesscore$getTopPos();

    @Accessor("imageWidth")
    int madnesscore$getImageWidth();

    @Accessor("imageHeight")
    int madnesscore$getImageHeight();

    @Accessor("hoveredSlot")
    Slot madnesscore$accessorHoveredSlot();

    @Accessor("menu")
    AbstractContainerMenu madnesscore$getMenu();
}
