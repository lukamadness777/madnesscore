package dev.lukamadness.madnesscore.common.client.menu.technology;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.technology.blocks.CompressorBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.energy.EnergyFormat;
import dev.lukamadness.madnesscore.common.content.technology.screen.CompressorScreenHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class CompressorScreen extends AbstractContainerScreen<CompressorScreenHandler> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/container/compressor.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/energy_bar.png");
    private static final ResourceLocation LIT_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/sprites/container/compressor/lit_progress.png");

    private static final int ENERGY_BAR_WIDTH = 13;
    private static final int ENERGY_BAR_HEIGHT = 46;

    private static final int LIT_PROGRESS_WIDTH = 18;
    private static final int LIT_PROGRESS_HEIGHT = 18;

    public CompressorScreen(CompressorScreenHandler menu, Inventory inventory, Component title) {
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
                    x + 68, y + 36,
                    0, 0,
                    progressWidth, LIT_PROGRESS_HEIGHT,
                    LIT_PROGRESS_WIDTH, LIT_PROGRESS_HEIGHT
            );
        }

        int filled = getScaled(menu.getEnergy(), menu.getEnergyCapacity(), ENERGY_BAR_HEIGHT);
        if (filled > 0) {
            guiGraphics.blit(
                    ENERGY_BAR_TEXTURE,
                    x + 146, y + 22 + (ENERGY_BAR_HEIGHT - filled),
                    0, ENERGY_BAR_HEIGHT - filled,
                    ENERGY_BAR_WIDTH, filled,
                    ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT
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

        if (isHovering(146, 22, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT, mouseX, mouseY)) {
            Font f = this.font;

            String current = hasShiftDown()
                    ? EnergyFormat.formatExact(menu.getEnergy())
                    : EnergyFormat.format(menu.getEnergy());
            String max = hasShiftDown()
                    ? EnergyFormat.formatExact(menu.getEnergyCapacity())
                    : EnergyFormat.format(menu.getEnergyCapacity());
            guiGraphics.renderComponentTooltip(f, List.of(
                    Component.translatable("tooltip.madnesscore.energy_stored", current, max)
                            .withStyle(ChatFormatting.AQUA),
                    Component.translatable("tooltip.madnesscore.energy_input",
                                    EnergyFormat.format(CompressorBlockEntity.MAX_RECEIVE_PER_SECOND))
                            .withStyle(ChatFormatting.GREEN)
            ), mouseX, mouseY);
        }
    }
}
