package dev.lukamadness.madnesscore.common.api.appearance.compat.mca;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Reimplementación server-safe de {@code net.conczin.mca.client.resources.ColorPalette}.
 * <p>
 * MCA calcula el color final de piel/pelo muestreando dos PNG "colormap" (uno para piel, otro para
 * pelo) con {@code NativeImage}, una clase que solo existe en el classpath de cliente (blaze3d). Como
 * {@link dev.lukamadness.madnesscore.common.api.appearance.EntityAppearanceApi} tiene que poder
 * resolverse también en el server dedicado, esta clase lee exactamente los mismos PNG (vienen
 * embebidos en el jar de MCA, en {@code assets/mca/textures/colormap/}, así que están en el
 * classpath sin importar el lado) pero con {@code javax.imageio}, que es Java puro.
 * <p>
 * La matemática de muestreo (coordenadas u/v y el "green shift" para el efecto zombi) es la misma
 * que la de MCA, para que el color coincida con el que se ve renderizado en el aldeano.
 */
final class McaColorPalette {
    static final McaColorPalette SKIN = new McaColorPalette("/assets/mca/textures/colormap/villager_skin.png");
    static final McaColorPalette HAIR = new McaColorPalette("/assets/mca/textures/colormap/villager_hair.png");

    private final String resourcePath;
    private volatile int[] pixels;
    private volatile int width = 1;
    private volatile int height = 1;

    private McaColorPalette(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    private void ensureLoaded() {
        if (pixels != null) return;
        synchronized (this) {
            if (pixels != null) return;
            try (InputStream stream = McaColorPalette.class.getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    fallback();
                    return;
                }
                BufferedImage image = ImageIO.read(stream);
                if (image == null) {
                    fallback();
                    return;
                }
                width = image.getWidth();
                height = image.getHeight();
                pixels = image.getRGB(0, 0, width, height, null, 0, width);
            } catch (Exception exception) {
                fallback();
            }
        }
    }

    private void fallback() {
        width = 1;
        height = 1;
        pixels = new int[]{0xFFFFFFFF};
    }

    /**
     * Misma llamada que {@code ColorPalette#getColor(u, v, greenShift)}: u recorre el eje vertical
     * (fila) de la textura, v el horizontal (columna).
     */
    int getColor(float u, float v, float greenShift) {
        ensureLoaded();
        int x = clampFloor(v, width - 1);
        int y = clampFloor(u, height - 1);
        int argb = pixels[y * width + x];
        return greenShift > 0 ? applyGreenShift(argb, greenShift) : argb;
    }

    private static int clampFloor(float value, int max) {
        float clamped = Math.max(0f, Math.min(1f, value));
        return (int) Math.floor(clamped * max);
    }

    private static int applyGreenShift(int color, float greenShift) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        r = clamp01(r * (1.0f - greenShift * 0.3f) - greenShift * 0.1f);
        g = clamp01(g * (1.0f + greenShift * 0.3f) + greenShift * 0.1f);

        return (Math.round(a * 255) << 24) | (Math.round(r * 255) << 16) | (Math.round(g * 255) << 8) | Math.round(b * 255);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}