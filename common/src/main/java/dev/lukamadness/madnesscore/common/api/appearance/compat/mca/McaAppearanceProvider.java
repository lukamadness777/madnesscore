package dev.lukamadness.madnesscore.common.api.appearance.compat.mca;

import dev.lukamadness.madnesscore.common.api.appearance.EntityAppearance;
import dev.lukamadness.madnesscore.common.api.appearance.EntityAppearanceProvider;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.entity.ai.Traits;
import net.conczin.mca.resources.HairStyleList;
import net.conczin.mca.resources.data.skin.HairStyle;
import net.conczin.mca.resources.data.skin.LayeredHair;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Optional;

/**
 * Integra Minecraft Comes Alive con {@link dev.lukamadness.madnesscore.common.api.appearance.EntityAppearanceApi}.
 * <p>
 * Lectura ({@link #resolve}): cualquier {@link VillagerLike} (aldeano MCA, zombi-aldeano MCA, etc.)
 * devuelve el color de piel/pelo/ojos REAL calculado a partir de su genética (o el tinte manual del
 * Editor de Aldeanos, si tiene uno puesto), en vez del color fijo que usa el fallback vanilla.
 * <p>
 * Escritura ({@link #apply}): pisa los dyes de piel/pelo/ojos de la entidad y, si viene un
 * {@link EntityAppearance.HairLength}, le busca un peinado registrado que matchee ese largo.
 */
public final class McaAppearanceProvider implements EntityAppearanceProvider {
    private static final int NATURAL_SKIN_DYE = 0xFF000000;
    private static final int NATURAL_HAIR_DYE = 0xFF000000;
    private static final int NATURAL_EYE_DYE = 0xFFFFFFFF;

    private static final int ALBINISM_EYE_COLOR = 0xFFE8A0A0;
    private static final int BLUE_EYE_COLOR = 0xFF3A98E8;
    private static final int GREEN_EYE_COLOR = 0xFF4CB346;
    private static final int HAZEL_EYE_COLOR = 0xFFC29B35;
    private static final int BROWN_EYE_COLOR = 0xFF7C4825;

    @Override
    public boolean supports(Entity entity) {
        return entity instanceof VillagerLike<?>;
    }

    @Override
    public Optional<EntityAppearance> resolve(MinecraftServer server, Entity entity) {
        if (!(entity instanceof VillagerLike<?> villager)) return Optional.empty();

        return Optional.of(new EntityAppearance(
                resolveSkinColor(villager),
                resolveHairColor(villager),
                resolveEyeColor(villager),
                resolveHairLength(villager)
        ));
    }

    @Override
    public boolean apply(MinecraftServer server, Entity entity, EntityAppearance appearance) {
        if (!(entity instanceof VillagerLike<?> villager)) return false;

        villager.setSkinDye(appearance.skinColor());
        villager.setHairDye(appearance.hairColor());
        villager.setEyeDye(appearance.eyeColor());

        if (appearance.hairLength() != null) {
            applyHairLength(villager, appearance.hairLength());
        }

        return true;
    }

    // ---- lectura -----------------------------------------------------------------------------

    private int resolveSkinColor(VillagerLike<?> villager) {
        int dye = villager.getSkinDye();
        if (dye != NATURAL_SKIN_DYE) return dye;

        float albinism = villager.getTraits().hasTrait(Traits.ALBINISM) ? 0.1f : 1.0f;
        return McaColorPalette.SKIN.getColor(
                villager.getGenetics().getGene(Genetics.MELANIN) * albinism,
                villager.getGenetics().getGene(Genetics.HEMOGLOBIN) * albinism,
                villager.getInfectionProgress()
        );
    }

    private int resolveHairColor(VillagerLike<?> villager) {
        int dye = villager.getHairDye();
        if (dye != NATURAL_HAIR_DYE) return dye;

        float albinism = villager.getTraits().hasTrait(Traits.ALBINISM) ? 0.1f : 1.0f;
        return McaColorPalette.HAIR.getColor(
                villager.getGenetics().getGene(Genetics.EUMELANIN) * albinism,
                villager.getGenetics().getGene(Genetics.PHEOMELANIN) * albinism,
                0
        );
    }

