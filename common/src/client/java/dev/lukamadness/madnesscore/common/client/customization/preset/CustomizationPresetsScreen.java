package dev.lukamadness.madnesscore.common.client.customization.preset;

import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;
import dev.lukamadness.madnesscore.common.client.customization.render.TemplatePreviewWidget;
import dev.lukamadness.madnesscore.common.client.gui.Colors;
import dev.lukamadness.madnesscore.common.client.gui.GuiTint;
import dev.lukamadness.madnesscore.common.client.gui.Layout;
import dev.lukamadness.madnesscore.common.client.gui.widgets.FlatButtonWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de presets de personalización (pelo, ojos, piel, energía), análoga a la de
 * GlowingEyes: se puede crear, seleccionar y borrar presets guardados.
 * <p>
 * Seleccionar un preset en la lista solo lo previsualiza en el panel de la derecha; el
 * personaje real (y los demás presets guardados) no se tocan hasta que se pulsa "Done" con
 * un preset seleccionado. "Cancel"/Esc descarta la selección y vuelve a la pantalla anterior
 * sin cambiar nada.
 */
public class CustomizationPresetsScreen extends Screen {
    private static final int MARGIN_X = 20;
    private static final int LIST_TOP = 32;
    private static final int LIST_WIDTH = 180;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 3;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_GAP = 6;
    private static final int SCROLL_STEP = 16;
    private static final int LIST_BUTTONS_HEIGHT = 20;
    private static final int LIST_BUTTONS_GAP = 6;
    private static final int BOTTOM_BAR_HEIGHT = 20;
    private static final int BOTTOM_MARGIN = 8;
    private static final int PREVIEW_GAP = 16;
    private static final int EMPTY_LABEL_COLOR = 0xFF888888;

    // Misma flechita (y mismos colores: fondo translúcido + texto blanco) que usa el tooltip
    // "anclado" de las opciones en la pestaña de configuración de Madness Core
    // (ver ScrollableTooltip), en vez de una caja opaca que persigue al cursor.
    private static final ResourceLocation TOOLTIP_ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("madnesscore", "textures/gui/sprites/tooltip_arrows.png");
    private static final int TOOLTIP_ARROW_WIDTH = 5;
    private static final int TOOLTIP_ARROW_SPRITE_WIDTH = 10;
    private static final int TOOLTIP_ARROW_HEIGHT = 9;

    private final Screen parent;
    private final CustomizationConfig liveConfig;
    private final CustomizationConfig previewConfig = new CustomizationConfig();

    private final List<PresetRowWidget> presetRows = new ArrayList<>();
    private CustomizationPreset selected;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int listTop, listBottom;
    private int scrollbarTrackX, scrollbarThumbY, scrollbarThumbHeight, scrollbarTrackHeight;
    private boolean draggingScrollbar = false;

    private FlatButtonWidget deleteButton;

    private boolean suppressBlur = false;

    public void setSuppressBlur(boolean suppressBlur) {
        this.suppressBlur = suppressBlur;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        if (this.suppressBlur) return;
        super.renderBlurredBackground(partialTick);
    }

    public CustomizationPresetsScreen(Screen parent, CustomizationConfig liveConfig) {
        super(Component.translatable("madnesscore.config.presets.title"));
        this.parent = parent;
        this.liveConfig = liveConfig;
    }

    @Override
    protected void init() {
        rebuildPresetWidgets();
    }

