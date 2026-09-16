package dev.lukamadness.madnesscore.common.client.slots.ui;

import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;

public final class SlotUiState {
    private SlotUiState() {
    }

    public static SlotGroup activeGroup;
    public static SlotType activeType;

    public static void clear() {
        activeGroup = null;
        activeType = null;
    }
}
