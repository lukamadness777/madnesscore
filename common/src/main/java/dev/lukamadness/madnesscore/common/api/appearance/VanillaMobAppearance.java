package dev.lukamadness.madnesscore.common.api.appearance;

/**
 * Colores por defecto (vanilla) por tipo de mob. Piel/Pelo/Ojos en ese orden.
 */
public final class VanillaMobAppearance {
    public static final EntityAppearance VILLAGER  = EntityAppearance.colorsOnly(0xBE886C, 0x332411, 0x009611);
    public static final EntityAppearance VINDICATOR = EntityAppearance.colorsOnly(0x959B9B, 0x393939, 0x325566);
    public static final EntityAppearance EVOKER     = EntityAppearance.colorsOnly(0x959B9B, 0x393939, 0x32663C);
    public static final EntityAppearance ILLUSIONER = EntityAppearance.colorsOnly(0x959B9B, 0x393939, 0x32663C);
    public static final EntityAppearance PILLAGER   = EntityAppearance.colorsOnly(0x959B9B, 0x393939, 0x32663C);

    private VanillaMobAppearance() {}
}
