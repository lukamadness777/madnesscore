package dev.lukamadness.madnesscore.common.impact.material;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/**
 * MadnessCore's own built-in {@link ImpactMaterial}s. These exist as sane, ready-to-use defaults
 * (mirroring the examples MadnessCore is expected to support - blood, generic fluid, tissue, bone
 * dust, oil, energy) - they are NOT a closed set. Any other mod is free to register additional
 * materials of its own via {@link ImpactMaterialRegistry#register(ImpactMaterial)}.
 * <p>
 * The entity -> material wiring for vanilla mobs lives in {@link ImpactMobBindings}, not here -
 * this class only builds/registers the materials themselves.
 */
public final class ImpactMaterials {

    // --- Generic defaults (pre-existing, unchanged) ---

    public static final ImpactMaterial BLOOD = build("blood", ImpactMaterialCategory.BLOOD,
            0xFFB30000, 0xB30000,
            ImpactEmissionProfile.DEFAULT);

    public static final ImpactMaterial FLUID = build("fluid", ImpactMaterialCategory.FLUID,
            0xFF7A2BC9, 0x7A2BC9,
            ImpactEmissionProfile.builder().count(8, 12).spread(0.25F).horizontalSpeed(0.18F).verticalSpeed(0.0F, 0.25F).build());

    public static final ImpactMaterial TISSUE = build("tissue", ImpactMaterialCategory.TISSUE,
            0xFF8C8C7A, 0x8C8C7A,
            ImpactEmissionProfile.builder().count(6, 10).spread(0.3F).horizontalSpeed(0.2F).verticalSpeed(0.05F, 0.35F).build());

    public static final ImpactMaterial BONE = build("bone", ImpactMaterialCategory.BONE,
            0xFFE8E4D8, 0xE8E4D8,
            ImpactEmissionProfile.builder().count(5, 8).spread(0.25F).horizontalSpeed(0.12F).verticalSpeed(0.0F, 0.2F).build());

    public static final ImpactMaterial OIL = build("oil", ImpactMaterialCategory.OIL,
            0xFF161616, 0x161616,
            ImpactEmissionProfile.builder().count(8, 12).spread(0.18F).horizontalSpeed(0.1F).verticalSpeed(0.0F, 0.15F).build());

    /** Genérico, sin mob vanilla asignado - queda disponible para otros mods. */
    public static final ImpactMaterial ENERGY = build("energy",
            ImpactMaterialCategory.ENERGY,
            0xFF33E6FF,
            random -> ParticleTypes.ELECTRIC_SPARK,
            ImpactEmissionProfile.builder().count(10, 16).spread(0.3F).horizontalSpeed(0.25F).verticalSpeed(0.05F, 0.35F).build());

    // --- Undead / bone variants ---

    /** Dark green / brown blood used by the undead (zombies, husks, drowned, zombie horse, etc). */
    public static final ImpactMaterial UNDEAD_BLOOD = build("undead_blood", ImpactMaterialCategory.BLOOD,
            0xFF2F4A2A,
            twoTone(0x2F4A2A /* dark green */, 0x4A3324 /* brown */),
            ImpactEmissionProfile.DEFAULT);

    /** Wither Skeleton: negro/carbón, distinto del hueso blanco genérico. */
    public static final ImpactMaterial WITHER_BONE = build("wither_bone", ImpactMaterialCategory.BONE,
            0xFF242424, 0x242424,
            ImpactEmissionProfile.builder().count(5, 8).spread(0.25F).horizontalSpeed(0.12F).verticalSpeed(0.0F, 0.2F).build());

    // --- Blood variants ---

    /** Spider / Cave Spider. Comparten material; alterna entre dos tonos de morado oscuro. */
    public static final ImpactMaterial ARACHNID_BLOOD = build("arachnid_blood", ImpactMaterialCategory.BLOOD,
            0xFF3F3159,
            twoTone(0x3F3159, 0x263B70 /* tono levemente más azulado para Cave Spider */),
            ImpactEmissionProfile.DEFAULT);

    /** Phantom: sangre desaturada, gris azulado (no es ni undead ni un fluido "de verdad"). */
    public static final ImpactMaterial PHANTOM_BLOOD = build("phantom_blood", ImpactMaterialCategory.BLOOD,
            0xFF57687A, 0x57687A,
            ImpactEmissionProfile.DEFAULT);

