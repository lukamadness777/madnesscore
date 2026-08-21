package dev.lukamadness.madnesscore.common.client.technology;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.technology.energy.EnergyFormat;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFormat;
import dev.lukamadness.madnesscore.common.content.technology.screen.EnergyConverterScreenHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.ChatFormatting;

import java.util.List;

/**
 * Pantalla puramente informativa del Energy Converter: 109x90, se centra sola (mismo
 * comportamiento default de AbstractContainerScreen con imageWidth/imageHeight chicos),
 * sin slots — no hay nada que craftear ni mover, solo mirar cuánto Heat entra y cuánta
 * Energy sale. Misma barra de siempre (heat_bar.png / energy_bar.png, 13x46, se llena
 * de abajo hacia arriba) que ya usan HeatGeneratorScreen y AlloySmelteryScreen.
 */
public class EnergyConverterScreen extends AbstractContainerScreen<EnergyConverterScreenHandler> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/container/energy_converter.png");
    private static final ResourceLocation HEAT_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/heat_bar.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/energy_bar.png");

    private static final int BAR_WIDTH = 13;
    private static final int BAR_HEIGHT = 46;

    private static final int HEAT_BAR_X = 17;
    private static final int ENERGY_BAR_X = 79;
    private static final int BAR_Y = 22;

    public EnergyConverterScreen(EnergyConverterScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 109;
        imageHeight = 90;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        drawBar(guiGraphics, HEAT_BAR_TEXTURE, x + HEAT_BAR_X, y + BAR_Y, menu.getHeat(), menu.getHeatCapacity());
        drawBar(guiGraphics, ENERGY_BAR_TEXTURE, x + ENERGY_BAR_X, y + BAR_Y, menu.getEnergy(), menu.getEnergyCapacity());
    }

    /** Dibuja una barra de 13x46 rellena de abajo hacia arriba, igual que heat_bar en el resto de pantallas. */
    private void drawBar(GuiGraphics guiGraphics, ResourceLocation texture, int screenX, int screenY, int value, int capacity) {
        int filled = capacity == 0 ? 0 : value * BAR_HEIGHT / capacity;
        if (filled <= 0) return;

        guiGraphics.blit(
                texture,
                screenX, screenY + (BAR_HEIGHT - filled),
                0, BAR_HEIGHT - filled,
                BAR_WIDTH, filled,
                BAR_WIDTH, BAR_HEIGHT
        );
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        Font f = this.font;
        if (isHovering(HEAT_BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(f, List.of(
                    Component.translatable("tooltip.madnesscore.heat_stored",
                                    HeatFormat.format(menu.getHeat()), HeatFormat.format(menu.getHeatCapacity()))
                            .withStyle(ChatFormatting.GOLD)
            ), mouseX, mouseY);
        } else if (isHovering(ENERGY_BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(f, List.of(
                    Component.translatable("tooltip.madnesscore.energy_stored",
                                    EnergyFormat.format(menu.getEnergy()), EnergyFormat.format(menu.getEnergyCapacity()))
                            .withStyle(ChatFormatting.AQUA)
            ), mouseX, mouseY);
        }
    }
}