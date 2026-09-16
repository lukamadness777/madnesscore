package dev.lukamadness.madnesscore.common.client.customization.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.appearance.HairEyeColorMode;
import dev.lukamadness.madnesscore.common.client.customization.model.SkinRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Genera y cachea, por jugador, una textura 64x64 "máscara" a partir de los hairPixels/eyePixels
 * dibujados en {@code CustomizationConfig}: alfa 255 sólo en los píxeles marcados, 0 en el resto.
 * <p>
 * Para los modos SOLID/MULTIPLY/GLOW_CUSTOM el color de esos píxeles es blanco puro — el tinte real
 * lo pone el color empaquetado que se pasa a {@code Model#renderToBuffer}. Para GLOW_SELF, en cambio,
 * se hornea ahí mismo el color que el propio skin ya tiene en esa zona (leído por GPU readback vía
 * {@link NativeImage#downloadTexture}, porque Minecraft no retiene en CPU los píxeles del skin
 * descargado una vez subido a la GPU).
 * <p>
 * El caché sólo se reconstruye cuando alguien llama a {@link #invalidate(UUID)} (al guardar la config
 * local, o al recibir un {@code AppearanceConfigPayload} nuevo de otro jugador) — nunca por sí solo,
 * para no hacer un GPU readback por jugador por frame.
 */
public final class AppearanceMaskTexture {
    public enum Part { HAIR, EYE }

    private record Key(UUID uuid, Part part) {}

    private static final Map<Key, ResourceLocation> LOCATIONS = new HashMap<>();
    private static int counter = 0;

    private AppearanceMaskTexture() {}

    public static void invalidate(UUID uuid) {
        for (Part part : Part.values()) {
            ResourceLocation existing = LOCATIONS.remove(new Key(uuid, part));
            if (existing != null) {
                Minecraft.getInstance().getTextureManager().release(existing);
            }
        }
    }

    public static void clearAll() {
        for (ResourceLocation location : LOCATIONS.values()) {
            Minecraft.getInstance().getTextureManager().release(location);
        }
        LOCATIONS.clear();
    }

    /**
     * Devuelve (construyendo si hace falta) la ResourceLocation de la máscara para este jugador/parte.
     * Sólo reconstruye la textura la primera vez desde el último {@link #invalidate(UUID)}.
     */
    public static ResourceLocation getOrBuild(UUID uuid, Part part, Map<String, boolean[][]> pixels,
                                               HairEyeColorMode mode, ResourceLocation baseSkin) {
        Key key = new Key(uuid, part);
        ResourceLocation existing = LOCATIONS.get(key);
        if (existing != null) {
            return existing;
        }

        NativeImage baseSnapshot = (mode == HairEyeColorMode.GLOW_SELF) ? downloadSkinSnapshot(baseSkin) : null;
        try {
            NativeImage mask = new NativeImage(64, 64, true);
            for (SkinRegion region : SkinRegion.all()) {
                boolean[][] regionPixels = pixels.get(region.key());
                if (regionPixels == null) {
                    continue;
                }
                bakeRegion(mask, baseSnapshot, region, regionPixels);
            }

            DynamicTexture texture = new DynamicTexture(mask);
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                    MadnessCoreCommon.MOD_ID, "dynamic/appearance_mask_" + (counter++));
            Minecraft.getInstance().getTextureManager().register(location, texture);
            LOCATIONS.put(key, location);
            return location;
        } finally {
            if (baseSnapshot != null) {
                baseSnapshot.close();
            }
        }
    }

    private static void bakeRegion(NativeImage mask, NativeImage baseSnapshot, SkinRegion region, boolean[][] regionPixels) {
        for (int y = 0; y < region.height; y++) {
            boolean[] row = regionPixels[y];
            for (int x = 0; x < row.length; x++) {
                if (!row[x]) {
                    continue;
                }
                int px = region.u + x;
                int py = region.v + y;
                int color = 0xFFFFFFFF;
                if (baseSnapshot != null && px < baseSnapshot.getWidth() && py < baseSnapshot.getHeight()) {
                    // Fuerza alfa opaco (el skin puede tener alfa 0 en zonas no usadas del atlas).
                    color = baseSnapshot.getPixelRGBA(px, py) | 0xFF000000;
                }
                mask.setPixelRGBA(px, py, color);
            }
        }
    }

    /**
     * Lee de vuelta de la GPU los píxeles actuales del skin (base + overlay) del jugador.
     * Minecraft no guarda en CPU la imagen de un skin descargado una vez subido, así que hace
     * falta un readback explícito. Devuelve null si la textura todavía no está lista.
     */
    private static NativeImage downloadSkinSnapshot(ResourceLocation baseSkin) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(baseSkin, null);
        if (texture == null) {
            return null;
        }
        texture.bind();
        NativeImage snapshot = new NativeImage(64, 64, false);
        try {
            snapshot.downloadTexture(0, false);
        } catch (Exception e) {
            snapshot.close();
            return null;
        }
        return snapshot;
    }

    static {
        Objects.requireNonNull(Part.HAIR); // evita warning de "unused" en algunos linters
    }
}