    // --- Fluids ---

    /** Dark blue-green fluid, used for the Warden. */
    public static final ImpactMaterial WARDEN_FLUID = build("warden_fluid", ImpactMaterialCategory.FLUID,
            0xFF123A3E, 0x123A3E,
            ImpactEmissionProfile.builder().count(8, 12).spread(0.25F).horizontalSpeed(0.15F).verticalSpeed(0.0F, 0.2F).build());

    /** Enderman, Endermite y Shulker: fluido violeta del End. */
    public static final ImpactMaterial END_FLUID = build("end_fluid", ImpactMaterialCategory.FLUID,
            0xFF8A2BE2, 0x8A2BE2,
            ImpactEmissionProfile.builder().count(8, 12).spread(0.25F).horizontalSpeed(0.18F).verticalSpeed(0.0F, 0.25F).build());

    /** Guardian / Elder Guardian. Comparten material; alterna entre dos tonos de teal. */
    public static final ImpactMaterial GUARDIAN_FLUID = build("guardian_fluid", ImpactMaterialCategory.FLUID,
            0xFF287C8C,
            twoTone(0x287C8C /* Guardian */, 0x1B5965 /* Elder Guardian, más oscuro */),
            ImpactEmissionProfile.builder().count(8, 12).spread(0.22F).horizontalSpeed(0.16F).verticalSpeed(0.0F, 0.2F).build());

    public static final ImpactMaterial SLIME_FLUID = build("slime_fluid", ImpactMaterialCategory.FLUID,
            0xFF55FF55, 0x55FF55,
            ImpactEmissionProfile.builder().count(10, 14).spread(0.3F).horizontalSpeed(0.2F).verticalSpeed(0.0F, 0.3F).build());

    public static final ImpactMaterial CREEPER_FLUID = build("creeper_fluid", ImpactMaterialCategory.FLUID,
            0xFF6FAF32, 0x6FAF32,
            ImpactEmissionProfile.builder().count(9, 13).spread(0.25F).horizontalSpeed(0.18F).verticalSpeed(0.0F, 0.25F).build());

    public static final ImpactMaterial GHAST_FLUID = build("ghast_fluid", ImpactMaterialCategory.FLUID,
            0xFFE8B8C0, 0xE8B8C0,
            ImpactEmissionProfile.builder().count(8, 10).spread(0.3F).horizontalSpeed(0.12F).verticalSpeed(0.05F, 0.2F).build());

    public static final ImpactMaterial MAGMA_FLUID = build("magma_fluid", ImpactMaterialCategory.FLUID,
            0xFFFF4A00, 0xFF4A00,
            ImpactEmissionProfile.builder().count(10, 14).spread(0.28F).horizontalSpeed(0.2F).verticalSpeed(0.05F, 0.35F).build());

    // --- Energy ---

    public static final ImpactMaterial BLAZE_ENERGY = build("blaze_energy", ImpactMaterialCategory.ENERGY,
            0xFFFFB52E, 0xFFB52E,
            ImpactEmissionProfile.builder().count(12, 16).spread(0.3F).horizontalSpeed(0.22F).verticalSpeed(0.05F, 0.35F).build());

    public static final ImpactMaterial BREEZE_ENERGY = build("breeze_energy", ImpactMaterialCategory.ENERGY,
            0xFFA9E8FF, 0xA9E8FF,
            ImpactEmissionProfile.builder().count(10, 14).spread(0.35F).horizontalSpeed(0.28F).verticalSpeed(0.05F, 0.3F).build());

    public static final ImpactMaterial VEX_ENERGY = build("vex_energy", ImpactMaterialCategory.ENERGY,
            0xFFB8A4FF, 0xB8A4FF,
            ImpactEmissionProfile.builder().count(8, 12).spread(0.28F).horizontalSpeed(0.2F).verticalSpeed(0.05F, 0.3F).build());

    /** Boss-tier: más partículas y más spread que un ENERGY genérico. */
    public static final ImpactMaterial WITHER_ENERGY = build("wither_energy", ImpactMaterialCategory.ENERGY,
            0xFF17151C, 0x17151C,
            ImpactEmissionProfile.builder().count(16, 22).spread(0.38F).horizontalSpeed(0.28F).verticalSpeed(0.05F, 0.4F).build());

