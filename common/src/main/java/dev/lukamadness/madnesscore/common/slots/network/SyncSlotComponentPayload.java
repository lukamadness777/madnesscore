package dev.lukamadness.madnesscore.common.slots.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete servidor -> cliente (Fase 4) con el estado completo de los slots de una entidad, para
 * que cualquier observador que la este viendo (no solo el dueno del inventario) pueda actualizar
 * el render de los items equipados. Reutiliza directamente {@code SlotComponent#writeToNbt} /
 * {@code #readFromNbt} (ya implementados desde la Fase 2), asi que no hace falta un formato de
 * red aparte.
 * <p>
 * Es un record {@link CustomPacketPayload} vanilla (100% comun): el registro concreto del canal
 * (Fabric: {@code PayloadTypeRegistry}; NeoForge: {@code RegisterPayloadHandlersEvent}) es lo
 * unico que varia por loader.
 */
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