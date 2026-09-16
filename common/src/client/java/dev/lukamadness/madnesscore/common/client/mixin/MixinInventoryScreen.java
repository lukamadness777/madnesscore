package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.mixin.accessor.AbstractContainerScreenAccessor;
import dev.lukamadness.madnesscore.common.client.tabbutton.DynamicTabButtonManager;
import dev.lukamadness.madnesscore.common.client.slots.ui.SlotHoverManager;
import dev.lukamadness.madnesscore.common.client.slots.ui.SlotHoverScreen;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.slots.gui.Point;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class MixinInventoryScreen extends Screen implements SlotHoverScreen {
    protected MixinInventoryScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void madnesscore$onInit(CallbackInfo ci) {
        SlotHoverManager.init(this);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void madnesscore$addDynamicTabButtons(CallbackInfo ci) {
        DynamicTabButtonManager.installButtons(this.madnesscore$getX(), this.madnesscore$getY(), this::addRenderableWidget);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void madnesscore$onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        SlotHoverManager.update(mouseX, mouseY);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void madnesscore$onRenderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        SlotHoverManager.drawExtraGroups(graphics);
    }

    @Inject(method = "renderLabels", at = @At("TAIL"))
    private void madnesscore$onRenderLabels(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        SlotHoverManager.drawActiveGroup(graphics);
    }

    @Override
    public PlayerSlotMenu madnesscore$getMenu() {
        return (PlayerSlotMenu) ((AbstractContainerScreenAccessor) this).madnesscore$getMenu();
    }

    @Override
    public Rect2i madnesscore$getGroupRect(SlotGroup group) {
        Point pos = this.madnesscore$getMenu().madnesscore$getGroupPos(group);
        if (pos != null) {
            return new Rect2i(pos.x() - 1, pos.y() - 1, 17, 17);
        }
        return new Rect2i(0, 0, 0, 0);
    }

    @Override
    public Slot madnesscore$getHoveredSlot() {
        return ((AbstractContainerScreenAccessor) this).madnesscore$accessorHoveredSlot();
    }

    @Override
    public int madnesscore$getX() {
        return ((AbstractContainerScreenAccessor) this).madnesscore$getLeftPos();
    }

    @Override
    public int madnesscore$getY() {
        return ((AbstractContainerScreenAccessor) this).madnesscore$getTopPos();
    }

    @Override
    public boolean madnesscore$isRecipeBookOpen() {
        return ((RecipeUpdateListener) this).getRecipeBookComponent().isVisible();
    }
}