    private int resolveEyeColor(VillagerLike<?> villager) {
        int dye = villager.getEyeDye();
        if (dye != NATURAL_EYE_DYE) return dye;

        if (villager.getTraits().hasTrait(Traits.ALBINISM)) {
            return ALBINISM_EYE_COLOR;
        }

        // Misma curva que net.conczin.mca.client.resources.EyeTextureLayers, reimplementada acá
        // (esa clase es de cliente) para poder resolverla también en el server. No distingue
        // heterocromía porque EntityAppearance solo tiene un campo de color de ojos.
        float eyeColor = frac(villager.getGenetics().getGene(Genetics.FACE));
        int base;
        if (eyeColor < 0.35F) {
            base = lerpArgb(eyeColor / 0.35F, BLUE_EYE_COLOR, GREEN_EYE_COLOR);
        } else if (eyeColor < 0.70F) {
            base = lerpArgb((eyeColor - 0.35F) / 0.35F, GREEN_EYE_COLOR, HAZEL_EYE_COLOR);
        } else {
            base = lerpArgb((eyeColor - 0.70F) / 0.30F, HAZEL_EYE_COLOR, BROWN_EYE_COLOR);
        }

        return applyBrightness(base, villager.getGenetics().getGene(Genetics.EYE_BRIGHTNESS));
    }

    /**
     * MCA no tiene un gen de "largo de pelo": el aspecto sale de capas (base/flequillo/atrás/
     * adelante/extra). Lo aproximamos con la misma lógica que ya le expliqué en el chat: si tiene
     * capa BACK -> LARGE, si no pero tiene FRONT -> MEDIUM, si solo tiene BASE/BANGS -> SHORT, si no
     * tiene nada puesto -> BALD.
     */
    private EntityAppearance.HairLength resolveHairLength(VillagerLike<?> villager) {
        boolean hasBack = !villager.getLayeredHair(LayeredHair.Category.BACK).isBlank();
        boolean hasFront = !villager.getLayeredHair(LayeredHair.Category.FRONT).isBlank();
        boolean hasBase = !villager.getLayeredHair(LayeredHair.Category.BASE).isBlank()
                || !villager.getHairStyleId().isBlank();

        if (hasBack) return EntityAppearance.HairLength.LARGE;
        if (hasFront) return EntityAppearance.HairLength.MEDIUM;
        if (hasBase) return EntityAppearance.HairLength.SHORT;
        return EntityAppearance.HairLength.BALD;
    }

    // ---- escritura -----------------------------------------------------------------------------

    private void applyHairLength(VillagerLike<?> villager, EntityAppearance.HairLength length) {
        if (length == EntityAppearance.HairLength.BALD) {
            villager.clearLayeredHair();
            villager.setHairStyleId("");
            return;
        }

        HairStyleList styles = HairStyleList.getInstance();
        if (styles == null) return;

        List<HairStyle> candidates = styles.getStyles(villager.getGenetics().getGender()).stream()
                .filter(style -> matchesLength(style, length))
                .toList();
        if (candidates.isEmpty()) return;

        RandomSource random = villager.asEntity().getRandom();
        villager.setHairStyle(candidates.get(random.nextInt(candidates.size())));
    }

    private boolean matchesLength(HairStyle style, EntityAppearance.HairLength length) {
        boolean hasBack = !style.back().isBlank();
        boolean hasFront = !style.front().isBlank();
        return switch (length) {
            case LARGE -> hasBack;
            case MEDIUM -> !hasBack && hasFront;
            case SHORT -> !hasBack && !hasFront;
            case BALD -> false;
        };
    }

    // ---- utilidades de color ---------------------------------------------------------------------

    private static float frac(float value) {
        return value - (float) Math.floor(value);
    }

    private static int lerpArgb(float delta, int from, int to) {
        delta = Math.max(0f, Math.min(1f, delta));
        int a = lerpChannel(delta, (from >> 24) & 0xFF, (to >> 24) & 0xFF);
        int r = lerpChannel(delta, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = lerpChannel(delta, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = lerpChannel(delta, from & 0xFF, to & 0xFF);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(float delta, int from, int to) {
        return Math.round(from + (to - from) * delta);
    }

    private static int applyBrightness(int argb, float brightness) {
        float factor = 0.5F + Math.max(0f, Math.min(1f, brightness));
        int a = (argb >>> 24) & 0xFF;
        int r = scaleChannel((argb >>> 16) & 0xFF, factor);
        int g = scaleChannel((argb >>> 8) & 0xFF, factor);
        int b = scaleChannel(argb & 0xFF, factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int scaleChannel(int channel, float factor) {
        return Math.max(0, Math.min(255, Math.round(channel * factor)));
    }
}