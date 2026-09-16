package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.mixin.accessor.SlotWrapperAccessor;
import dev.lukamadness.madnesscore.common.client.slots.ui.SlotUiState;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerDynamicSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected AbstractContainerMenu menu;

    @Shadow
    private boolean isHovering(Slot pSlot, double pMouseX, double pMouseY) {
        throw new AssertionError();
    }

    @Unique
    private static final ResourceLocation MORE_SLOTS =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/slots/more_slots.png");

    @Unique
    private static final ResourceLocation BLANK_BACK =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/slots/blank_back.png");

    protected MixinAbstractContainerScreen(net.minecraft.network.chat.Component title) {
        super(title);
    }

    @Unique
    private static PlayerDynamicSlot madnesscore$unwrap(Slot slot) {
        if (slot instanceof PlayerDynamicSlot ds) {
            return ds;
        }
        if (slot instanceof SlotWrapperAccessor wrapper && wrapper.madnesscore$getTarget() instanceof PlayerDynamicSlot ds) {
            return ds;
        }
        return null;
    }

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void madnesscore$onRenderSlotHead(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        PlayerDynamicSlot ds = madnesscore$unwrap(slot);
        if (ds == null) {
            return;
        }
        boolean groupOpen = SlotUiState.activeGroup == ds.madnesscore$getGroup();
        if (!ds.madnesscore$isAnchor() && !groupOpen) {
            ci.cancel();
            return;
        }
        if (!ds.madnesscore$isAnchor() && SlotUiState.activeType != null
                && SlotUiState.activeType != ds.madnesscore$getType()) {
            ci.cancel();
            return;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);

        ResourceLocation background = slot.hasItem() ? BLANK_BACK : ds.madnesscore$getBackground();
        if (background == null) {
            background = BLANK_BACK;
        }
        graphics.blit(background, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
        if (!ds.madnesscore$isAnchor()) {
            graphics.blit(MORE_SLOTS, slot.x - 1, slot.y - 1, 4, 4, 18, 18, 256, 256);
        }
        if (this.hoveredSlot == slot && groupOpen) {
            graphics.fill(slot.x - 1, slot.y - 1, slot.x + 17, slot.y + 17, 0x80FFFFFF);
        }
    }

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void madnesscore$onRenderSlotTail(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (madnesscore$unwrap(slot) != null) {
            graphics.pose().popPose();
        }
    }

    @Unique
    private boolean madnesscore$anyDynamicSlotActive() {
        for (Slot slot : this.menu.slots) {
            if (slot.isActive() && madnesscore$unwrap(slot) != null) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At("HEAD"))
    private void madnesscore$onRenderTooltipHead(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (madnesscore$anyDynamicSlotActive()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);
        }
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At("TAIL"))
    private void madnesscore$onRenderTooltipTail(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (madnesscore$anyDynamicSlotActive()) {
            graphics.pose().popPose();
        }
    }

    @Inject(method = "renderFloatingItem", at = @At("HEAD"))
    private void madnesscore$onRenderFloatingItemHead(GuiGraphics graphics, ItemStack stack, int x, int y,
            @Nullable String countString, CallbackInfo ci) {
        if (madnesscore$anyDynamicSlotActive()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);
        }
    }

    @Inject(method = "renderFloatingItem", at = @At("TAIL"))
    private void madnesscore$onRenderFloatingItemTail(GuiGraphics graphics, ItemStack stack, int x, int y,
            @Nullable String countString, CallbackInfo ci) {
        if (madnesscore$anyDynamicSlotActive()) {
            graphics.pose().popPose();
        }
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void madnesscore$hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        for (Slot slot : this.menu.slots) {
            if (slot.isActive() && madnesscore$unwrap(slot) != null && this.isHovering(slot, mouseX, mouseY)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void madnesscore$isHovering(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (SlotUiState.activeGroup == null) {
            return;
        }
        PlayerDynamicSlot ds = madnesscore$unwrap(slot);
        if (ds != null) {
            boolean visible = ds.madnesscore$isAnchor() || SlotUiState.activeGroup == ds.madnesscore$getGroup();
            if (!visible) {
                cir.setReturnValue(false);
            }
        }
    }
}