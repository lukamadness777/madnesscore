package dev.lukamadness.madnesscore.common.client.customization.render;

import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;
import dev.lukamadness.madnesscore.common.client.customization.model.SkinRegion;
import dev.lukamadness.madnesscore.common.client.gui.widgets.AbstractWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TemplatePreviewWidget extends AbstractWidget {
    public static final int MAX_PREVIEW_SCALE = 3;
    private static final int MIN_PREVIEW_SCALE = 1;

    private static final int BASE_PIXEL_SIZE = CustomizationConfig.YourSkinPreviewWidget.PIXEL_SIZE;
    public static final int HEAD_UV = CustomizationConfig.YourSkinPreviewWidget.HEAD_UV;
    public static final int LABEL_H = CustomizationConfig.YourSkinPreviewWidget.LABEL_H;

    private final CustomizationConfig cfg;
    private final int pixelSize;
    private final int headPx;

    public TemplatePreviewWidget(int rightEdgeX, int topY, CustomizationConfig cfg, int maxAvailableWidth) {
        super(new Dim2i(
                rightEdgeX - HEAD_UV * computePixelSize(maxAvailableWidth), topY,
                HEAD_UV * computePixelSize(maxAvailableWidth),
                LABEL_H + HEAD_UV * computePixelSize(maxAvailableWidth)));
        this.cfg = cfg;
        this.pixelSize = computePixelSize(maxAvailableWidth);
        this.headPx = HEAD_UV * this.pixelSize;
    }

    private static int computePixelSize(int maxAvailableWidth) {
        int maxScaleThatFits = maxAvailableWidth / (HEAD_UV * BASE_PIXEL_SIZE);
        int effectiveScale = Math.max(MIN_PREVIEW_SCALE, Math.min(MAX_PREVIEW_SCALE, maxScaleThatFits));
        return BASE_PIXEL_SIZE * effectiveScale;
    }

    public int getHeadPx() {
        return headPx;
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        int headY = getY() + LABEL_H;

        Component label = Component.translatable("madnesscore.config.preview.template");
        int textWidth = Minecraft.getInstance().font.width(label);
        int headRightX = getX() + headPx;

        ctx.drawString(Minecraft.getInstance().font, label, headRightX - textWidth, getY(), 0xFFAAAAAA, false);

        drawHead(ctx, getX(), headY);
    }

    private void drawHead(GuiGraphics ctx, int ox, int oy) {
        int baseColor = 0xFF000000 | (cfg.skinColor & 0xFFFFFF);
        ctx.fill(ox, oy, ox + headPx, oy + headPx, baseColor);
        ctx.renderOutline(ox, oy, headPx, headPx, 0xFF888888);

        int clampedOffsetX = clamp(cfg.eyeOffsetX, 0, HEAD_UV - cfg.eyeWidth);
        int eyeRow = clamp(HEAD_UV - cfg.eyeOffsetY - cfg.eyeHeight, 0, HEAD_UV - cfg.eyeHeight);

        int eyeColRight = HEAD_UV - clampedOffsetX - cfg.eyeWidth;
        int eyeColLeft  = clampedOffsetX;

        drawEyeRect(ctx, ox, oy, eyeColRight, eyeRow, cfg.eyeWidth, cfg.eyeHeight);
        drawEyeRect(ctx, ox, oy, eyeColLeft, eyeRow, cfg.eyeWidth, cfg.eyeHeight);

        drawPixelPart(ctx, ox, oy, SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.BASE, SkinRegion.Face.FRONT),
                cfg.getEyePixels(SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.BASE, SkinRegion.Face.FRONT)),
                cfg.eyeColor);
        drawPixelPart(ctx, ox, oy, SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.OVERLAY, SkinRegion.Face.FRONT),
                cfg.getEyePixels(SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.OVERLAY, SkinRegion.Face.FRONT)),
                cfg.eyeColor);

        drawPixelPart(ctx, ox, oy, SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.BASE, SkinRegion.Face.FRONT),
                cfg.getPixels(SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.BASE, SkinRegion.Face.FRONT)),
                cfg.hairColor);
        drawPixelPart(ctx, ox, oy, SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.OVERLAY, SkinRegion.Face.FRONT),
                cfg.getPixels(SkinRegion.of(SkinRegion.BodyPart.HEAD, SkinRegion.Layer.OVERLAY, SkinRegion.Face.FRONT)),
                cfg.hairColor);
    }

    private void drawPixelPart(GuiGraphics ctx, int ox, int oy, SkinRegion region, boolean[][] pixels, int color) {
        int fillColor = 0xFF000000 | (color & 0xFFFFFF);
        for (int row = 0; row < region.height; row++) {
            for (int col = 0; col < region.width; col++) {
                if (!pixels[row][col]) continue;
                int px = ox + col * pixelSize;
                int py = oy + row * pixelSize;
                ctx.fill(px, py, px + pixelSize, py + pixelSize, fillColor);
            }
        }
    }

    private void drawEyeRect(GuiGraphics ctx, int ox, int oy, int col, int row, int width, int height) {
        int eyeScreenX = ox + col * pixelSize;
        int eyeScreenY = oy + row * pixelSize;
        int eyeW = width * pixelSize;
        int eyeH = height * pixelSize;

        int scleraColor = 0xFF000000 | (cfg.scleraColor & 0xFFFFFF);
        ctx.fill(eyeScreenX, eyeScreenY, eyeScreenX + eyeW, eyeScreenY + eyeH, scleraColor);
        ctx.renderOutline(eyeScreenX, eyeScreenY, eyeW, eyeH, scleraColor);
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
