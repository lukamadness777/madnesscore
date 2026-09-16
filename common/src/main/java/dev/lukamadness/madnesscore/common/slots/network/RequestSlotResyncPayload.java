package dev.lukamadness.madnesscore.common.slots.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent client -> server when the client detects that its local slot layout no longer
 * matches what the server is sending (e.g. an out-of-range {@code ClientboundContainerSetSlotPacket}
 * or a content packet whose item count doesn't match the client's current slot count).
 * <p>
 * This is the "same fix" pattern Trinkets/Accessories already ship: instead of trusting
 * the desynced packet and crashing, the client asks the server for an authoritative
 * full resync of the dynamic slot layout.
 */
public record RequestSlotResyncPayload() implements CustomPacketPayload {
    public static final Type<RequestSlotResyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "request_slot_resync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSlotResyncPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestSlotResyncPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}