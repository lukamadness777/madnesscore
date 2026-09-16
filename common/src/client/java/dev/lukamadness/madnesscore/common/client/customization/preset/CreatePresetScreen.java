package dev.lukamadness.madnesscore.common.client.customization.preset;

import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;
import dev.lukamadness.madnesscore.common.client.gui.Colors;
import dev.lukamadness.madnesscore.common.client.gui.widgets.FlatButtonWidget;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.function.Consumer;

public class CreatePresetScreen extends Screen {
    private static final int WIDTH = 260;
    private static final int HEIGHT = 130;
    private static final int PADDING_X = 20;
    private static final int FIELD_HEIGHT = 20;
    private static final int FIELD_GAP = 8;
    private static final int LABEL_GAP = 11;
    private static final int BUTTON_HEIGHT = 20;
    private static final int MAX_NAME_LENGTH = 32;
    private static final int MAX_DESCRIPTION_LENGTH = 80;

    private final Screen parent;
    private final CustomizationConfig source;
    private final Consumer<CustomizationPreset> onCreated;

    private int guiLeft, guiTop;
    private EditBox nameField;
    private EditBox descriptionField;
    private FlatButtonWidget createButton;

    public CreatePresetScreen(Screen parent, CustomizationConfig source, Consumer<CustomizationPreset> onCreated) {
        super(Component.translatable("madnesscore.config.presets.create.title"));
        this.parent = parent;
        this.source = source;
        this.onCreated = onCreated;
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width - WIDTH) / 2;
        this.guiTop = (this.height - HEIGHT) / 2;

        int fieldX = this.guiLeft + PADDING_X;
        int fieldWidth = WIDTH - PADDING_X * 2;
        int y = this.guiTop + 24;

        this.nameField = new EditBox(this.font, fieldX, y, fieldWidth, FIELD_HEIGHT, Component.empty());
        this.nameField.setMaxLength(MAX_NAME_LENGTH);
        // Sin borde vainilla: el fondo plano se pinta a mano en render(), igual que en el
        // SearchWidget de la pestaña de configuración de Madness Core.
        this.nameField.setBordered(false);
        this.nameField.setHint(Component.translatable("madnesscore.config.presets.name_hint")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        this.nameField.setResponder(value -> this.createButton.setEnabled(!value.isBlank()));
        this.addRenderableWidget(this.nameField);
        this.setInitialFocus(this.nameField);

        y += FIELD_HEIGHT + LABEL_GAP + FIELD_GAP;

        this.descriptionField = new EditBox(this.font, fieldX, y, fieldWidth, FIELD_HEIGHT, Component.empty());
        this.descriptionField.setMaxLength(MAX_DESCRIPTION_LENGTH);
        this.descriptionField.setBordered(false);
        this.descriptionField.setHint(Component.translatable("madnesscore.config.presets.description_hint")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        this.addRenderableWidget(this.descriptionField);

        int buttonWidth = (fieldWidth - 8) / 2;
        int buttonY = this.guiTop + HEIGHT - BUTTON_HEIGHT - 12;

        this.createButton = new FlatButtonWidget(
                new Dim2i(fieldX, buttonY, buttonWidth, BUTTON_HEIGHT),
                Component.translatable("madnesscore.config.presets.create"),
                this::createPreset, true, false);
        this.createButton.setEnabled(false);
        this.addRenderableWidget(this.createButton);

        this.addRenderableWidget(new FlatButtonWidget(
                new Dim2i(fieldX + buttonWidth + 8, buttonY, buttonWidth, BUTTON_HEIGHT),
                Component.translatable("madnesscore.config.presets.cancel"),
                this::cancel, true, false));
    }

    private void createPreset() {
        String name = this.nameField.getValue().trim();
        if (name.isEmpty()) {
            return;
        }
        String description = this.descriptionField.getValue().trim();
        CustomizationPreset preset = CustomizationPresetManager.create(name, description, this.source);
        if (this.onCreated != null) {
            this.onCreated.accept(preset);
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
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // No-op: si dejamos que Screen#renderBackground corra su implementación default, Minecraft
        // aplica el shader de blur de fondo (menú/backgroundBlur) detrás de esta pantalla. Como esta
        // pantalla ya pinta su propio velo oscuro + panel a mano en render(), ese blur se ve como una
        // capa borrosa de más, pegada a los bordes del popup. Anulándolo acá evitamos que se dispare.
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        if (this.parent != null) {
            ctx.pose().pushPose();
            ctx.pose().translate(0, 0, -100);
            this.parent.render(ctx, -1, -1, delta);
            ctx.pose().popPose();
            ctx.flush();
        }
        ctx.fill(0, 0, this.width, this.height, 0x80000000);
        ctx.fill(this.guiLeft, this.guiTop, this.guiLeft + WIDTH, this.guiTop + HEIGHT, Colors.BACKGROUND_LIGHT);
        
        ctx.drawCenteredString(this.font, this.title, this.guiLeft + WIDTH / 2, this.guiTop + 8, 0xFFFFFFFF);

        int fieldX = this.guiLeft + PADDING_X;
        ctx.drawString(this.font, Component.translatable("madnesscore.config.presets.name_label"),
                fieldX, this.nameField.getY() - LABEL_GAP, 0xFFAAAAAA, false);
        ctx.drawString(this.font, Component.translatable("madnesscore.config.presets.description_label"),
                fieldX, this.descriptionField.getY() - LABEL_GAP, 0xFFAAAAAA, false);

        // Fondo plano detrás de los textbox, igual que el de la barra de búsqueda de la
        // pestaña de configuración (los EditBox van sin borde, con setBordered(false)).
        drawFieldBackground(ctx, this.nameField);
        drawFieldBackground(ctx, this.descriptionField);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private static void drawFieldBackground(GuiGraphics ctx, EditBox field) {
        ctx.fill(field.getX(), field.getY(), field.getX() + field.getWidth(), field.getY() + field.getHeight(),
                Colors.BACKGROUND_DEFAULT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.cancel();
    }
}