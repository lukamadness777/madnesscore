package dev.lukamadness.madnesscore.common.api.appearance;

/**
 * Modo de coloreado para las máscaras de pelo/ojos dibujadas en {@code CustomizationConfig}
 * (hairPixels/eyePixels). Server-safe: viaja tal cual dentro de {@code AppearanceConfigPayload}
 * (por ordinal), la lógica de render vive del lado cliente.
 */
public enum HairEyeColorMode {
    /** Pinta un color plano y opaco sobre los píxeles marcados, sin importar lo que haya debajo. */
    SOLID,

    /** Multiplica el color elegido contra lo que ya está renderizado (conserva el sombreado/luz del skin). */
    MULTIPLY,

    /** Suma el color elegido (blend aditivo, full-bright) para que brille con ESE color, tipo ojos de Enderman. */
    GLOW_CUSTOM,

    /**
     * Igual que GLOW_CUSTOM pero sin recolorear: vuelve a dibujar los píxeles marcados con el color
     * que YA tienen en el skin base (sampleado del propio skin), en blend aditivo full-bright.
     */
    GLOW_SELF;

    public static HairEyeColorMode byOrdinal(int ordinal) {
        HairEyeColorMode[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, ordinal))];
    }

    public boolean isGlow() {
        return this == GLOW_CUSTOM || this == GLOW_SELF;
    }
}