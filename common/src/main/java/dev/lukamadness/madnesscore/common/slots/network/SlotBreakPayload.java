package dev.lukamadness.madnesscore.common.slots.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.slots.Slottable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Paquete servidor -> cliente que notifica que el item equipado en un slot dinamico puntual
 * (identificado por entidad + grupo + nombre de slot + indice) acaba de romperse (llegar a 0 de
 * durabilidad), para que el cliente pueda reproducir el efecto de rotura (sonido + particulas) via
 * {@link Slottable#onBreak}.
 * <p>
 * Portado de dev.emi.trinkets.payload.BreakPayload. A diferencia de {@link SyncSlotComponentPayload}
 * (que sincroniza contenido/atributos), este paquete es "fire and forget": no toca el estado del
 * inventario, solo dispara el efecto visual/sonoro una vez.
 */
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
