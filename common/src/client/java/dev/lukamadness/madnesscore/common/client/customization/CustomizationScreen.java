package dev.lukamadness.madnesscore.common.client.customization;

import dev.lukamadness.madnesscore.common.client.customization.eye.EyeDrawScreen;
import dev.lukamadness.madnesscore.common.client.customization.hair.HairDrawScreen;
import dev.lukamadness.madnesscore.common.client.customization.model.SkinRegion;
import dev.lukamadness.madnesscore.common.client.customization.preset.CustomizationPresetsScreen;
import dev.lukamadness.madnesscore.common.client.customization.render.TemplatePreviewWidget;
import dev.lukamadness.madnesscore.common.client.gui.ButtonTheme;
import dev.lukamadness.madnesscore.common.client.gui.Colors;
import dev.lukamadness.madnesscore.common.client.gui.widgets.AbstractWidget;
import dev.lukamadness.madnesscore.common.client.gui.widgets.FlatButtonWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class CustomizationScreen extends Screen {
    private static final int MARGIN_X = 20;
    private static final int HEADER_Y_START = 32;
    private static final int HEADER_WIDTH = 200;
    private static final int HEADER_HEIGHT = 20;
    private static final int SECTION_GAP = 6;

    private static final int SLIDER_WIDTH = 200;
    private static final int SLIDER_HEIGHT = 20;
    private static final int SLIDER_GAP = 4;
    private static final int PREVIEW_GAP = 10;

    // Deja sitio de sobra para la fila de botones inferior (Presets a la izquierda, Done a la
    // derecha) más un pequeño respiro, para que el contenido del acordeón (hair/eyes/skin/energy)
    // nunca se solape visualmente con esos botones aunque el usuario haga scroll hasta el fondo.
    private static final int BOTTOM_BUTTON_HEIGHT = 20;
    private static final int BOTTOM_BUTTON_MARGIN = 8;
    private static final int BOTTOM_BUTTON_GAP = 12;
    private static final int CONTENT_BOTTOM_MARGIN = BOTTOM_BUTTON_HEIGHT + BOTTOM_BUTTON_MARGIN + BOTTOM_BUTTON_GAP;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLL_STEP = 16;
    private boolean draggingScrollbar = false;
    private int scrollbarTrackX, scrollbarThumbY, scrollbarThumbHeight, scrollbarTrackHeight;

    private final Screen parent;
    private final CustomizationConfig cfg;

    private boolean hairExpanded = false;
    private boolean eyesExpanded = false;
    private boolean skinExpanded = false;
    private boolean energyExpanded = false;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int contentTop, contentBottom, totalContentHeight;

    // Widgets del acordeón (hair/eyes/skin/energy + la previsualización de arriba), que se
    // desplazan con el scroll y se recortan (scissor) para que nunca se dibujen por encima de la
    // fila de botones inferior. Los widgets "estáticos" (previsualización de la derecha, Presets
    // y Done) no se recortan nunca.
    private final List<AbstractWidget> scrollWidgets = new ArrayList<>();
    private final List<AbstractWidget> staticWidgets = new ArrayList<>();

    private boolean colorPickerOpen = false;
    private boolean eyedropperActive = false;
    private IntConsumer colorConfirmCallback;

    private static final int SB_SIZE = 130;
    private static final int HUE_W = 18;
    private static final int POPUP_GAP = 8;
    private static final int PREVIEW_H = 22;
    private static final int PICK_BTN_H = 18;
    private float pickerHue, pickerSaturation, pickerBrightness;
    private boolean draggingSB = false;
    private boolean draggingHue = false;
    private int popupX, popupY;

    private static final ResourceLocation SB_TEXTURE_LOCATION =
            ResourceLocation.fromNamespaceAndPath("madnesscore", "dynamic/customization_color_sb");
    private static final ResourceLocation HUE_TEXTURE_LOCATION =
            ResourceLocation.fromNamespaceAndPath("madnesscore", "dynamic/customization_color_hue");

    private DynamicTexture sbTexture;
    private DynamicTexture hueTexture;
    private float sbTextureHue = Float.NaN;

    public CustomizationScreen(Screen parent, CustomizationConfig cfg) {
        super(Component.translatable("madnesscore.config.customization.title"));
        this.parent = parent;
        this.cfg = cfg;
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private int computeContentHeight() {
        int y = HEADER_Y_START;

        y += CustomizationConfig.YourSkinPreviewWidget.LABEL_H + CustomizationConfig.YourSkinPreviewWidget.HEAD_PX + PREVIEW_GAP;

        y += HEADER_HEIGHT + SECTION_GAP;
        if (hairExpanded) {
            y += (HEADER_HEIGHT + SECTION_GAP) * 3;
        }

        y += HEADER_HEIGHT + SECTION_GAP;
        if (eyesExpanded) {
            y += (SLIDER_HEIGHT + SLIDER_GAP) * 4;
            y += HEADER_HEIGHT + SECTION_GAP;
            y += HEADER_HEIGHT + SECTION_GAP;
            y += HEADER_HEIGHT + SECTION_GAP;
        }

        y += HEADER_HEIGHT + SECTION_GAP;
        if (skinExpanded) y += HEADER_HEIGHT + SECTION_GAP;

        y += HEADER_HEIGHT + SECTION_GAP;
        if (energyExpanded) y += HEADER_HEIGHT + SECTION_GAP;

        return y - HEADER_Y_START;
    }

    private void computeScrollbarGeometry() {
        scrollbarTrackX = MARGIN_X + HEADER_WIDTH + 10;
        scrollbarTrackHeight = contentBottom - contentTop;
        if (scrollbarTrackHeight <= 0 || totalContentHeight <= 0) {
            scrollbarThumbHeight = 0;
            scrollbarThumbY = contentTop;
            return;
        }
        scrollbarThumbHeight = clampInt(
                (int) ((float) scrollbarTrackHeight / totalContentHeight * scrollbarTrackHeight),
                16, scrollbarTrackHeight);
        scrollbarThumbY = contentTop + (maxScroll == 0 ? 0
                : (int) ((float) scrollOffset / maxScroll * (scrollbarTrackHeight - scrollbarThumbHeight)));
    }

    private <T extends AbstractWidget> T addScrollable(T widget) {
        this.addRenderableWidget(widget);
        this.scrollWidgets.add(widget);
        return widget;
    }

    private <T extends AbstractWidget> T addStatic(T widget) {
        this.addRenderableWidget(widget);
        this.staticWidgets.add(widget);
        return widget;
    }

    public void rebuildWidgets() {
        this.clearWidgets();
        this.scrollWidgets.clear();
        this.staticWidgets.clear();

        contentTop = HEADER_Y_START;
        contentBottom = this.height - CONTENT_BOTTOM_MARGIN;
        int viewportHeight = Math.max(0, contentBottom - contentTop);

        totalContentHeight = computeContentHeight();
        maxScroll = Math.max(0, totalContentHeight - viewportHeight);
        scrollOffset = clampInt(scrollOffset, 0, maxScroll);
        computeScrollbarGeometry();

        int y = HEADER_Y_START;

        int yourSkinPreviewX = MARGIN_X + (SLIDER_WIDTH - CustomizationConfig.YourSkinPreviewWidget.HEAD_PX) / 2;
        CustomizationConfig.YourSkinPreviewWidget yourSkinPreview =
                new CustomizationConfig.YourSkinPreviewWidget(yourSkinPreviewX, y - scrollOffset);
        this.addScrollable(yourSkinPreview);
        y += yourSkinPreview.getHeight() + PREVIEW_GAP;

        this.addScrollable(new FlatButtonWidget(
                new Dim2i(MARGIN_X, y - scrollOffset, HEADER_WIDTH, HEADER_HEIGHT),
                Component.translatable("madnesscore.config.section.hair", hairExpanded ? "▲" : "▼"), () -> {
                    hairExpanded = !hairExpanded;
                    rebuildWidgets();
                }, true, false));
        y += HEADER_HEIGHT + SECTION_GAP;

        if (hairExpanded) {
            this.addScrollable(new FlatButtonWidget(
                    new Dim2i(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT),
                    Component.translatable("madnesscore.config.hair.type", cfg.hairType.label()), () -> {
                        cfg.hairType = cfg.hairType.next();
                        CustomizationConfig.save();
                        rebuildWidgets();
                    }, true, false));
            y += HEADER_HEIGHT + SECTION_GAP;

            this.addScrollable(new FlatButtonWidget(
                    new Dim2i(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT),
                    Component.translatable("madnesscore.config.hair.color"), () ->
                            openColorPicker(cfg.hairColor, color -> { cfg.hairColor = color; CustomizationConfig.save(); }),
                    true, false));
            y += HEADER_HEIGHT + SECTION_GAP;

            this.addScrollable(new FlatButtonWidget(
                    new Dim2i(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT),
                    Component.translatable("madnesscore.config.hair.draw"), this::openHairDrawScreen, true, false));
            y += HEADER_HEIGHT + SECTION_GAP;
        }

        this.addScrollable(new FlatButtonWidget(
                new Dim2i(MARGIN_X, y - scrollOffset, HEADER_WIDTH, HEADER_HEIGHT),
                Component.translatable("madnesscore.config.section.eyes", eyesExpanded ? "▲" : "▼"), () -> {
                    eyesExpanded = !eyesExpanded;
                    rebuildWidgets();
                }, true, false));
        y += HEADER_HEIGHT + SECTION_GAP;

        if (eyesExpanded) {
            this.addScrollable(new IntSliderWidget(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, SLIDER_HEIGHT,
                    "madnesscore.config.eye.offset_x", 0, CustomizationConfig.YourSkinPreviewWidget.HEAD_UV - 1,
                    () -> cfg.eyeOffsetX, v -> cfg.eyeOffsetX = v));
            y += SLIDER_HEIGHT + SLIDER_GAP;

            this.addScrollable(new IntSliderWidget(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, SLIDER_HEIGHT,
                    "madnesscore.config.eye.offset_y", 0, CustomizationConfig.YourSkinPreviewWidget.HEAD_UV - 1,
                    () -> cfg.eyeOffsetY, v -> cfg.eyeOffsetY = v));
            y += SLIDER_HEIGHT + SLIDER_GAP;

            this.addScrollable(new IntSliderWidget(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, SLIDER_HEIGHT,
                    "madnesscore.config.eye.width", 1, CustomizationConfig.YourSkinPreviewWidget.MAX_EYE_SIZE,
                    () -> cfg.eyeWidth, v -> cfg.eyeWidth = v));
            y += SLIDER_HEIGHT + SLIDER_GAP;

            this.addScrollable(new IntSliderWidget(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, SLIDER_HEIGHT,
                    "madnesscore.config.eye.height", 1, CustomizationConfig.YourSkinPreviewWidget.MAX_EYE_SIZE,
                    () -> cfg.eyeHeight, v -> cfg.eyeHeight = v));
            y += SLIDER_HEIGHT + SLIDER_GAP;

            this.addScrollable(new FlatButtonWidget(
                    new Dim2i(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT),
                    Component.translatable("madnesscore.config.eye.color"), () ->
                            openColorPicker(cfg.eyeColor, color -> { cfg.eyeColor = color; CustomizationConfig.save(); }),
                    true, false));
            y += HEADER_HEIGHT + SECTION_GAP;

            this.addScrollable(new FlatButtonWidget(
                    new Dim2i(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT),
                    Component.translatable("madnesscore.config.eye.sclera_color"), () ->
                            openColorPicker(cfg.scleraColor, color -> { cfg.scleraColor = color; CustomizationConfig.save(); }),
                    true, false));
            y += HEADER_HEIGHT + SECTION_GAP;

            this.addScrollable(new FlatButtonWidget(
                    new Dim2i(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT),
                    Component.translatable("madnesscore.config.eye.draw"), this::openEyeDrawScreen, true, false));
            y += HEADER_HEIGHT + SECTION_GAP;
        }

        this.addScrollable(new FlatButtonWidget(
                new Dim2i(MARGIN_X, y - scrollOffset, HEADER_WIDTH, HEADER_HEIGHT),
                Component.translatable("madnesscore.config.section.skin", skinExpanded ? "▲" : "▼"), () -> {
                    skinExpanded = !skinExpanded;
                    rebuildWidgets();
                }, true, false));
        y += HEADER_HEIGHT + SECTION_GAP;

        if (skinExpanded) {
            this.addScrollable(new FlatButtonWidget(
                    new Dim2i(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT),
                    Component.translatable("madnesscore.config.skin.color"), () ->
                            openColorPicker(cfg.skinColor, color -> { cfg.skinColor = color; CustomizationConfig.save(); }),
                    true, false));
            y += HEADER_HEIGHT + SECTION_GAP;
        }

        this.addScrollable(new FlatButtonWidget(
                new Dim2i(MARGIN_X, y - scrollOffset, HEADER_WIDTH, HEADER_HEIGHT),
                Component.translatable("madnesscore.config.section.energy", energyExpanded ? "▲" : "▼"), () -> {
                    energyExpanded = !energyExpanded;
                    rebuildWidgets();
                }, true, false));
        y += HEADER_HEIGHT + SECTION_GAP;

        if (energyExpanded) {
            this.addScrollable(new FlatButtonWidget(
                    new Dim2i(MARGIN_X, y - scrollOffset, SLIDER_WIDTH, HEADER_HEIGHT),
                    Component.translatable("madnesscore.config.energy.color"), () ->
                            openColorPicker(cfg.energyColor, color -> { cfg.energyColor = color; CustomizationConfig.save(); }),
                    true, false));
            y += HEADER_HEIGHT + SECTION_GAP;
        }

        int previewGap = 16;
        int leftContentRightEdge = MARGIN_X + HEADER_WIDTH + 10 + SCROLLBAR_WIDTH;
        int maxPreviewWidth = this.width - leftContentRightEdge - previewGap - MARGIN_X;

        this.addStatic(new TemplatePreviewWidget(
                this.width - MARGIN_X, HEADER_Y_START, cfg, maxPreviewWidth));

        // "Presets" va en el lado contrario a "Done" (misma fila, esquina opuesta). Abre la
        // pestaña de presets guardados; seleccionar uno ahí solo lo aplica de verdad si se
        // confirma con el "Done" de esa pantalla, no con este.
        this.addStatic(new FlatButtonWidget(
                new Dim2i(BOTTOM_BUTTON_MARGIN, this.height - BOTTOM_BUTTON_MARGIN - BOTTOM_BUTTON_HEIGHT, 100, BOTTOM_BUTTON_HEIGHT),
                Component.translatable("madnesscore.config.presets.open"), this::openPresetsScreen, true, false));

        this.addStatic(new FlatButtonWidget(
                new Dim2i(this.width - BOTTOM_BUTTON_MARGIN - 100, this.height - BOTTOM_BUTTON_MARGIN - BOTTOM_BUTTON_HEIGHT, 100, BOTTOM_BUTTON_HEIGHT),
                Component.translatable("gui.done"), this::onClose, true, false));
    }

    private void openPresetsScreen() {
        if (this.minecraft == null) return;
        CustomizationConfig.save();
        this.minecraft.setScreen(new CustomizationPresetsScreen(this, cfg));
    }

    private void openHairDrawScreen() {
        if (this.minecraft == null) return;
        this.minecraft.setScreen(new HairDrawScreen(this, cfg, SkinRegion.defaultRegion()));
    }

    private void openEyeDrawScreen() {
        if (this.minecraft == null) return;
        this.minecraft.setScreen(new EyeDrawScreen(this, cfg, SkinRegion.defaultRegion()));
    }

    private static int clampInt(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (colorPickerOpen) return true;
        scrollOffset -= (int) (verticalAmount * SCROLL_STEP);
        rebuildWidgets();
        return true;
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        // El contenido del acordeón se recorta al área visible (un poco por encima de la fila
        // de botones inferior, gracias a CONTENT_BOTTOM_MARGIN) para que hair/eyes/skin/energy
        // expandidos nunca se dibujen encima de "Presets"/"Done", aunque sí se pueda seguir
        // desplazando con la ruedita. Los widgets fijos (previsualización, Presets, Done) se
        // dibujan siempre, sin recorte.
        ctx.enableScissor(0, contentTop, this.width, contentBottom);
        for (AbstractWidget widget : this.scrollWidgets) {
            widget.render(ctx, mouseX, mouseY, delta);
        }
        ctx.disableScissor();

        for (AbstractWidget widget : this.staticWidgets) {
            widget.render(ctx, mouseX, mouseY, delta);
        }

        ctx.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        if (maxScroll > 0 && !(colorPickerOpen && !eyedropperActive)) {
            drawScrollbar(ctx);
        }

        if (colorPickerOpen && eyedropperActive) {
            ctx.drawCenteredString(this.font,
                    Component.translatable("madnesscore.config.colorpicker.eyedropper_hint"),
                    this.width / 2, 8, 0xFFFFFF00);
        } else if (colorPickerOpen) {
            renderColorPickerPopup(ctx);
        }
    }

    private void drawScrollbar(GuiGraphics ctx) {
        if (scrollbarTrackHeight <= 0 || totalContentHeight <= 0) return;
        ctx.fill(scrollbarTrackX, contentTop, scrollbarTrackX + SCROLLBAR_WIDTH, contentBottom, 0x40FFFFFF);
        ctx.fill(scrollbarTrackX, scrollbarThumbY, scrollbarTrackX + SCROLLBAR_WIDTH,
                scrollbarThumbY + scrollbarThumbHeight, 0xFFAAAAAA);
    }

    private boolean mouseInScrollbarThumb(double mx, double my) {
        if (scrollbarThumbHeight <= 0) return false;
        return mx >= scrollbarTrackX && mx < scrollbarTrackX + SCROLLBAR_WIDTH
                && my >= scrollbarThumbY && my < scrollbarThumbY + scrollbarThumbHeight;
    }

    private void updateScrollFromDrag(double my) {
        if (scrollbarTrackHeight <= scrollbarThumbHeight) return;
        double usableTrack = scrollbarTrackHeight - scrollbarThumbHeight;
        double relative = (my - contentTop - scrollbarThumbHeight / 2.0) / usableTrack;
        scrollOffset = clampInt((int) Math.round(relative * maxScroll), 0, maxScroll);
        rebuildWidgets();
    }

    private void openColorPicker(int initialColor, IntConsumer onConfirm) {
        int color = initialColor & 0xFFFFFF;
        float[] hsb = rgbToHsb((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
        pickerHue = hsb[0];
        pickerSaturation = hsb[1];
        pickerBrightness = hsb[2];
        colorConfirmCallback = onConfirm;

        int totalW = SB_SIZE + POPUP_GAP + HUE_W;
        int totalH = SB_SIZE + POPUP_GAP + PREVIEW_H + POPUP_GAP + PICK_BTN_H + POPUP_GAP + 20;
        popupX = (this.width - totalW) / 2;
        popupY = (this.height - totalH) / 2;

        eyedropperActive = false;
        colorPickerOpen = true;
    }

    private void renderColorPickerPopup(GuiGraphics ctx) {
        int totalW = SB_SIZE + POPUP_GAP + HUE_W;
        int totalH = SB_SIZE + POPUP_GAP + PREVIEW_H + POPUP_GAP + PICK_BTN_H + POPUP_GAP + 20;

        ctx.fill(0, 0, this.width, this.height, 0x80000000);
        ctx.fill(popupX - 6, popupY - 6, popupX + totalW + 6, popupY + totalH + 6, Colors.BACKGROUND_LIGHT);

        drawSBSquare(ctx, popupX, popupY);
        int cursorX = popupX + (int) (pickerSaturation * (SB_SIZE - 1));
        int cursorY = popupY + (int) ((1f - pickerBrightness) * (SB_SIZE - 1));
        ctx.fill(cursorX - 2, cursorY - 2, cursorX + 3, cursorY + 3, 0xFFFFFFFF);
        ctx.fill(cursorX - 1, cursorY - 1, cursorX + 2, cursorY + 2, 0xFF000000);

        int hueX = popupX + SB_SIZE + POPUP_GAP;
        drawHueBar(ctx, hueX, popupY);
        int hueCursorY = popupY + (int) (pickerHue * (SB_SIZE - 1));
        ctx.fill(hueX - 2, hueCursorY - 1, hueX + HUE_W + 2, hueCursorY + 2, 0xFFFFFFFF);
        ctx.fill(hueX - 1, hueCursorY, hueX + HUE_W + 1, hueCursorY + 1, 0xFF000000);

        int previewY = popupY + SB_SIZE + POPUP_GAP;
        int currentColor = hsbToRgb(pickerHue, pickerSaturation, pickerBrightness);
        ctx.fill(popupX, previewY, popupX + totalW, previewY + PREVIEW_H, 0xFF000000 | currentColor);
        ctx.drawCenteredString(this.font,
                Component.literal("#" + String.format("%06X", currentColor)),
                popupX + totalW / 2, previewY + (PREVIEW_H - 8) / 2, 0xFFFFFFFF);

        int pickBtnY = previewY + PREVIEW_H + POPUP_GAP;
        drawPopupButton(ctx, popupX, pickBtnY, totalW, PICK_BTN_H,
                Component.translatable("madnesscore.config.colorpicker.pick").getString(), mouseInPickButton());

        int btnY = pickBtnY + PICK_BTN_H + POPUP_GAP;
        drawPopupButton(ctx, popupX, btnY, 60, 18,
                Component.translatable("madnesscore.config.colorpicker.done").getString(), mouseInPopupDoneButton());
        drawPopupButton(ctx, popupX + totalW - 60, btnY, 60, 18,
                Component.translatable("madnesscore.config.colorpicker.cancel").getString(), mouseInPopupCancelButton());
    }

    private void drawSBSquare(GuiGraphics ctx, int x, int y) {
        if (this.sbTexture == null || this.sbTextureHue != this.pickerHue) {
            this.rebuildSBTexture();
        }
        ctx.blit(SB_TEXTURE_LOCATION, x, y, SB_SIZE, SB_SIZE, 0, 0, SB_SIZE, SB_SIZE, SB_SIZE, SB_SIZE);
    }

    private void rebuildSBTexture() {
        if (this.sbTexture == null) {
            NativeImage image = new NativeImage(SB_SIZE, SB_SIZE, false);
            this.sbTexture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(SB_TEXTURE_LOCATION, this.sbTexture);
        }

        NativeImage image = this.sbTexture.getPixels();
        if (image != null) {
            for (int px = 0; px < SB_SIZE; px++) {
                float s = px / (float) (SB_SIZE - 1);
                for (int py = 0; py < SB_SIZE; py++) {
                    float b = 1f - py / (float) (SB_SIZE - 1);
                    int color = hsbToRgb(this.pickerHue, s, b);
                    image.setPixelRGBA(px, py, toNativeImageColor(color));
                }
            }
            this.sbTexture.upload();
        }
        this.sbTextureHue = this.pickerHue;
    }

    private void drawHueBar(GuiGraphics ctx, int x, int y) {
        if (this.hueTexture == null) {
            NativeImage image = new NativeImage(HUE_W, SB_SIZE, false);
            for (int py = 0; py < SB_SIZE; py++) {
                float h = py / (float) (SB_SIZE - 1);
                int color = toNativeImageColor(hsbToRgb(h, 1f, 1f));
                for (int px = 0; px < HUE_W; px++) {
                    image.setPixelRGBA(px, py, color);
                }
            }
            this.hueTexture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(HUE_TEXTURE_LOCATION, this.hueTexture);
            this.hueTexture.upload();
        }
        ctx.blit(HUE_TEXTURE_LOCATION, x, y, HUE_W, SB_SIZE, 0, 0, HUE_W, SB_SIZE, HUE_W, SB_SIZE);
    }

    private static int toNativeImageColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return 0xFF000000 | (b << 16) | (g << 8) | r;
    }

    @Override
    public void removed() {
        super.removed();
        if (this.sbTexture != null) {
            this.sbTexture.close();
            this.sbTexture = null;
        }
        if (this.hueTexture != null) {
            this.hueTexture.close();
            this.hueTexture = null;
        }
    }

    private void drawPopupButton(GuiGraphics ctx, int x, int y, int w, int h, String label, boolean hovered) {
        ButtonTheme theme = FlatButtonWidget.DEFAULT_THEME;
        ctx.fill(x, y, x + w, y + h, hovered ? theme.bgHighlight : theme.bgDefault);
        ctx.drawCenteredString(this.font, label, x + w / 2, y + (h - 8) / 2, theme.themeLighter);
    }

    private boolean mouseInPopupDoneButton() {
        return lastMouseX >= popupX && lastMouseX < popupX + 60
                && lastMouseY >= popupDoneY() && lastMouseY < popupDoneY() + 18;
    }

    private boolean mouseInPopupCancelButton() {
        int totalW = SB_SIZE + POPUP_GAP + HUE_W;
        int cx = popupX + totalW - 60;
        return lastMouseX >= cx && lastMouseX < cx + 60
                && lastMouseY >= popupDoneY() && lastMouseY < popupDoneY() + 18;
    }

    private boolean mouseInPickButton() {
        int totalW = SB_SIZE + POPUP_GAP + HUE_W;
        int pickBtnY = popupY + SB_SIZE + POPUP_GAP + PREVIEW_H + POPUP_GAP;
        return lastMouseX >= popupX && lastMouseX < popupX + totalW
                && lastMouseY >= pickBtnY && lastMouseY < pickBtnY + PICK_BTN_H;
    }

    private int popupDoneY() {
        return popupY + SB_SIZE + POPUP_GAP + PREVIEW_H + POPUP_GAP + PICK_BTN_H + POPUP_GAP;
    }

    private double lastMouseX, lastMouseY;

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        lastMouseX = mx;
        lastMouseY = my;

        if (colorPickerOpen && eyedropperActive) {
            int sampled = sampleColorAt(mx, my);
            float[] hsb = rgbToHsb((sampled >> 16) & 0xFF, (sampled >> 8) & 0xFF, sampled & 0xFF);
            pickerHue = hsb[0];
            pickerSaturation = hsb[1];
            pickerBrightness = hsb[2];
            eyedropperActive = false;
            return true;
        }

        if (colorPickerOpen) {
            if (mx >= popupX && mx < popupX + SB_SIZE && my >= popupY && my < popupY + SB_SIZE) {
                draggingSB = true;
                updateSB(mx, my);
                return true;
            }
            int hueX = popupX + SB_SIZE + POPUP_GAP;
            if (mx >= hueX && mx < hueX + HUE_W && my >= popupY && my < popupY + SB_SIZE) {
                draggingHue = true;
                updateHue(my);
                return true;
            }
            if (mouseInPickButton()) {
                eyedropperActive = true;
                return true;
            }
            if (mouseInPopupDoneButton()) {
                int finalColor = hsbToRgb(pickerHue, pickerSaturation, pickerBrightness);
                colorPickerOpen = false;
                if (colorConfirmCallback != null) colorConfirmCallback.accept(finalColor);
                return true;
            }
            if (mouseInPopupCancelButton()) {
                colorPickerOpen = false;
                return true;
            }
            return true;
        }

        if (maxScroll > 0 && mouseInScrollbarThumb(mx, my)) {
            draggingScrollbar = true;
            return true;
        }

        // Los widgets fijos (previsualización, Presets, Done) tienen prioridad sobre el
        // contenido del acordeón, y este último solo puede recibir clics dentro del área
        // visible (recortada) para que un botón desplazado fuera de la vista no "robe" el clic
        // a lo que se ve encima de él (p. ej. la fila de botones inferior).
        for (AbstractWidget widget : this.staticWidgets) {
            if (widget.mouseClicked(mx, my, button)) {
                this.setFocused(widget);
                if (button == 0) this.setDragging(true);
                return true;
            }
        }

        if (my >= contentTop && my < contentBottom) {
            for (AbstractWidget widget : this.scrollWidgets) {
                if (widget.mouseClicked(mx, my, button)) {
                    this.setFocused(widget);
                    if (button == 0) this.setDragging(true);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        lastMouseX = mx;
        lastMouseY = my;

        if (colorPickerOpen && !eyedropperActive) {
            if (draggingSB) { updateSB(mx, my); return true; }
            if (draggingHue) { updateHue(my); return true; }
            return true;
        }
        if (colorPickerOpen) return true;

        if (draggingScrollbar) {
            updateScrollFromDrag(my);
            return true;
        }

        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (colorPickerOpen) {
            draggingSB = false;
            draggingHue = false;
            return true;
        }
        draggingScrollbar = false;
        return super.mouseReleased(mx, my, button);
    }

    private void updateSB(double mx, double my) {
        pickerSaturation = clamp01((float) (mx - popupX) / (SB_SIZE - 1));
        pickerBrightness = clamp01(1f - (float) (my - popupY) / (SB_SIZE - 1));
    }

    private void updateHue(double my) {
        pickerHue = clamp01((float) (my - popupY) / (SB_SIZE - 1));
    }

    private int sampleColorAt(double mouseX, double mouseY) {
        Minecraft client = Minecraft.getInstance();
        try (NativeImage image = Screenshot.takeScreenshot(client.getMainRenderTarget())) {
            double scale = client.getWindow().getGuiScale();
            int px = (int) Math.round(mouseX * scale);
            int py = (int) Math.round(mouseY * scale);
            px = Math.max(0, Math.min(image.getWidth() - 1, px));
            py = Math.max(0, Math.min(image.getHeight() - 1, py));

            int abgr = image.getPixelRGBA(px, py);
            int r = abgr & 0xFF;
            int g = (abgr >> 8) & 0xFF;
            int b = (abgr >> 16) & 0xFF;
            return (r << 16) | (g << 8) | b;
        } catch (Exception e) {
            return 0xFFFFFF;
        }
    }

    @Override
    public void onClose() {
        CustomizationConfig.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int hsbToRgb(float h, float s, float b) {
        if (s == 0f) {
            int v = (int) (b * 255);
            return (v << 16) | (v << 8) | v;
        }
        float sector = h * 6f;
        int i = (int) sector;
        float f = sector - i;
        float p = b * (1f - s);
        float q = b * (1f - s * f);
        float t = b * (1f - s * (1f - f));
        float r, g, bl;
        switch (i % 6) {
            case 0 -> { r = b; g = t; bl = p; }
            case 1 -> { r = q; g = b; bl = p; }
            case 2 -> { r = p; g = b; bl = t; }
            case 3 -> { r = p; g = q; bl = b; }
            case 4 -> { r = t; g = p; bl = b; }
            default -> { r = b; g = p; bl = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (bl * 255);
    }

    private static float[] rgbToHsb(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        float brightness = max;
        float saturation = max == 0 ? 0 : delta / max;
        float hue = 0;
        if (delta != 0) {
            if (max == rf) hue = (gf - bf) / delta % 6;
            else if (max == gf) hue = (bf - rf) / delta + 2;
            else hue = (rf - gf) / delta + 4;
            hue /= 6f;
            if (hue < 0) hue += 1f;
        }
        return new float[]{hue, saturation, brightness};
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static class IntSliderWidget extends AbstractWidget {
        private static final ButtonTheme THEME = FlatButtonWidget.DEFAULT_THEME;

        private final String translationKey;
        private final int min, max;
        private final IntSupplier getter;
        private final IntConsumer onChange;
        private boolean dragging;

        IntSliderWidget(int x, int y, int width, int height, String translationKey, int min, int max,
                        IntSupplier getter, IntConsumer onChange) {
            super(new Dim2i(x, y, width, height));
            this.translationKey = translationKey;
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.onChange = onChange;
        }

        private double getProgress() {
            int value = this.getter.getAsInt();
            return this.max == this.min ? 0 : Mth.clamp((value - this.min) / (double) (this.max - this.min), 0.0, 1.0);
        }

        private void setValueFromMouse(double mouseX) {
            double progress = Mth.clamp((mouseX - this.getX()) / (double) this.getWidth(), 0.0, 1.0);
            int value = this.min + (int) Math.round(progress * (this.max - this.min));
            this.onChange.accept(value);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            this.hovered = this.isMouseOver(mouseX, mouseY);

            int backgroundColor = this.hovered ? THEME.bgHighlight : THEME.bgDefault;
            this.drawRect(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), backgroundColor);

            int fillWidth = (int) Math.round(this.getProgress() * this.getWidth());
            if (fillWidth > 0) {
                this.drawRect(graphics, this.getX(), this.getY(), this.getX() + fillWidth, this.getLimitY(), Colors.BACKGROUND_HIGHLIGHT);
            }

            int thumbX = Mth.clamp(this.getX() + fillWidth, this.getX(), this.getLimitX() - 1);
            this.drawRect(graphics, thumbX, this.getY(), thumbX + 1, this.getLimitY(), THEME.theme);

            Component label = Component.translatable(this.translationKey, this.getter.getAsInt());
            int strWidth = this.font.width(label);
            this.drawString(graphics, label, this.getCenterX() - strWidth / 2, this.getCenterY() - this.font.lineHeight / 2, THEME.themeLighter);

            if (this.isFocused()) {
                this.drawBorder(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY(), Colors.BUTTON_BORDER);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && this.isMouseOver(mouseX, mouseY)) {
                this.dragging = true;
                this.setValueFromMouse(mouseX);
                this.playClickSound();
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
            if (this.dragging) {
                this.setValueFromMouse(mouseX);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            boolean was = this.dragging;
            this.dragging = false;
            return was;
        }
    }
}