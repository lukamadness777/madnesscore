package dev.lukamadness.madnesscore.common.client.customization.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;
import dev.lukamadness.madnesscore.common.platform.Services;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y carga en disco la lista de {@link CustomizationPreset} del jugador, independiente
 * del estado "en vivo" del personaje ({@link CustomizationConfig}).
 * <p>
 * Todos los presets guardados aquí quedan disponibles para elegir más adelante (otras skins,
 * variantes, etc), pero ninguno se refleja en el personaje hasta que se selecciona y se
 * confirma explícitamente desde {@link CustomizationPresetsScreen}.
 */
public final class CustomizationPresetManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type PRESET_LIST_TYPE = new TypeToken<ArrayList<CustomizationPreset>>() {}.getType();
    private static final Path PRESETS_PATH = Services.PLATFORM.getConfigDirectory()
            .resolve("madnesscore_customization_presets.json");

    private static List<CustomizationPreset> presets;

    private CustomizationPresetManager() {
    }

    public static List<CustomizationPreset> getPresets() {
        ensureLoaded();
        return presets;
    }

    private static void ensureLoaded() {
        if (presets == null) {
            load();
        }
    }

    public static void load() {
        if (!Files.exists(PRESETS_PATH)) {
            presets = new ArrayList<>();
            return;
        }
        try (Reader reader = Files.newBufferedReader(PRESETS_PATH)) {
            List<CustomizationPreset> loaded = GSON.fromJson(reader, PRESET_LIST_TYPE);
            presets = loaded != null ? loaded : new ArrayList<>();
        } catch (IOException | JsonParseException e) {
            MadnessCoreCommon.LOG.error("Failed to load customization presets", e);
            backupBrokenPresets();
            presets = new ArrayList<>();
        }
    }

    private static void backupBrokenPresets() {
        try {
            Path backup = PRESETS_PATH.resolveSibling(PRESETS_PATH.getFileName() + ".broken-" + System.currentTimeMillis());
            Files.move(PRESETS_PATH, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        ensureLoaded();
        try {
            Files.createDirectories(PRESETS_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PRESETS_PATH)) {
                GSON.toJson(presets, PRESET_LIST_TYPE, writer);
            }
        } catch (IOException e) {
            MadnessCoreCommon.LOG.error("Failed to save customization presets", e);
        }
    }

    /** Crea un preset nuevo a partir del estado actual de {@code source}, lo guarda y lo devuelve. */
    public static CustomizationPreset create(String name, String description, CustomizationConfig source) {
        ensureLoaded();
        CustomizationPreset preset = CustomizationPreset.capture(name, description, source);
        presets.add(preset);
        save();
        return preset;
    }

    public static void remove(CustomizationPreset preset) {
        ensureLoaded();
        presets.remove(preset);
        save();
    }
}