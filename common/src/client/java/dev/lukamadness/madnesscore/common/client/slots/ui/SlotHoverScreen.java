package dev.lukamadness.madnesscore.common.client.slots.ui;

import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;

public interface SlotHoverScreen {
    PlayerSlotMenu madnesscore$getMenu();

    Rect2i madnesscore$getGroupRect(SlotGroup group);

    Slot madnesscore$getHoveredSlot();

    int madnesscore$getX();

    int madnesscore$getY();

    default boolean madnesscore$isRecipeBookOpen() {
        return false;
    }
}
