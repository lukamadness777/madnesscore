package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTabSelector;
import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTabsAPI;
import dev.lukamadness.madnesscore.common.client.compat.vanillabackport.VanillaBackportBundledTabCompat;
import dev.lukamadness.madnesscore.common.client.mixin.accessor.AbstractContainerScreenAccessor;
import dev.lukamadness.madnesscore.common.client.mixin.accessor.CreativeModeInventoryScreenAccessor;
import dev.lukamadness.madnesscore.common.client.mixin.accessor.SlotAccessor;
import dev.lukamadness.madnesscore.common.client.mixin.accessor.SlotWrapperAccessor;
import dev.lukamadness.madnesscore.common.client.tabbutton.DynamicTabButtonManager;
import dev.lukamadness.madnesscore.common.client.tabbutton.DynamicTabButtonWidget;
import dev.lukamadness.madnesscore.common.client.slots.ui.SlotHoverManager;
import dev.lukamadness.madnesscore.common.client.slots.ui.SlotHoverScreen;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerDynamicSlot;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import dev.lukamadness.madnesscore.common.slots.gui.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen extends Screen implements SlotHoverScreen {
    @Unique
    private BundledTabSelector madnesscore$selector;

    /**
     * Widgets creados por {@link DynamicTabButtonManager#installButtons}, guardados para poder
     * correrlos horizontalmente en {@link #madnesscore$repositionDynamicTabButtons} cuando
     * cambia si la pestaña seleccionada tiene un bundled tab activo (ver esa franja en {@code
     * BundledTabSelector#renderAndSync}) — {@code selectTab} NO reinicializa la screen, así que
     * sin esto los botones quedarían en la posición calculada en {@code init} para siempre.
     */
    @Unique
    private List<DynamicTabButtonWidget> madnesscore$dynamicTabButtons = List.of();

    protected MixinCreativeModeInventoryScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void madnesscore$onInit(CallbackInfo ci) {
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        this.madnesscore$selector = new BundledTabSelector(self);
        this.madnesscore$selector.injectWidgets(this::addRenderableWidget, this::removeWidget);
        SlotHoverManager.init(this);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void madnesscore$addDynamicTabButtons(CallbackInfo ci) {
        CreativeModeTab currentTab = CreativeModeInventoryScreenAccessor.madnesscore$getSelectedTab();
        boolean bundledTabActive = currentTab != null
                && (BundledTabsAPI.getGroup(currentTab) != null
                || VanillaBackportBundledTabCompat.hasBundledTab(currentTab));

        this.madnesscore$dynamicTabButtons = DynamicTabButtonManager.installButtons(
                this.madnesscore$getX(), this.madnesscore$getY(), bundledTabActive, this::addRenderableWidget);
    }

    @Inject(method = "renderBg", at = @At("HEAD"))
    private void madnesscore$onUpdateHover(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        SlotHoverManager.update(mouseX, mouseY);
    }

    @Inject(method = "renderLabels", at = @At("TAIL"))
    private void madnesscore$onRenderLabels(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        SlotHoverManager.drawActiveGroup(graphics);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void madnesscore$onRenderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        CreativeModeTab currentTab = CreativeModeInventoryScreenAccessor.madnesscore$getSelectedTab();
        this.madnesscore$selector.renderAndSync(graphics, currentTab);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void madnesscore$onScroll(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab currentTab = CreativeModeInventoryScreenAccessor.madnesscore$getSelectedTab();
        if (this.madnesscore$selector != null && this.madnesscore$selector.handleScroll(currentTab, mouseX, mouseY, scrollY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void madnesscore$onRemoved(CallbackInfo ci) {
        if (this.madnesscore$selector != null) {
            this.madnesscore$selector.onClose();
        }
        SlotHoverManager.close();
        SlotHoverManager.removeSelections();
    }

    @Override
    public PlayerSlotMenu madnesscore$getMenu() {
        return (PlayerSlotMenu) Minecraft.getInstance().player.inventoryMenu;
    }

    @Override
    public Rect2i madnesscore$getGroupRect(SlotGroup group) {
        CreativeModeTab currentTab = CreativeModeInventoryScreenAccessor.madnesscore$getSelectedTab();
        if (currentTab == null || currentTab.getType() != CreativeModeTab.Type.INVENTORY) {
            return new Rect2i(0, 0, 0, 0);
        }
        PlayerSlotMenu survivalMenu = this.madnesscore$getMenu();
        int groupNum = survivalMenu.madnesscore$getGroupNum(group);
        if (groupNum == 0) {
            return new Rect2i(0, 0, 0, 0);
        }
        Point anchor = groupNum < 0
                ? madnesscore$vanillaSlotCreativePos(-groupNum)
                : new Point(8, 6 + (groupNum - 1) * 18);
        return new Rect2i(anchor.x() - 1, anchor.y() - 1, 17, 17);
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

    @Inject(method = "selectTab", at = @At("TAIL"), require = 1)
    private void madnesscore$onSelectTab(CreativeModeTab tab, CallbackInfo ci) {
        if (this.madnesscore$selector != null) {
            this.madnesscore$selector.onVanillaTabSelected(tab);
        }
    }

    /**
     * {@code selectTab} no reinicializa la screen (a diferencia de abrirla de nuevo), así que
     * el offset calculado una sola vez en {@link #madnesscore$addDynamicTabButtons} quedaría
     * obsoleto en cuanto el jugador entra o sale de una pestaña con bundled tab. Acá se
     * recalcula y se aplica solo la X (la Y de cada botón no depende de la pestaña).
     */
    @Inject(method = "selectTab", at = @At("TAIL"), require = 1)
    private void madnesscore$repositionDynamicTabButtons(CreativeModeTab tab, CallbackInfo ci) {
        if (this.madnesscore$dynamicTabButtons.isEmpty()) {
            return;
        }
        boolean bundledTabActive = BundledTabsAPI.getGroup(tab) != null
                || VanillaBackportBundledTabCompat.hasBundledTab(tab);
        int extraOffset = bundledTabActive ? DynamicTabButtonManager.BUNDLED_TAB_EXTRA_OFFSET : 0;
        int x = this.madnesscore$getX() - 30 - extraOffset;

        for (DynamicTabButtonWidget button : this.madnesscore$dynamicTabButtons) {
            button.setX(x);
        }
    }

    @Inject(method = "selectTab", at = @At("TAIL"), require = 1)
    private void madnesscore$repositionDynamicSlots(CreativeModeTab tab, CallbackInfo ci) {
        if (tab.getType() != CreativeModeTab.Type.INVENTORY) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.inventoryMenu instanceof PlayerSlotMenu survivalMenu)) {
            return;
        }
        CreativeModeInventoryScreen.ItemPickerMenu menu =
                ((CreativeModeInventoryScreen) (Object) this).getMenu();

        for (Slot wrapped : menu.slots) {
            if (!(wrapped instanceof SlotWrapperAccessor sw)) {
                continue;
            }
            Slot original = sw.madnesscore$getTarget();
            if (!(original instanceof PlayerDynamicSlot ds)) {
                continue;
            }

            int groupNum = survivalMenu.madnesscore$getGroupNum(ds.madnesscore$getGroup());
            Point survivalAnchor = survivalMenu.madnesscore$getGroupPos(ds.madnesscore$getGroup());
            if (groupNum == 0 || survivalAnchor == null) {
                continue;
            }

            Point creativeAnchor = groupNum < 0
                    ? madnesscore$vanillaSlotCreativePos(-groupNum)
                    : new Point(8, 6 + (groupNum - 1) * 18);

            int newX = creativeAnchor.x() + (original.x - survivalAnchor.x());
            int newY = creativeAnchor.y() + (original.y - survivalAnchor.y());
            ((SlotAccessor) wrapped).madnesscore$setX(newX);
            ((SlotAccessor) wrapped).madnesscore$setY(newY);
        }
    }

    @Unique
    private static Point madnesscore$vanillaSlotCreativePos(int k) {
        int x;
        int y;
        if (k >= 5 && k < 9) {
            int k1 = k - 5;
            x = 54 + (k1 / 2) * 54;
            y = 6 + (k1 % 2) * 27;
        } else if (k >= 0 && k < 5) {
            x = -2000;
            y = -2000;
        } else if (k == 45) {
            x = 35;
            y = 20;
        } else {
            int j1 = k - 9;
            int l1 = j1 % 9;
            int j2 = j1 / 9;
            x = 9 + l1 * 18;
            y = k >= 36 ? 112 : 54 + j2 * 18;
        }
        return new Point(x, y);
    }
}