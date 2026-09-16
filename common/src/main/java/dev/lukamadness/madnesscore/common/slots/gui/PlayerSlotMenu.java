package dev.lukamadness.madnesscore.common.slots.gui;

import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PlayerSlotMenu {
    void madnesscore$updateSlots(boolean slotsChanged);

    int madnesscore$getGroupNum(SlotGroup group);

    @Nullable
    Point madnesscore$getGroupPos(SlotGroup group);

    @NotNull
    List<Point> madnesscore$getSlotHeights(SlotGroup group);

    @Nullable
    Point madnesscore$getSlotHeight(SlotGroup group, int i);

    @NotNull
    List<SlotType> madnesscore$getSlotTypes(SlotGroup group);

    int madnesscore$getSlotWidth(SlotGroup group);

    int madnesscore$getGroupCount();

    int madnesscore$getSlotRangeStart();

    int madnesscore$getSlotRangeEnd();
}
