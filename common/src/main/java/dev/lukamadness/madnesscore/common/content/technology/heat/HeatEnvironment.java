package dev.lukamadness.madnesscore.common.content.technology.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.Set;

public final class HeatEnvironment {
    private HeatEnvironment() {}

    public static final double TAIGA_LOSS_MULTIPLIER = 1.5;
    public static final double PLAINS_LOSS_MULTIPLIER = 1.0;
    public static final double DESERT_LOSS_MULTIPLIER = 0.5;

    public static final double TAIGA_HEATING_MULTIPLIER = 0.5;
    public static final double PLAINS_HEATING_MULTIPLIER = 1.0;
    public static final double DESERT_HEATING_MULTIPLIER = 1.5;

    public static final double BASE_TEMPERATURE_LOSS_RATE = 0.01;

    private static final Set<ResourceKey<Biome>> COLD_BIOMES = Set.of(
            Biomes.TAIGA, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA,
            Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.FROZEN_PEAKS, Biomes.SNOWY_SLOPES,
            Biomes.GROVE, Biomes.FROZEN_RIVER, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN
    );

    private static final Set<ResourceKey<Biome>> HOT_DRY_BIOMES = Set.of(
            Biomes.DESERT, Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS,
            Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU
    );

    public static double lossMultiplier(Level level, BlockPos pos) {
        if (isNether(level)) return DESERT_LOSS_MULTIPLIER;
        Holder<Biome> biome = level.getBiome(pos);
        return biome.unwrapKey()
                .map(key -> multiplierFor(key, COLD_BIOMES, TAIGA_LOSS_MULTIPLIER, DESERT_LOSS_MULTIPLIER, PLAINS_LOSS_MULTIPLIER))
                .orElse(PLAINS_LOSS_MULTIPLIER);
    }

    public static double heatGainMultiplier(Level level, BlockPos pos) {
        if (isNether(level)) return DESERT_HEATING_MULTIPLIER;
        Holder<Biome> biome = level.getBiome(pos);
        return biome.unwrapKey()
                .map(key -> multiplierFor(key, COLD_BIOMES, TAIGA_HEATING_MULTIPLIER, DESERT_HEATING_MULTIPLIER, PLAINS_HEATING_MULTIPLIER))
                .orElse(PLAINS_HEATING_MULTIPLIER);
    }

    private static boolean isNether(Level level) {
        return level.dimension() == Level.NETHER;
    }

    private static double multiplierFor(ResourceKey<Biome> key, Set<ResourceKey<Biome>> coldSet,
                                         double coldValue, double hotValue, double baseValue) {
        if (coldSet.contains(key)) return coldValue;
        if (HOT_DRY_BIOMES.contains(key)) return hotValue;
        return baseValue;
    }
}
