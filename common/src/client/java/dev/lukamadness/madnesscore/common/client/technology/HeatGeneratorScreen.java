package dev.lukamadness.madnesscore.common.client.technology;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFormat;
import dev.lukamadness.madnesscore.common.content.technology.screen.HeatGeneratorScreenHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.ChatFormatting;

import java.util.List;

/**
 * Recreación de CoalGeneratorScreen (Madness Core normal). Mismo layout y misma
 * textura base de fondo, pero la barra lateral y el tooltip muestran Heat en vez de
 * Energy — son recursos distintos, ver HeatGeneratorBlockEntity.
 */
public class HeatGeneratorScreen extends AbstractContainerScreen<HeatGeneratorScreenHandler> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/container/heat_generator.png");
    private static final ResourceLocation LIT_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/heat_generator/lit_progress.png");
    private static final ResourceLocation HEAT_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/heat_bar.png");

    private static final int LIT_PROGRESS_WIDTH = 14;
    private static final int LIT_PROGRESS_HEIGHT = 14;

    private static final int HEAT_BAR_WIDTH = 13;
    private static final int HEAT_BAR_HEIGHT = 46;

    public HeatGeneratorScreen(HeatGeneratorScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        // Fuego: crece/decrece de abajo hacia arriba según el fuel restante
        if (menu.isBurning()) {
            int litHeight = getLitProgress();
            guiGraphics.blit(
                    LIT_PROGRESS_TEXTURE,
                    x + 80, y + 20 + (LIT_PROGRESS_HEIGHT - litHeight),
                    0, LIT_PROGRESS_HEIGHT - litHeight,
                    LIT_PROGRESS_WIDTH, litHeight,
                    LIT_PROGRESS_WIDTH, LIT_PROGRESS_HEIGHT
            );
        }

        drawHeatBar(guiGraphics, x, y, menu.getHeat(), menu.getHeatCapacity());
    }

    /**
     * Dibuja el sprite de heat_bar.png (13x46) relleno de abajo hacia arriba, mismo
     * layout que la vieja barra de energy_bar.png del Coal Generator.
     */
    private void drawHeatBar(GuiGraphics guiGraphics, int screenX, int screenY, int heat, int capacity) {
        int filled = capacity == 0 ? 0 : heat * HEAT_BAR_HEIGHT / capacity;
        if (filled <= 0) return;

        guiGraphics.blit(
                HEAT_BAR_TEXTURE,
                screenX + 146, screenY + 22 + (HEAT_BAR_HEIGHT - filled),
                0, HEAT_BAR_HEIGHT - filled,
                HEAT_BAR_WIDTH, filled,
                HEAT_BAR_WIDTH, HEAT_BAR_HEIGHT
        );
    }

    private int getLitProgress() {
        int total = menu.getBurnTimeTotal();
        return total == 0 ? 0 : menu.getBurnTime() * LIT_PROGRESS_HEIGHT / total;
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
                            .withStyle(ChatFormatting.GOLD)
            ), mouseX, mouseY);
        }
    }
}