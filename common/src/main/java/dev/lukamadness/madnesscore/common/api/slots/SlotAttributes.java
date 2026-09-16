package dev.lukamadness.madnesscore.common.api.slots;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;

public final class SlotAttributes {
    private static final Map<String, ResourceLocation> CACHED_IDS = Maps.newHashMap();
    private static final Map<String, Holder<Attribute>> CACHED_ATTRIBUTES = Maps.newHashMap();

    private SlotAttributes() {
    }

    public static void addSlotModifier(Multimap<Holder<Attribute>, AttributeModifier> map, String slot, ResourceLocation identifier, double amount,
                                       AttributeModifier.Operation operation) {
        CACHED_ATTRIBUTES.computeIfAbsent(slot, s -> Holder.direct(new SlotEntityAttribute(s)));
        map.put(CACHED_ATTRIBUTES.get(slot), new AttributeModifier(identifier, amount, operation));
    }

    public static ResourceLocation getIdentifier(SlotReference ref) {
        String key = ref.getId();
        return CACHED_IDS.computeIfAbsent(key, k -> ResourceLocation.parse(
                dev.lukamadness.madnesscore.common.MadnessCoreCommon.MOD_ID + ":slot_ref/" + k.replace("/", "_")));
    }

    public static class SlotEntityAttribute extends Attribute {
        public final String slot;

        private SlotEntityAttribute(String slot) {
            super("madnesscore.slot." + slot.replace("/", "."), 0);
            this.slot = slot;
        }
    }
}