    private void rebuildPresetWidgets() {
        this.clearWidgets();
        this.presetRows.clear();
        refreshPreview();

        listTop = LIST_TOP;
        listBottom = this.height - BOTTOM_MARGIN - BOTTOM_BAR_HEIGHT - LIST_BUTTONS_GAP - LIST_BUTTONS_HEIGHT - LIST_BUTTONS_GAP;
        int viewportHeight = Math.max(0, listBottom - listTop);

        List<CustomizationPreset> presets = CustomizationPresetManager.getPresets();
        int totalHeight = presets.isEmpty() ? 0 : presets.size() * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
        maxScroll = Math.max(0, totalHeight - viewportHeight);
        scrollOffset = clampInt(scrollOffset, 0, maxScroll);
        computeScrollbarGeometry(viewportHeight, totalHeight);

        boolean needsScrollbar = maxScroll > 0;
        int rowWidth = LIST_WIDTH - (needsScrollbar ? SCROLLBAR_WIDTH + SCROLLBAR_GAP : 0);

        int y = listTop - scrollOffset;
        for (CustomizationPreset preset : presets) {
            if (y + ROW_HEIGHT > listTop && y < listBottom) {
                PresetRowWidget row = new PresetRowWidget(
                        new Dim2i(MARGIN_X, y, rowWidth, ROW_HEIGHT), preset, this::onRowClicked);
                row.setSelected(preset == this.selected);
                this.presetRows.add(row);
                this.addRenderableWidget(row);
            }
            y += ROW_HEIGHT + ROW_GAP;
        }

        int listButtonsY = listBottom + LIST_BUTTONS_GAP;
        int halfWidth = (LIST_WIDTH - 4) / 2;
        this.addRenderableWidget(new FlatButtonWidget(
                new Dim2i(MARGIN_X, listButtonsY, halfWidth, LIST_BUTTONS_HEIGHT),
                Component.translatable("madnesscore.config.presets.create"),
                this::openCreateScreen, true, false));

        this.deleteButton = new FlatButtonWidget(
                new Dim2i(MARGIN_X + halfWidth + 4, listButtonsY, halfWidth, LIST_BUTTONS_HEIGHT),
                Component.translatable("madnesscore.config.presets.delete"),
                this::openDeleteConfirm, true, false);
        this.deleteButton.setEnabled(this.selected != null);
        this.addRenderableWidget(this.deleteButton);

        int previewLeftEdge = MARGIN_X + LIST_WIDTH + PREVIEW_GAP;
        int maxPreviewWidth = Math.max(0, this.width - previewLeftEdge - MARGIN_X);
        this.addRenderableWidget(new TemplatePreviewWidget(
                this.width - MARGIN_X, listTop, this.previewConfig, maxPreviewWidth));

        this.addRenderableWidget(new FlatButtonWidget(
                new Dim2i(MARGIN_X, this.height - BOTTOM_MARGIN - BOTTOM_BAR_HEIGHT, 100, BOTTOM_BAR_HEIGHT),
                Component.translatable("madnesscore.config.presets.cancel"), this::cancel, true, false));

        this.addRenderableWidget(new FlatButtonWidget(
                new Dim2i(this.width - MARGIN_X - 100, this.height - BOTTOM_MARGIN - BOTTOM_BAR_HEIGHT, 100, BOTTOM_BAR_HEIGHT),
                Component.translatable("gui.done"), this::confirm, true, false));
    }

    /** Refleja en el panel de previsualización el preset seleccionado, o el look actual si no hay ninguno. */
    private void refreshPreview() {
        if (this.selected != null) {
            this.selected.applyTo(this.previewConfig);
        } else {
            CustomizationPreset.capture("", "", this.liveConfig).applyTo(this.previewConfig);
        }
    }

    private void onRowClicked(CustomizationPreset preset) {
        this.selected = (this.selected == preset) ? null : preset;
        rebuildPresetWidgets();
    }

    private void openCreateScreen() {
        if (this.minecraft == null) return;
        this.minecraft.setScreen(new CreatePresetScreen(this, this.liveConfig, created -> {
            this.selected = created;
        }));
    }

    private void openDeleteConfirm() {
        if (this.selected == null) return;
        CustomizationPreset toDelete = this.selected;
        ConfirmDeletePresetScreen.askToDelete(this, toDelete.name).thenAccept(confirmed -> {
            if (confirmed) {
                CustomizationPresetManager.remove(toDelete);
                if (this.selected == toDelete) {
                    this.selected = null;
                }
            }
            rebuildPresetWidgets();
        });
    }

    /** Aplica de verdad el preset seleccionado al personaje y lo guarda; sin selección, no cambia nada. */
    private void confirm() {
        if (this.selected != null) {
            this.selected.applyTo(this.liveConfig);
            CustomizationConfig.save();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void cancel() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void onClose() {
        this.cancel();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);

        ctx.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);

        if (CustomizationPresetManager.getPresets().isEmpty()) {
            ctx.drawString(this.font, Component.translatable("madnesscore.config.presets.empty"),
                    MARGIN_X, listTop, EMPTY_LABEL_COLOR, false);
        }

        if (maxScroll > 0) {
            drawScrollbar(ctx);
        }

        renderHoveredTooltip(ctx, mouseX, mouseY);
    }

    private void renderHoveredTooltip(GuiGraphics ctx, int mouseX, int mouseY) {
        for (PresetRowWidget row : this.presetRows) {
            if (row.isHovered()) {
                String description = row.getPreset().description;
                if (description != null && !description.isBlank()) {
                    renderThemedTooltip(ctx, description, row);
                }
                return;
            }
        }
    }

