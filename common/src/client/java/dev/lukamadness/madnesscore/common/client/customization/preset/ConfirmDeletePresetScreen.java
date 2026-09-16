package dev.lukamadness.madnesscore.common.client.customization.preset;

import dev.lukamadness.madnesscore.common.client.gui.Colors;
import dev.lukamadness.madnesscore.common.client.gui.widgets.FlatButtonWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Pequeño popup de confirmación antes de borrar un preset, análogo al {@code ConfirmDeletionScreen}
 * de GlowingEyes.
 */
public class ConfirmDeletePresetScreen extends Screen {
    private static final int WIDTH = 220;
    private static final int HEIGHT = 90;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 6;
    private static final int PADDING_X = 16;

    private final Screen parent;
    private final String presetName;
    private final CompletableFuture<Boolean> future = new CompletableFuture<>();

    private int guiLeft, guiTop;

    private ConfirmDeletePresetScreen(Screen parent, String presetName) {
        super(Component.translatable("madnesscore.config.presets.delete.title"));
        this.parent = parent;
        this.presetName = presetName;
    }

    public static CompletableFuture<Boolean> askToDelete(Screen parent, String presetName) {
        ConfirmDeletePresetScreen screen = new ConfirmDeletePresetScreen(parent, presetName);
        Minecraft.getInstance().setScreen(screen);
        return screen.future;
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width - WIDTH) / 2;
        this.guiTop = (this.height - HEIGHT) / 2;

        int buttonWidth = (WIDTH - PADDING_X * 2 - BUTTON_SPACING) / 2;
        int buttonY = this.guiTop + HEIGHT - BUTTON_HEIGHT - 12;

        this.addRenderableWidget(new FlatButtonWidget(
                new Dim2i(this.guiLeft + PADDING_X, buttonY, buttonWidth, BUTTON_HEIGHT),
                Component.translatable("madnesscore.config.presets.confirm"),
                () -> this.finish(true), true, false));

        this.addRenderableWidget(new FlatButtonWidget(
                new Dim2i(this.guiLeft + PADDING_X + buttonWidth + BUTTON_SPACING, buttonY, buttonWidth, BUTTON_HEIGHT),
                Component.translatable("madnesscore.config.presets.cancel"),
                () -> this.finish(false), true, false));
    }

    private void finish(boolean confirmed) {
        this.future.complete(confirmed);
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // No-op: mismo fix que CreatePresetScreen. Sin esto, Screen#renderBackground dispara el
        // shader de blur de fondo de Minecraft detrás del popup, y como el popup ya pinta su propio
        // velo oscuro + panel a mano en render(), el blur se ve como una capa borrosa de más.
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        if (this.parent != null) {
            ctx.pose().pushPose();
            ctx.pose().translate(0, 0, -100);
            this.parent.render(ctx, -1, -1, delta);
            ctx.pose().popPose();
            // Ver el comentario equivalente en CreatePresetScreen: sin este flush, el texto
            // encolado del padre se dibuja al final del frame, por encima del velo oscuro.
            ctx.flush();
        }
        ctx.fill(0, 0, this.width, this.height, 0x80000000);
        ctx.fill(this.guiLeft, this.guiTop, this.guiLeft + WIDTH, this.guiTop + HEIGHT, Colors.BACKGROUND_LIGHT);

        Component question = Component.translatable("madnesscore.config.presets.delete.confirm", this.presetName);
        List<net.minecraft.util.FormattedCharSequence> lines = this.font.split(question, WIDTH - PADDING_X * 2);
        int textY = this.guiTop + 16;
        for (net.minecraft.util.FormattedCharSequence line : lines) {
            ctx.drawCenteredString(this.font, line, this.guiLeft + WIDTH / 2, textY, 0xFFFFFFFF);
            textY += this.font.lineHeight + 2;
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.finish(false);
    }
}