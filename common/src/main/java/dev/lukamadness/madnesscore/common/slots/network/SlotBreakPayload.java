package dev.lukamadness.madnesscore.common.slots.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.slots.Slottable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SlotBreakPayload(int entityId, String group, String slot, int index) implements CustomPacketPayload {
    public static final Type<SlotBreakPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "break_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotBreakPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SlotBreakPayload::entityId,
            ByteBufCodecs.STRING_UTF8, SlotBreakPayload::group,
            ByteBufCodecs.STRING_UTF8, SlotBreakPayload::slot,
            ByteBufCodecs.VAR_INT, SlotBreakPayload::index,
            SlotBreakPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
