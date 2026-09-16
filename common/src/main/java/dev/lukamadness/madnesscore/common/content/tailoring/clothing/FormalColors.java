package dev.lukamadness.madnesscore.common.content.tailoring.clothing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FormalColors(int suitColor, int tieColor, int shirtColor, boolean tieVisible) {
    public static final FormalColors DEFAULT = new FormalColors(0x1E1E1E, 0x8B0000, 0xFFFFFF, true);

    public static final Codec<FormalColors> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("suit_color").forGetter(FormalColors::suitColor),
            Codec.INT.fieldOf("tie_color").forGetter(FormalColors::tieColor),
            Codec.INT.fieldOf("shirt_color").forGetter(FormalColors::shirtColor),
            Codec.BOOL.optionalFieldOf("tie_visible", true).forGetter(FormalColors::tieVisible)
    ).apply(instance, FormalColors::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FormalColors> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, FormalColors::suitColor,
            ByteBufCodecs.INT, FormalColors::tieColor,
            ByteBufCodecs.INT, FormalColors::shirtColor,
            ByteBufCodecs.BOOL, FormalColors::tieVisible,
            FormalColors::new
    );
}
