package dev.lukamadness.madnesscore.common.content.tailoring;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record TailoringColors(List<Integer> colors) {
    public static final TailoringColors EMPTY = new TailoringColors(List.of());

    public int get(int index, int fallback) {
        return index >= 0 && index < colors.size() ? colors.get(index) : fallback;
    }

    public int primary() {
        return get(0, 0xFFFFFF);
    }

    public static final Codec<TailoringColors> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.listOf().fieldOf("colors").forGetter(TailoringColors::colors)
    ).apply(instance, TailoringColors::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TailoringColors> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.INT), TailoringColors::colors,
            TailoringColors::new
    );
}
