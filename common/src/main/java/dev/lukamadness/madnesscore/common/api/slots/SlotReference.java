package dev.lukamadness.madnesscore.common.api.slots;

public record SlotReference(SlotInventory inventory, int index) {
    public String getId() {
        return this.inventory.getSlotType().getId() + "/" + index;
    }
}
