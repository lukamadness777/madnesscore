package dev.lukamadness.madnesscore.common.api.slots;

/**
 * Referencia inmutable a un slot concreto: el {@link SlotInventory} al que pertenece y el indice
 * dentro de ese inventario (un SlotType puede tener mas de un slot fisico via "amount").
 * Portado de dev.emi.trinkets.api.SlotReference.
 */
public record SlotReference(SlotInventory inventory, int index) {

    public String getId() {
        return this.inventory.getSlotType().getId() + "/" + index;
    }
}