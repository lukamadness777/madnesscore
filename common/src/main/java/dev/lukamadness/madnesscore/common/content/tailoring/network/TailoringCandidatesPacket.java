package dev.lukamadness.madnesscore.common.content.tailoring.network;

import java.util.List;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record TailoringCandidatesPacket(int containerId, List<ItemStack> results) implements CustomPacketPayload {
    public static final Type<TailoringCandidatesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "tailoring_candidates"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TailoringCandidatesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TailoringCandidatesPacket::containerId,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), TailoringCandidatesPacket::results,
            TailoringCandidatesPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
