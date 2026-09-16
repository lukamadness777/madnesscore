package dev.lukamadness.madnesscore.common.client.customization.preset;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.config.ConfigManager;
import dev.lukamadness.madnesscore.common.client.config.structure.ModOptions;
import dev.lukamadness.madnesscore.common.client.gui.ButtonTheme;
import dev.lukamadness.madnesscore.common.client.gui.ColorTheme;
import dev.lukamadness.madnesscore.common.client.gui.Colors;
import dev.lukamadness.madnesscore.common.client.gui.widgets.FlatButtonWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Fila de la lista de presets: el nombre se ve siempre (como pide el diseño), la descripción
 * corta solo aparece como tooltip al pasar el mouse por encima (ver
 * {@link CustomizationPresetsScreen#renderHoveredTooltip}).
 * <p>
 * La franja que marca la fila seleccionada usa el mismo acento que la propia pestaña de
 * configuración de Madness Core (rojo), en vez del acento genérico (azulado/turquesa) que usan
 * por defecto el resto de botones del mod.
 */
public class PresetRowWidget extends FlatButtonWidget {
    // Mismas constantes que usa ColorThemeBuilderImpl para construir el tema de un mod a partir
    // de un único color base. Se duplican aquí (en vez de depender de esa clase interna del
    // builder) solo como último recurso, por si esta fila llegase a construirse antes de que el
    // registro de configuración exista todavía.
    private static final float MIN_THEME_SATURATION = 0.2f;
    private static final float MIN_THEME_BRIGHTNESS = 0.55f;
    private static final int FALLBACK_BASE_COLOR = 0xFF8B0000;

    private final CustomizationPreset preset;

    public PresetRowWidget(Dim2i dim, CustomizationPreset preset, Consumer<CustomizationPreset> onClick) {
        super(dim, labelFor(preset), () -> onClick.accept(preset), true, false, true, madnessCoreTheme());
        this.preset = preset;
    }

    private static Component labelFor(CustomizationPreset preset) {
        String name = preset.name;
        return Component.literal(name == null || name.isBlank() ? "?" : name);
    }

    public CustomizationPreset getPreset() {
        return this.preset;
    }

    /**
     * El acento de Madness Core (rojo oscuro) solo debe marcar la franja inferior de la fila
     * seleccionada (ver {@link FlatButtonWidget#render}); el texto del nombre debe seguir
     * usando el color de texto normal, no el acento.
     */
    @Override
    protected int getTextColor() {
        return this.isEnabled() ? Colors.FOREGROUND : Colors.FOREGROUND_DISABLED;
    }

    /**
     * Tema de botón con el acento de Madness Core en vez del acento genérico por defecto de
     * {@link FlatButtonWidget#DEFAULT_THEME}. Se lee del {@code ModOptions} ya registrado para
     * la pestaña de configuración del mod si está disponible, para que sea exactamente el mismo
     * color; si no lo está todavía, se recalcula con la misma fórmula que usa el registro.
     */
    private static ButtonTheme madnessCoreTheme() {
        ColorTheme theme = findRegisteredTheme();
        if (theme == null) {
            theme = new ColorTheme(Colors.constrainColorHSV(FALLBACK_BASE_COLOR, MIN_THEME_SATURATION, MIN_THEME_BRIGHTNESS));
        }
        return new ButtonTheme(theme, Colors.BACKGROUND_HOVER, Colors.BACKGROUND_DEFAULT, Colors.BACKGROUND_LIGHT);
    }

    private static ColorTheme findRegisteredTheme() {
        if (ConfigManager.CONFIG == null) {
            return null;
        }
        for (ModOptions modOptions : ConfigManager.CONFIG.getModOptions()) {
            if (modOptions.configId().equals(MadnessCoreCommon.MOD_ID)) {
                return modOptions.theme();
            }
        }
        return null;
    }
}