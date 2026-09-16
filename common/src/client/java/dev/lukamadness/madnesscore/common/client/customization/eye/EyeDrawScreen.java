package dev.lukamadness.madnesscore.common.client.customization.eye;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;
import dev.lukamadness.madnesscore.common.client.customization.model.SkinRegion;
import dev.lukamadness.madnesscore.common.client.customization.render.SkinTextureCache;
import dev.lukamadness.madnesscore.common.client.gui.widgets.FlatButtonWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public class EyeDrawScreen extends Screen {
    private static final int MIN_PIXEL_SIZE = 8;
    private static final int MAX_PIXEL_SIZE = 34;

    private static final float SKIN_REFERENCE_ALPHA = 0.35f;
    private static final int HAIR_PAINT_ALPHA = 0xE0;

    private static final int LABEL_Y = 26;
    private static final int CANVAS_TOP = LABEL_Y + 14;
    private static final int BOTTOM_MARGIN = 44;

    private static final int SIDE_BTN_W = 90;
    private static final int SIDE_BTN_H = 20;
    private static final int SIDE_BTN_GAP = 6;
    private static final int SIDE_PANEL_RESERVED = SIDE_BTN_W + 32;

    private final Screen parent;
    private final CustomizationConfig cfg;
    private final SkinRegion region;
    private final int gridW, gridH;

    private final boolean[][] workingPixels;

    private boolean eraseMode = false;

    private int pixelSize;
    private int canvasW, canvasH;
    private int canvasX, canvasY;
    private boolean painting = false;

    private FlatButtonWidget drawModeButton;
    private FlatButtonWidget eraseModeButton;

    public EyeDrawScreen(Screen parent, CustomizationConfig cfg, SkinRegion region) {
        super(Component.translatable("madnesscore.config.draw.title_eyes"));
        this.parent = parent;
        this.cfg = cfg;
        this.region = region;
        this.gridW = region.width;
        this.gridH = region.height;
        this.workingPixels = deepCopy(cfg.getEyePixels(region));
    }

    private static boolean[][] deepCopy(boolean[][] source) {
        boolean[][] copy = new boolean[source.length][];
        for (int r = 0; r < source.length; r++) {
            copy[r] = source[r].clone();
        }
        return copy;
    }

    @Override
    protected void init() {
        this.clearWidgets();

        int availableW = this.width - SIDE_PANEL_RESERVED * 2;
        int availableH = this.height - CANVAS_TOP - BOTTOM_MARGIN;
        int maxByWidth = Math.max(MIN_PIXEL_SIZE, availableW / gridW);
        int maxByHeight = Math.max(MIN_PIXEL_SIZE, availableH / gridH);
        pixelSize = Math.max(MIN_PIXEL_SIZE, Math.min(MAX_PIXEL_SIZE, Math.min(maxByWidth, maxByHeight)));

        canvasW = gridW * pixelSize;
        canvasH = gridH * pixelSize;
        canvasX = (this.width - canvasW) / 2;
        canvasY = CANVAS_TOP;

        int sideX = Math.max(8, canvasX - SIDE_BTN_W - 16);
        int sideY = canvasY;

        drawModeButton = new FlatButtonWidget(new Dim2i(sideX, sideY, SIDE_BTN_W, SIDE_BTN_H),
                Component.translatable("madnesscore.config.draw.mode_draw"), () -> eraseMode = false, true, false);
        this.addRenderableWidget(drawModeButton);
        sideY += SIDE_BTN_H + SIDE_BTN_GAP;

        eraseModeButton = new FlatButtonWidget(new Dim2i(sideX, sideY, SIDE_BTN_W, SIDE_BTN_H),
                Component.translatable("madnesscore.config.draw.mode_erase"), () -> eraseMode = true, true, false);
        this.addRenderableWidget(eraseModeButton);
        sideY += SIDE_BTN_H + SIDE_BTN_GAP;

        this.addRenderableWidget(new FlatButtonWidget(new Dim2i(sideX, sideY, SIDE_BTN_W, SIDE_BTN_H),
                Component.translatable("madnesscore.config.draw.clear"), this::clearWorkingPixels, true, false));
        sideY += SIDE_BTN_H + SIDE_BTN_GAP;

        this.addRenderableWidget(new FlatButtonWidget(new Dim2i(sideX, sideY, SIDE_BTN_W, SIDE_BTN_H),
                Component.translatable("madnesscore.config.draw.select_part"), this::openPartSelector, true, false));

        this.addRenderableWidget(new FlatButtonWidget(new Dim2i(this.width / 2 - 104, this.height - 30, 100, 20),
                Component.translatable("madnesscore.config.draw.done"), this::confirmAndClose, true, false));

        this.addRenderableWidget(new FlatButtonWidget(new Dim2i(this.width / 2 + 4, this.height - 30, 100, 20),
                Component.translatable("madnesscore.config.draw.cancel"), this::cancelAndClose, true, false));

        this.addRenderableOnly(this::renderCanvasAndLabels);
    }

    private void renderCanvasAndLabels(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        ctx.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        ctx.drawCenteredString(this.font,
                region.label(), this.width / 2, LABEL_Y, 0xA0A0A0);

        drawModeButton.setEnabled(eraseMode);
        eraseModeButton.setEnabled(!eraseMode);

        drawCanvas(ctx);

        ctx.drawString(this.font,
                Component.translatable("madnesscore.config.draw.color_label",
                        String.format("%06X", cfg.eyeColor & 0xFFFFFF)),
                canvasX, canvasY - 12, 0xFFFFFF);
    }

    private void clearWorkingPixels() {
        for (boolean[] row : workingPixels) {
            Arrays.fill(row, false);
        }
    }

    private void openPartSelector() {
        if (this.minecraft == null) return;
        cfg.setEyePixels(region, deepCopy(workingPixels));
        CustomizationConfig.save();
        this.minecraft.setScreen(new EyePartSelectScreen(this, cfg, region));
    }

    private void confirmAndClose() {
        cfg.setEyePixels(region, deepCopy(workingPixels));
        CustomizationConfig.save();
        goBack();
    }

    private void cancelAndClose() {
        goBack();
    }

    private void goBack() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void onClose() {
        cancelAndClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawCanvas(GuiGraphics ctx) {
        ctx.renderOutline(canvasX - 1, canvasY - 1, canvasW + 2, canvasH + 2, 0xFFAAAAAA);

        RenderSystem.setShaderColor(1f, 1f, 1f, SKIN_REFERENCE_ALPHA);
        ResourceLocation skin = SkinTextureCache.get();
        ctx.blit(skin, canvasX, canvasY, canvasW, canvasH,
                region.u, region.v, region.width, region.height, 64, 64);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int paintColor = (HAIR_PAINT_ALPHA << 24) | (cfg.eyeColor & 0xFFFFFF);
        for (int row = 0; row < gridH; row++) {
            for (int col = 0; col < gridW; col++) {
                if (!workingPixels[row][col]) continue;
                int px = canvasX + col * pixelSize;
                int py = canvasY + row * pixelSize;
                ctx.fill(px, py, px + pixelSize, py + pixelSize, paintColor);
            }
        }

        for (int i = 0; i <= gridW; i++) {
            int x = canvasX + i * pixelSize;
            ctx.fill(x, canvasY, x + 1, canvasY + canvasH, 0x50FFFFFF);
        }
        for (int i = 0; i <= gridH; i++) {
            int y = canvasY + i * pixelSize;
            ctx.fill(canvasX, y, canvasX + canvasW, y + 1, 0x50FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (isInsideCanvas(mx, my)) {
            painting = true;
            paintAt(mx, my);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (painting) {
            paintAt(mx, my);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        painting = false;
        return super.mouseReleased(mx, my, button);
    }

    private boolean isInsideCanvas(double mx, double my) {
        return mx >= canvasX && mx < canvasX + canvasW
                && my >= canvasY && my < canvasY + canvasH;
    }

    private void paintAt(double mx, double my) {
        if (!isInsideCanvas(mx, my)) return;
        int col = (int) ((mx - canvasX) / pixelSize);
        int row = (int) ((my - canvasY) / pixelSize);
        if (col < 0 || col >= gridW || row < 0 || row >= gridH) return;
        workingPixels[row][col] = !eraseMode;
    }
}
