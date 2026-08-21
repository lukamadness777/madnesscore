package dev.lukamadness.madnesscore.common.slots.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete servidor -> cliente con las *definiciones* de slots resueltas por tipo de entidad
 * (grupos, {@link SlotType}s, iconos, predicados,
 * etc.), NO el contenido equipado (eso ya lo cubre {@link SyncSlotComponentPayload}).
 * <p>
 * Hace falta porque el cliente no puede leer la carpeta {@code data/} de los data packs (el
 * {@code ReloadableResourceManager} del cliente solo ve {@code assets/}), asi que
 * {@code SlotsApi.getClientEntityLoader()} se queda vacio para siempre si nadie se lo manda por
 * red. Se envia en {@code ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS} (Fabric) /
 * {@code OnDatapackSyncEvent} (NeoForge), que cubren tanto el login de un jugador como un
 * {@code /reload}.
 * <p>
 * Reutiliza NBT (como {@link SyncSlotComponentPayload}) en vez de un StreamCodec campo por campo:
 * es un paquete infrecuente (no por-tick), asi que la simplicidad de {@code writeToNbt}/parseo
 * manual gana contra la ceremonia de serializar {@code SlotType} con StreamCodec.composite (mas
 * de 6 campos, no entra en los overloads estandar).
 */
public record SyncSlotDefinitionsPayload(CompoundTag data) implements CustomPacketPayload {

    public static final Type<SyncSlotDefinitionsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "sync_slot_definitions"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSlotDefinitionsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, SyncSlotDefinitionsPayload::data,
            SyncSlotDefinitionsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}