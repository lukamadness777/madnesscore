package dev.lukamadness.madnesscore.common.api.appearance;

import net.minecraft.network.chat.Component;

/**
 * Apariencia resuelta de una entidad: color de piel, pelo y ojos (ARGB opaco), más el largo de pelo
 * (solo tiene sentido para jugadores; para aldeanos/illagers viene null).
 */
public record EntityAppearance(int skinColor, int hairColor, int eyeColor, HairLength hairLength) {

    public static EntityAppearance colorsOnly(int skinColor, int hairColor, int eyeColor) {
        return new EntityAppearance(opaque(skinColor), opaque(hairColor), opaque(eyeColor), null);
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    /**
     * Espejo común (server-safe) de dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig.HairType,
     * que vive en el sourceset de client y no es accesible desde el servidor. El orden de los valores
     * tiene que coincidir con esa enum, porque se mapea desde AppearanceConfigPayload#hairType() por ordinal.
     */
    public enum HairLength {
        BALD,
        SHORT,
        MEDIUM,
        LARGE; // corresponde a HairType.LONG del lado client

        public Component label() {
            // Reusa las claves de lang ya existentes de CustomizationConfig.HairType (bald/short/medium/long)
            String key = this == LARGE ? "long" : name().toLowerCase(java.util.Locale.ROOT);
            return Component.translatable("madnesscore.hairtype." + key);
        }

        public static HairLength byOrdinal(int ordinal) {
            HairLength[] values = values();
            return values[Math.max(0, Math.min(values.length - 1, ordinal))];
        }
    }
}
