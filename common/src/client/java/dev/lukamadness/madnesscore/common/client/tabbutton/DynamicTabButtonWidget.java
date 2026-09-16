package dev.lukamadness.madnesscore.common.client.tabbutton;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DynamicTabButtonWidget extends AbstractWidget {

    public enum Variant {
        DEFAULT,
        DOWN
    }

    private static final ResourceLocation ACTIVE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "dynamictab/custom_tab_active");
    private static final ResourceLocation INACTIVE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "dynamictab/custom_tab_inactive");
    private static final ResourceLocation ACTIVE_DOWN =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "dynamictab/custom_tab_active_down");
    private static final ResourceLocation INACTIVE_DOWN =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "dynamictab/custom_tab_inactive_down");

    private final Runnable onPress;
    private final ItemStack displayItemStack;
    private final boolean active;
    private final Variant variant;
    private final ResourceLocation activeTexture;
    private final ResourceLocation inactiveTexture;
    private final ResourceLocation activeDownTexture;
    private final ResourceLocation inactiveDownTexture;

    public DynamicTabButtonWidget(int x, int y, int width, int height, Component message,
                                   ItemStack displayItemStack, boolean active, Runnable onPress) {
        this(x, y, width, height, message, displayItemStack, active, Variant.DEFAULT, onPress);
    }

    public DynamicTabButtonWidget(int x, int y, int width, int height, Component message,
                                   ItemStack displayItemStack, boolean active, Variant variant, Runnable onPress) {
        this(x, y, width, height, message, displayItemStack, active, variant,
                ACTIVE, INACTIVE, ACTIVE_DOWN, INACTIVE_DOWN, onPress);
    }

    public DynamicTabButtonWidget(int x, int y, int width, int height, Component message,
                                   ItemStack displayItemStack, boolean active, Variant variant,
                                   ResourceLocation activeTexture, ResourceLocation inactiveTexture,
                                   ResourceLocation activeDownTexture, ResourceLocation inactiveDownTexture,
                                   Runnable onPress) {
        super(x, y, width, height, message);
        this.displayItemStack = displayItemStack;
        this.active = active;
        this.variant = variant;
        this.activeTexture = activeTexture;
        this.inactiveTexture = inactiveTexture;
        this.activeDownTexture = activeDownTexture;
        this.inactiveDownTexture = inactiveDownTexture;
        this.onPress = onPress;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.onPress.run();
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = switch (this.variant) {
            case DOWN -> this.active ? this.activeDownTexture : this.inactiveDownTexture;
            case DEFAULT -> this.active ? this.activeTexture : this.inactiveTexture;
        };

        context.blitSprite(texture, this.getX(), this.getY() + 1, this.getWidth(), this.getHeight());

        context.renderItem(
                this.displayItemStack,
                this.getX() + this.getWidth() / 2 - 8,
                this.getY() + this.getHeight() / 2 - 8
        );

        if (this.isHovered()) {
            context.renderTooltip(
                    Minecraft.getInstance().font,
                    this.getMessage(),
                    mouseX, mouseY
            );
        }
    }
}
