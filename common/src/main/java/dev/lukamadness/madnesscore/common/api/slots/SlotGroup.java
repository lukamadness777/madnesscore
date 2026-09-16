package dev.lukamadness.madnesscore.common.api.slots;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public final class SlotGroup {
    private final String name;
    private final int slotId;
    private final int order;
    private final Map<String, SlotType> slots;

    private SlotGroup(Builder builder) {
        this.name = builder.name;
        this.slotId = builder.slotId;
        this.order = builder.order;
        this.slots = builder.slots;
    }

    public String getName() {
        return name;
    }

    public int getSlotId() {
        return slotId;
    }

    public int getOrder() {
        return order;
    }

    public Map<String, SlotType> getSlots() {
        return ImmutableMap.copyOf(slots);
    }

    public static final class Builder {
        private final String name;
        private final int slotId;
        private final int order;
        private final Map<String, SlotType> slots = new HashMap<>();

        public Builder(String name, int slotId, int order) {
            this.name = name;
            this.slotId = slotId;
            this.order = order;
        }

        public Builder addSlot(String name, SlotType slot) {
            this.slots.put(name, slot);
            return this;
        }

        public SlotGroup build() {
            return new SlotGroup(this);
        }
    }
}
