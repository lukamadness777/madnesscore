package dev.lukamadness.madnesscore.common.client.customization.preset;

import dev.lukamadness.madnesscore.common.api.appearance.HairEyeColorMode;
import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Snapshot de todos los valores de personalización (pelo, ojos, piel, energía) guardado
 * bajo un nombre y una descripción corta elegidos por el jugador.
 * <p>
 * Un {@code CustomizationPreset} guardado NUNCA modifica el personaje por si solo: solo se
 * refleja de verdad cuando el jugador lo selecciona en {@link CustomizationPresetsScreen} y
 * confirma con "Done". El resto de presets guardados quedan intactos para usarse más
 * adelante (otras skins, variantes, etc).
 */
public class CustomizationPreset {
    public String name = "";
    public String description = "";

    public CustomizationConfig.HairType hairType = CustomizationConfig.HairType.SHORT;
    public int hairColor = 0x3B2412;
    public Map<String, boolean[][]> hairPixels = new LinkedHashMap<>();
    public HairEyeColorMode hairColorMode = HairEyeColorMode.SOLID;

    public int eyeOffsetX = 5;
    public int eyeOffsetY = 3;
    public int eyeWidth = 2;
    public int eyeHeight = 1;
    public int eyeColor = 0x000000;
    public int scleraColor = 0xFFFFFF;
    public Map<String, boolean[][]> eyePixels = new LinkedHashMap<>();
    public HairEyeColorMode eyeColorMode = HairEyeColorMode.SOLID;

    public int skinColor = 0xFFFFFF;
    public int energyColor = 0xFFFFFF;

    public CustomizationPreset() {
    }

    /** Captura el estado actual de {@code source} en un preset nuevo con el nombre/descripción dados. */
    public static CustomizationPreset capture(String name, String description, CustomizationConfig source) {
        CustomizationPreset preset = new CustomizationPreset();
        preset.name = name != null ? name : "";
        preset.description = description != null ? description : "";

        preset.hairType = source.hairType;
        preset.hairColor = source.hairColor;
        preset.hairPixels = deepCopy(source.hairPixels);
        preset.hairColorMode = source.hairColorMode;

        preset.eyeOffsetX = source.eyeOffsetX;
        preset.eyeOffsetY = source.eyeOffsetY;
        preset.eyeWidth = source.eyeWidth;
        preset.eyeHeight = source.eyeHeight;
        preset.eyeColor = source.eyeColor;
        preset.scleraColor = source.scleraColor;
        preset.eyePixels = deepCopy(source.eyePixels);
        preset.eyeColorMode = source.eyeColorMode;

        preset.skinColor = source.skinColor;
        preset.energyColor = source.energyColor;
        return preset;
    }

    /**
     * Vuelca este preset dentro de {@code target} (pisa sus valores de personalización).
     * No guarda en disco ni sincroniza con el servidor por si solo: eso lo decide quien
     * lo llama (ver {@link CustomizationPresetsScreen}).
     */
    public void applyTo(CustomizationConfig target) {
        target.hairType = this.hairType != null ? this.hairType : CustomizationConfig.HairType.SHORT;
        target.hairColor = this.hairColor;
        target.hairPixels = deepCopy(this.hairPixels);
        target.hairColorMode = this.hairColorMode != null ? this.hairColorMode : HairEyeColorMode.SOLID;

        target.eyeOffsetX = this.eyeOffsetX;
        target.eyeOffsetY = this.eyeOffsetY;
        target.eyeWidth = this.eyeWidth;
        target.eyeHeight = this.eyeHeight;
        target.eyeColor = this.eyeColor;
        target.scleraColor = this.scleraColor;
        target.eyePixels = deepCopy(this.eyePixels);
        target.eyeColorMode = this.eyeColorMode != null ? this.eyeColorMode : HairEyeColorMode.SOLID;

        target.skinColor = this.skinColor;
        target.energyColor = this.energyColor;
    }

    public static Map<String, boolean[][]> deepCopy(Map<String, boolean[][]> source) {
        Map<String, boolean[][]> copy = new LinkedHashMap<>();
        if (source == null) {
            return copy;
        }
        for (Map.Entry<String, boolean[][]> entry : source.entrySet()) {
            copy.put(entry.getKey(), deepCopyRows(entry.getValue()));
        }
        return copy;
    }

    private static boolean[][] deepCopyRows(boolean[][] source) {
        if (source == null) {
            return null;
        }
        boolean[][] copy = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i] != null ? source[i].clone() : null;
        }
        return copy;
    }
}