    /**
     * Tooltip anclado junto a la fila (con la misma flechita, fondo translúcido y texto blanco)
     * que usan las descripciones de opciones en la pestaña de configuración de Madness Core, en
     * vez de una caja que persigue al cursor.
     */
    private void renderThemedTooltip(GuiGraphics ctx, String text, PresetRowWidget row) {
        int padding = Layout.INNER_MARGIN - 1;
        int lineHeight = this.font.lineHeight + Layout.TEXT_LINE_SPACING;

        int boxX = row.getLimitX() + TOOLTIP_ARROW_WIDTH;
        int availableWidth = this.width - boxX - MARGIN_X;
        int boxWidth = Mth.clamp(availableWidth, Layout.MIN_TOOLTIP_WIDTH, Layout.MAX_TOOLTIP_WIDTH);
        int maxTextWidth = boxWidth - padding * 2;

        List<FormattedCharSequence> lines = this.font.split(Component.literal(text), maxTextWidth);

        int textWidth = 0;
        for (FormattedCharSequence line : lines) {
            textWidth = Math.max(textWidth, this.font.width(line));
        }
        boxWidth = Math.min(boxWidth, textWidth + padding * 2);
        int boxHeight = lines.size() * lineHeight - Layout.TEXT_LINE_SPACING + padding * 2;

        int minBoxY = LIST_TOP;
        int maxBoxY = this.height - BOTTOM_MARGIN - BOTTOM_BAR_HEIGHT - boxHeight;
        int boxY = Mth.clamp(row.getCenterY() - boxHeight / 2, minBoxY, Math.max(minBoxY, maxBoxY));

        int arrowX = boxX - TOOLTIP_ARROW_WIDTH;
        int arrowY = row.getCenterY() - (TOOLTIP_ARROW_HEIGHT / 2);

        ctx.pose().pushPose();
        ctx.pose().translate(0, 0, 400);

        GuiTint.withTint(Colors.BACKGROUND_LIGHT, () ->
                ctx.blit(TOOLTIP_ARROW_TEXTURE, arrowX, arrowY, TOOLTIP_ARROW_WIDTH, 0,
                        TOOLTIP_ARROW_WIDTH, TOOLTIP_ARROW_HEIGHT, TOOLTIP_ARROW_SPRITE_WIDTH, TOOLTIP_ARROW_HEIGHT));

        ctx.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, Colors.BACKGROUND_LIGHT);
        for (int i = 0; i < lines.size(); i++) {
            ctx.drawString(this.font, lines.get(i), boxX + padding, boxY + padding + i * lineHeight, Colors.FOREGROUND, false);
        }
        ctx.pose().popPose();
    }

    private void computeScrollbarGeometry(int viewportHeight, int totalHeight) {
        scrollbarTrackX = MARGIN_X + LIST_WIDTH - SCROLLBAR_WIDTH;
        scrollbarTrackHeight = viewportHeight;
        if (scrollbarTrackHeight <= 0 || totalHeight <= 0 || maxScroll <= 0) {
            scrollbarThumbHeight = 0;
            scrollbarThumbY = listTop;
            return;
        }
        scrollbarThumbHeight = clampInt(
                (int) ((float) scrollbarTrackHeight / totalHeight * scrollbarTrackHeight),
                16, scrollbarTrackHeight);
        scrollbarThumbY = listTop + (int) ((float) scrollOffset / maxScroll * (scrollbarTrackHeight - scrollbarThumbHeight));
    }

    private void drawScrollbar(GuiGraphics ctx) {
        if (scrollbarTrackHeight <= 0) return;
        ctx.fill(scrollbarTrackX, listTop, scrollbarTrackX + SCROLLBAR_WIDTH, listBottom, 0x40FFFFFF);
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
        double relative = (my - listTop - scrollbarThumbHeight / 2.0) / usableTrack;
        scrollOffset = clampInt((int) Math.round(relative * maxScroll), 0, maxScroll);
        rebuildPresetWidgets();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll <= 0) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        scrollOffset = clampInt(scrollOffset - (int) (verticalAmount * SCROLL_STEP), 0, maxScroll);
        rebuildPresetWidgets();
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (maxScroll > 0 && mouseInScrollbarThumb(mx, my)) {
            draggingScrollbar = true;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingScrollbar) {
            updateScrollFromDrag(my);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mx, my, button);
    }

    private static int clampInt(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}