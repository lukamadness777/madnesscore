package dev.lukamadness.madnesscore.common.client.menu.technology;

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

public class AlloySmelteryScreen extends AbstractContainerScreen<AlloySmelteryScreenHandler> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/container/industrial_smelter.png");
    private static final ResourceLocation LIT_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/industrial_smelter/lit_progress.png");
    private static final ResourceLocation HEAT_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/heat_bar.png");

    private static final int HEAT_BAR_WIDTH = 13;
    private static final int HEAT_BAR_HEIGHT = 46;

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

        int filled = getScaled(menu.getTemperature(), menu.getMaxTemperature(), HEAT_BAR_HEIGHT);
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
                                    HeatFormat.formatTemperature(menu.getTemperature()),
                                    HeatFormat.formatTemperature(menu.getMaxTemperature()))
                            .withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.madnesscore.min_temperature",
                                    (int) AlloySmelteryBlockEntity.MIN_TEMPERATURE)
                            .withStyle(ChatFormatting.RED)
            ), mouseX, mouseY);
        }
    }
}
