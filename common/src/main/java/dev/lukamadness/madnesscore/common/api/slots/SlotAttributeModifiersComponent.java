package dev.lukamadness.madnesscore.common.api.slots;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.Optional;

public record SlotAttributeModifiersComponent(List<Entry> modifiers, boolean showInTooltip) {
    public static final SlotAttributeModifiersComponent DEFAULT = new SlotAttributeModifiersComponent(List.of(), true);

    private static final Codec<SlotAttributeModifiersComponent> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("modifiers").forGetter(SlotAttributeModifiersComponent::modifiers),
            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(SlotAttributeModifiersComponent::showInTooltip)
    ).apply(instance, SlotAttributeModifiersComponent::new));

    public static final Codec<SlotAttributeModifiersComponent> CODEC = Codec.withAlternative(
            BASE_CODEC, Entry.CODEC.listOf(), modifiers -> new SlotAttributeModifiersComponent(modifiers, true));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotAttributeModifiersComponent> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SlotAttributeModifiersComponent::modifiers,
            ByteBufCodecs.BOOL,
            SlotAttributeModifiersComponent::showInTooltip,
            SlotAttributeModifiersComponent::new);

    public SlotAttributeModifiersComponent withShowInTooltip(boolean showInTooltip) {
        return new SlotAttributeModifiersComponent(this.modifiers, showInTooltip);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmutableList.Builder<Entry> entries = ImmutableList.builder();

        Builder() {
        }

        public Builder add(Holder<Attribute> attribute, AttributeModifier modifier) {
            return add(attribute, modifier, Optional.empty());
        }

        public Builder add(Holder<Attribute> attribute, AttributeModifier modifier, String slot) {
            return add(attribute, modifier, Optional.of(slot));
        }

        public Builder add(Holder<Attribute> attribute, AttributeModifier modifier, Optional<String> slot) {
            this.entries.add(new Entry(attribute, modifier, slot));
            return this;
        }

        public SlotAttributeModifiersComponent build() {
            return new SlotAttributeModifiersComponent(this.entries.build(), true);
        }
    }

    public record Entry(Holder<Attribute> attribute, AttributeModifier modifier, Optional<String> slot) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Attribute.CODEC.fieldOf("type").forGetter(Entry::attribute),
                AttributeModifier.MAP_CODEC.forGetter(Entry::modifier),
                Codec.STRING.optionalFieldOf("slot").forGetter(Entry::slot)
        ).apply(instance, Entry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                Attribute.STREAM_CODEC, Entry::attribute,
                AttributeModifier.STREAM_CODEC, Entry::modifier,
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), Entry::slot,
                Entry::new);
    }
}
