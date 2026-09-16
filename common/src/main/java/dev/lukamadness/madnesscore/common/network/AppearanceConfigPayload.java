package dev.lukamadness.madnesscore.common.network;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record AppearanceConfigPayload(
        UUID owner,
        int skinColor,
        int eyeOffsetX,
        int eyeOffsetY,
        int eyeWidth,
        int eyeHeight,
        int eyeColor,
        int scleraColor,
        int hairType,
        int hairColor,
        Map<String, byte[]> hairPixels,
        Map<String, byte[]> eyePixels,
        int hairColorMode,
        int eyeColorMode
) implements CustomPacketPayload {
    public static final Type<AppearanceConfigPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "appearance_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AppearanceConfigPayload> STREAM_CODEC = StreamCodec.of(
            AppearanceConfigPayload::write,
            AppearanceConfigPayload::read
    );

    private static void write(RegistryFriendlyByteBuf buf, AppearanceConfigPayload payload) {
        buf.writeUUID(payload.owner());
        buf.writeInt(payload.skinColor());
        buf.writeInt(payload.eyeOffsetX());
        buf.writeInt(payload.eyeOffsetY());
        buf.writeInt(payload.eyeWidth());
        buf.writeInt(payload.eyeHeight());
        buf.writeInt(payload.eyeColor());
        buf.writeInt(payload.scleraColor());
        buf.writeInt(payload.hairType());
        buf.writeInt(payload.hairColor());
        writePixelMap(buf, payload.hairPixels());
        writePixelMap(buf, payload.eyePixels());
        buf.writeInt(payload.hairColorMode());
        buf.writeInt(payload.eyeColorMode());
    }

    private static AppearanceConfigPayload read(RegistryFriendlyByteBuf buf) {
        UUID owner = buf.readUUID();
        int skinColor = buf.readInt();
        int eyeOffsetX = buf.readInt();
        int eyeOffsetY = buf.readInt();
        int eyeWidth = buf.readInt();
        int eyeHeight = buf.readInt();
        int eyeColor = buf.readInt();
        int scleraColor = buf.readInt();
        int hairType = buf.readInt();
        int hairColor = buf.readInt();
        Map<String, byte[]> hairPixels = readPixelMap(buf);
        Map<String, byte[]> eyePixels = readPixelMap(buf);
        int hairColorMode = buf.readInt();
        int eyeColorMode = buf.readInt();
        return new AppearanceConfigPayload(owner, skinColor, eyeOffsetX, eyeOffsetY, eyeWidth, eyeHeight,
                eyeColor, scleraColor, hairType, hairColor, hairPixels, eyePixels, hairColorMode, eyeColorMode);
    }

    private static void writePixelMap(RegistryFriendlyByteBuf buf, Map<String, byte[]> map) {
        buf.writeVarInt(map.size());
        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeByteArray(entry.getValue());
        }
    }

    private static Map<String, byte[]> readPixelMap(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, byte[]> map = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            byte[] value = buf.readByteArray();
            map.put(key, value);
        }
        return map;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}