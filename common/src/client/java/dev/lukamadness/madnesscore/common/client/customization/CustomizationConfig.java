package dev.lukamadness.madnesscore.common.client.customization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.lukamadness.madnesscore.common.api.appearance.HairEyeColorMode;
import dev.lukamadness.madnesscore.common.client.customization.model.SkinRegion;
import dev.lukamadness.madnesscore.common.client.customization.render.AppearanceMaskTexture;
import dev.lukamadness.madnesscore.common.client.customization.render.SkinTextureCache;
import dev.lukamadness.madnesscore.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import dev.lukamadness.madnesscore.common.client.gui.widgets.AbstractWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomizationConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Services.PLATFORM.getConfigDirectory()
            .resolve("madnesscore_customization.json");

    private static CustomizationConfig INSTANCE = new CustomizationConfig();

    public enum HairType {
        BALD("madnesscore.hairtype.bald"),
        SHORT("madnesscore.hairtype.short"),
        MEDIUM("madnesscore.hairtype.medium"),
        LONG("madnesscore.hairtype.long");

        private final String translationKey;
        HairType(String translationKey) { this.translationKey = translationKey; }

        public Component label() { return Component.translatable(translationKey); }

        public HairType next() {
            HairType[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    public int skinColor = 0xFFFFFF;

    public int energyColor = 0xFFFFFF;

    public int eyeOffsetX = 5;
    public int eyeOffsetY = 3;
    public int eyeWidth   = 2;
    public int eyeHeight  = 1;
    public int eyeColor   = 0x000000;

    public int scleraColor = 0xFFFFFF;
    public Map<String, boolean[][]> eyePixels = new LinkedHashMap<>();
    public HairEyeColorMode eyeColorMode = HairEyeColorMode.SOLID;

    public HairType hairType = HairType.SHORT;
    public int hairColor = 0x3B2412;
    public Map<String, boolean[][]> hairPixels = new LinkedHashMap<>();
    public HairEyeColorMode hairColorMode = HairEyeColorMode.SOLID;

    public boolean[][] getPixels(SkinRegion region) {
        return hairPixels.computeIfAbsent(region.key(),
                k -> new boolean[region.height][region.width]);
    }

    public void setPixels(SkinRegion region, boolean[][] pixels) {
        hairPixels.put(region.key(), pixels);
    }

    public boolean[][] getEyePixels(SkinRegion region) {
        return eyePixels.computeIfAbsent(region.key(),
                k -> new boolean[region.height][region.width]);
    }

    public void setEyePixels(SkinRegion region, boolean[][] pixels) {
        eyePixels.put(region.key(), pixels);
    }

    public static CustomizationConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            CustomizationConfig loaded = GSON.fromJson(reader, CustomizationConfig.class);
            INSTANCE = (loaded != null) ? loaded : new CustomizationConfig();
            INSTANCE.ensureDefaults();
        } catch (IOException | JsonParseException e) {
            backupBrokenConfig();
            INSTANCE = new CustomizationConfig();
            save();
        }
    }

    private static void backupBrokenConfig() {
        try {
            Path backup = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".broken-" + System.currentTimeMillis());
            Files.move(CONFIG_PATH, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            dev.lukamadness.madnesscore.common.MadnessCoreCommon.LOG.error("Failed to save customization config", e);
        }

        dev.lukamadness.madnesscore.common.client.customization.network.AppearanceClientHandler.sendToServer();

        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            AppearanceMaskTexture.invalidate(client.player.getUUID());
        }
    }

    private void ensureDefaults() {
        if (hairType == null) {
            hairType = HairType.SHORT;
        }
        if (hairColorMode == null) {
            hairColorMode = HairEyeColorMode.SOLID;
        }
        if (eyeColorMode == null) {
            eyeColorMode = HairEyeColorMode.SOLID;
        }
        if (hairPixels == null) {
            hairPixels = new LinkedHashMap<>();
        }
        if (eyePixels == null) {
            eyePixels = new LinkedHashMap<>();
        }

        for (SkinRegion region : SkinRegion.all()) {
            boolean[][] hairP = hairPixels.get(region.key());
            if (!isValid(hairP, region)) {
                hairPixels.put(region.key(), new boolean[region.height][region.width]);
            }

            boolean[][] eyeP = eyePixels.get(region.key());
            if (!isValid(eyeP, region)) {
                eyePixels.put(region.key(), new boolean[region.height][region.width]);
            }
        }
    }

    private static boolean isValid(boolean[][] pixels, SkinRegion region) {
        if (pixels == null || pixels.length != region.height) return false;
        for (boolean[] row : pixels) {
            if (row == null || row.length != region.width) return false;
        }
        return true;
    }

    public static class YourSkinPreviewWidget extends AbstractWidget {
        public static final int PIXEL_SIZE = 12;
        public static final int HEAD_UV    = 8;
        public static final int HEAD_PX    = HEAD_UV * PIXEL_SIZE;
        public static final int LABEL_H    = 12;
        public static final int MAX_EYE_SIZE = 3;

        public YourSkinPreviewWidget(int x, int y) {
            super(new Dim2i(x, y, HEAD_PX, LABEL_H + HEAD_PX));
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            int headY = getY() + LABEL_H;

            guiGraphics.drawString(Minecraft.getInstance().font,
                    Component.translatable("madnesscore.config.preview.your_skin"), getX(), getY(), 0xFFAAAAAA, false);

            ResourceLocation skinTexture = SkinTextureCache.get();

            guiGraphics.blit(skinTexture, getX(), headY, HEAD_PX, HEAD_PX, 8f, 8f, 8, 8, 64, 64);
            guiGraphics.blit(skinTexture, getX(), headY, HEAD_PX, HEAD_PX, 40f, 8f, 8, 8, 64, 64);

            guiGraphics.renderOutline(getX(), headY, HEAD_PX, HEAD_PX, 0xFF888888);
        }
    }
}