    /** Boss-tier, propio para diferenciarlo del resto de las entidades del End. */
    public static final ImpactMaterial ENDER_DRAGON_ENERGY = build("ender_dragon_energy", ImpactMaterialCategory.ENERGY,
            0xFF5A168A, 0x5A168A,
            ImpactEmissionProfile.builder().count(16, 24).spread(0.4F).horizontalSpeed(0.3F).verticalSpeed(0.05F, 0.4F).build());

    // --- Non-biológicos (golems): no es sangre de ningún tipo, son fragmentos del material del que
    // está hecho el golem. Categoría CUSTOM porque no encajan como blood/fluid/bone/energy/oil. ---

    /** Iron Golem: fragmentos metálicos, no sangre. Color ajustable, no lo especificó el usuario. */
    public static final ImpactMaterial METAL_FRAGMENTS = build("metal_fragments", ImpactMaterialCategory.CUSTOM,
            0xFFB0AFA8, 0xB0AFA8,
            ImpactEmissionProfile.builder().count(4, 7).spread(0.2F).horizontalSpeed(0.12F).verticalSpeed(0.0F, 0.2F).build());

    /** Snow Golem: fragmentos de nieve, no sangre. Color ajustable, no lo especificó el usuario. */
    public static final ImpactMaterial SNOW_FRAGMENTS = build("snow_fragments", ImpactMaterialCategory.CUSTOM,
            0xFFEFF8FF, 0xEFF8FF,
            ImpactEmissionProfile.builder().count(6, 10).spread(0.25F).horizontalSpeed(0.15F).verticalSpeed(0.0F, 0.25F).build());

    private ImpactMaterials() {
    }

    /**
     * Registers the built-in materials. Safe to call more than once. Other mods do not need to
     * call this - it only registers MadnessCore's own defaults.
     */
    public static void init() {
        ImpactMaterial[] materials = {
                BLOOD, FLUID, TISSUE, BONE, OIL, ENERGY,
                UNDEAD_BLOOD, WITHER_BONE, ARACHNID_BLOOD, PHANTOM_BLOOD,
                WARDEN_FLUID, END_FLUID, GUARDIAN_FLUID, SLIME_FLUID, CREEPER_FLUID, GHAST_FLUID, MAGMA_FLUID,
                BLAZE_ENERGY, BREEZE_ENERGY, VEX_ENERGY, WITHER_ENERGY, ENDER_DRAGON_ENERGY,
                METAL_FRAGMENTS, SNOW_FRAGMENTS
        };
        for (ImpactMaterial material : materials) {
            ImpactMaterialRegistry.register(material);
        }
        ImpactMobBindings.init();
        MadnessCoreCommon.LOG.debug("[impact] registered {} built-in impact materials", materials.length);
    }

    private static ImpactMaterial build(String path, ImpactMaterialCategory category, int displayColor,
                                        int dustColor, ImpactEmissionProfile emission) {
        return build(path, category, displayColor, ImpactParticleFactory.constant(dust(dustColor)), emission);
    }

    /**
     * Builds a {@link DustParticleOptions} from an RGB color (0xRRGGBB). Kept as a small helper so
     * multi-color particle factories (see {@link #twoTone}) don't duplicate the conversion.
     */
    private static DustParticleOptions dust(int rgb) {
        Vector3f color = new Vector3f(
                ((rgb >> 16) & 0xFF) / 255F,
                ((rgb >> 8) & 0xFF) / 255F,
                (rgb & 0xFF) / 255F
        );
        return new DustParticleOptions(color, 1.0F);
    }

    /**
     * Particle factory que alterna al azar entre dos tonos (0xRRGGBB cada uno) - usado por
     * materiales que representan una familia con variación de color (undead, arácnidos,
     * guardianes) sin necesitar un {@link ImpactMaterial} separado por variante.
     */
    private static ImpactParticleFactory twoTone(int colorA, int colorB) {
        return random -> dust(random.nextBoolean() ? colorA : colorB);
    }

    private static ImpactMaterial build(String path, ImpactMaterialCategory category, int displayColor,
                                        ImpactParticleFactory particleFactory, ImpactEmissionProfile emission) {
        return ImpactMaterial.builder(id(path))
                .category(category)
                .displayColor(displayColor)
                .particle(particleFactory)
                .emission(emission)
                .build();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, path);
    }
}