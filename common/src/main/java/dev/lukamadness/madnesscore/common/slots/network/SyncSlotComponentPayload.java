package dev.lukamadness.madnesscore.common.slots.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncSlotComponentPayload(int entityId, CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncSlotComponentPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "sync_slots"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSlotComponentPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncSlotComponentPayload::entityId,
            ByteBufCodecs.COMPOUND_TAG, SyncSlotComponentPayload::data,
            SyncSlotComponentPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
