package dev.lukamadness.madnesscore.common.network;

import java.util.List;
import java.util.Set;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record UpdateDimensionsPacket(Set<ResourceKey<Level>> keys, boolean add) implements CustomPacketPayload
{
	public static final Type<UpdateDimensionsPacket> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "update_dimensions"));

	public static final StreamCodec<ByteBuf, UpdateDimensionsPacket> STREAM_CODEC = StreamCodec.composite(
			ResourceKey.streamCodec(Registries.DIMENSION).apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), UpdateDimensionsPacket::keys,
			ByteBufCodecs.BOOL, UpdateDimensionsPacket::add,
			UpdateDimensionsPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
