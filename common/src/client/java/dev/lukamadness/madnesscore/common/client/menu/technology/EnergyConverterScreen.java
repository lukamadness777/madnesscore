package dev.lukamadness.madnesscore.common.client.menu.technology;

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

        drawBar(guiGraphics, HEAT_BAR_TEXTURE, x + HEAT_BAR_X, y + BAR_Y, menu.getTemperature(), menu.getMaxTemperature());
        drawBar(guiGraphics, ENERGY_BAR_TEXTURE, x + ENERGY_BAR_X, y + BAR_Y, menu.getEnergy(), menu.getEnergyCapacity());
    }

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
                                    HeatFormat.formatTemperature(menu.getTemperature()),
                                    HeatFormat.formatTemperature(menu.getMaxTemperature()))
                            .withStyle(ChatFormatting.GOLD)
            ), mouseX, mouseY);
        } else if (isHovering(ENERGY_BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY)) {
            String current = hasShiftDown()
                    ? EnergyFormat.formatExact(menu.getEnergy())
                    : EnergyFormat.format(menu.getEnergy());
            String max = hasShiftDown()
                    ? EnergyFormat.formatExact(menu.getEnergyCapacity())
                    : EnergyFormat.format(menu.getEnergyCapacity());
            guiGraphics.renderComponentTooltip(f, List.of(
                    Component.translatable("tooltip.madnesscore.energy_stored", current, max)
                            .withStyle(ChatFormatting.AQUA)
            ), mouseX, mouseY);
        }
    }
}
