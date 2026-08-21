package dev.lukamadness.madnesscore.common.content.technology.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.Set;

/**
 * Clasifica el bioma de una posición para la pérdida ambiental de Heat, tal como se
 * definió en el diseño: Taiga (y biomas fríos/nevados en general) pierde más, Plains
 * (y cualquier bioma no listado) es la base, Desierto (y biomas cálidos/áridos) pierde
 * menos. Usado por HeatGeneratorBlockEntity para escalar tanto la pérdida de amount
 * como la de temperature.
 */
public final class HeatEnvironment {

    private HeatEnvironment() {}

    public static final double TAIGA_LOSS_MULTIPLIER = 1.5;
    public static final double PLAINS_LOSS_MULTIPLIER = 1.0;
    public static final double DESERT_LOSS_MULTIPLIER = 0.5;

    /**
     * Pérdida de temperatura base (°C/tick, ritmo Plains) antes de aplicar el
     * multiplicador de bioma. Compartida por cualquier bloque con temperatura propia
     * (HeatGeneratorBlockEntity, AlloySmelteryBlockEntity y futuras mesas) para que
     * todas enfríen al mismo ritmo salvo que definan una razón propia.
     */
    public static final double BASE_TEMPERATURE_LOSS_PER_TICK = 0.05; // ~1°C/seg a ritmo Plains

    private static final Set<ResourceKey<Biome>> COLD_BIOMES = Set.of(
            Biomes.TAIGA, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA,
            Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.FROZEN_PEAKS, Biomes.SNOWY_SLOPES,
            Biomes.GROVE, Biomes.FROZEN_RIVER, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN
    );

    private static final Set<ResourceKey<Biome>> HOT_DRY_BIOMES = Set.of(
            Biomes.DESERT, Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS,
            Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.NETHER_WASTES
    );

    /** Multiplicador de pérdida ambiental para la posición dada (1.0 = ritmo Plains). */
    public static double lossMultiplier(Level level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        return biome.unwrapKey()
                .map(HeatEnvironment::multiplierFor)
                .orElse(PLAINS_LOSS_MULTIPLIER);
    }

    private static double multiplierFor(ResourceKey<Biome> key) {
        if (COLD_BIOMES.contains(key)) return TAIGA_LOSS_MULTIPLIER;
        if (HOT_DRY_BIOMES.contains(key)) return DESERT_LOSS_MULTIPLIER;
        return PLAINS_LOSS_MULTIPLIER;
    }
}