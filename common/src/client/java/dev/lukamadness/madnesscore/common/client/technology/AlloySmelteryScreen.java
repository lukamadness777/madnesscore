package dev.lukamadness.madnesscore.common.client.technology;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFormat;
import dev.lukamadness.madnesscore.common.content.technology.screen.AlloySmelteryScreenHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.ChatFormatting;

import java.util.List;

/**
 * Recreación de IndustrialSmelterScreen (Madness Core normal), renombrado a Alloy
 * Smeltery. Mismo layout de fondo (176x166) y misma barra de progreso "boca" de fundido,
 * pero con 4 slots de input y 4 de output (ver AlloySmelteryScreenHandler para las
 * coordenadas exactas de cada slot) en vez de uno solo. Corre a Heat directo (no a
 * Energy convertida) y exige AlloySmelteryBlockEntity.MIN_TEMPERATURE (800°C) — la
 * barra se pinta gris cuando el sistema todavía no llegó a esa temperatura.
 */
public class AlloySmelteryScreen extends AbstractContainerScreen<AlloySmelteryScreenHandler> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/container/industrial_smelter.png");
    private static final ResourceLocation LIT_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/industrial_smelter/lit_progress.png");
    private static final ResourceLocation HEAT_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/heat_bar.png");

    private static final int HEAT_BAR_WIDTH = 13;
    private static final int HEAT_BAR_HEIGHT = 46;

    // Mismo PNG que el viejo Industrial Smelter (24x16)
    private static final int LIT_PROGRESS_WIDTH = 24;
    private static final int LIT_PROGRESS_HEIGHT = 16;

    public AlloySmelteryScreen(AlloySmelteryScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        // Progreso de fundido: crece de izquierda a derecha, centrado entre los 4
        // inputs (terminan en x=49) y los 4 outputs (empiezan en x=95).
        int progressWidth = getScaled(menu.getProgress(), menu.getMaxProgress(), LIT_PROGRESS_WIDTH);
        if (progressWidth > 0) {
            guiGraphics.blit(
                    LIT_PROGRESS_TEXTURE,
                    x + 59, y + 36,
                    0, 0,
                    progressWidth, LIT_PROGRESS_HEIGHT,
                    LIT_PROGRESS_WIDTH, LIT_PROGRESS_HEIGHT
            );
        }

        int filled = getScaled(menu.getHeat(), menu.getHeatCapacity(), HEAT_BAR_HEIGHT);
        if (filled > 0) {
            guiGraphics.blit(
                    HEAT_BAR_TEXTURE,
                    x + 146, y + 22 + (HEAT_BAR_HEIGHT - filled),
                    0, HEAT_BAR_HEIGHT - filled,
                    HEAT_BAR_WIDTH, filled,
                    HEAT_BAR_WIDTH, HEAT_BAR_HEIGHT
            );
        }
    }

    private int getScaled(int value, int max, int size) {
        return max == 0 ? 0 : value * size / max;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(146, 22, HEAT_BAR_WIDTH, HEAT_BAR_HEIGHT, mouseX, mouseY)) {
            Font f = this.font;
            guiGraphics.renderComponentTooltip(f, List.of(
                    Component.translatable("tooltip.madnesscore.heat_stored",
                                    HeatFormat.format(menu.getHeat()), HeatFormat.format(menu.getHeatCapacity()))
                            .withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.madnesscore.heat_input",
                                    HeatFormat.format(AlloySmelteryBlockEntity.MAX_HEAT_RECEIVE * 20))
                            .withStyle(ChatFormatting.GREEN),
                    Component.translatable("tooltip.madnesscore.min_temperature",
                                    (int) AlloySmelteryBlockEntity.MIN_TEMPERATURE)
                            .withStyle(ChatFormatting.RED)
            ), mouseX, mouseY);
        }
    }
}