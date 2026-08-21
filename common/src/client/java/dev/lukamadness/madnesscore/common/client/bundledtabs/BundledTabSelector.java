package dev.lukamadness.madnesscore.common.client.bundledtabs;

import dev.lukamadness.madnesscore.common.client.mixin.AbstractContainerScreenAccessor;
import dev.lukamadness.madnesscore.common.client.mixin.CreativeModeInventoryScreenAccessor;
import dev.lukamadness.madnesscore.common.client.mixin.ItemPickerMenuAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

/**
 * Selector lateral de bundle tabs. Vive en la screen de inventario creativo
 * (uno por instancia de {@link CreativeModeInventoryScreen}, ver
 * {@code MixinCreativeModeInventoryScreen}) y soporta CUALQUIER cantidad de
 * {@link BundledTabGroup} registrados en {@link BundledTabsAPI}: cada vez
 * que el jugador cambia de CreativeModeTab, reconstruye la barra lateral
 * para el grupo correspondiente a esa pestaña (o la esconde si no tiene
 * ninguno registrado).
 */
public class BundledTabSelector {
    private static final int VISIBLE_CATEGORIES = 5;

    private final CreativeModeInventoryScreen screen;
    private Consumer<AbstractWidget> addWidget;
    private Consumer<AbstractWidget> removeWidget;

    private BundledTabGroup currentGroup;
    private final List<Tab> tabWidgets = new ArrayList<>();
    private AbstractWidget scrollUpButton;
    private AbstractWidget scrollDownButton;
    private int scroll;
    private static CreativeModeTab lastTab;

    public BundledTabSelector(CreativeModeInventoryScreen screen) {
        this.screen = screen;
    }

    private int getGuiLeft() {
        return ((AbstractContainerScreenAccessor) this.screen).madnesscore$getLeftPos();
    }

    private int getGuiTop() {
        return ((AbstractContainerScreenAccessor) this.screen).madnesscore$getTopPos();
    }

    public void injectWidgets(Consumer<AbstractWidget> add, Consumer<AbstractWidget> remove) {
        this.addWidget = add;
        this.removeWidget = remove;
        this.onSwitchCreativeTab(getSelectedTab());
    }

    public void renderAndSync(GuiGraphics graphics, CreativeModeTab currentTab) {
        if (this.currentGroup != null && this.currentGroup.getTab() == currentTab) {
            graphics.blit(this.currentGroup.getTexture(), this.getGuiLeft() - 30, this.getGuiTop() + 2, 0, 0, 30, 120);
        }
        if (lastTab != currentTab) {
            this.onSwitchCreativeTab(currentTab);
            lastTab = currentTab;
        }
    }

    public boolean handleScroll(CreativeModeTab currentTab, double mouseX, double mouseY, double scrollY) {
        if (this.currentGroup == null || this.currentGroup.getTab() != currentTab) {
            return false;
        }
        int guiLeft = this.getGuiLeft();
        int guiTop = this.getGuiTop();
        boolean withinBar = mouseX >= guiLeft - 30 && mouseX <= guiLeft
                && mouseY >= guiTop + 2 && mouseY <= guiTop + 122;
        if (withinBar) {
            int delta = (int) Math.signum(scrollY);
            if (delta != 0) {
                this.scroll = Mth.clamp(this.scroll - delta, 0, this.getMaxScroll());
                this.updateWidgets();
            }
            return true;
        }
        return false;
    }

    public void onClose() {
        this.scrollUpButton = null;
        this.scrollDownButton = null;
        this.tabWidgets.clear();
        if (this.currentGroup != null) {
            this.currentGroup.getTabs().forEach(bundle -> {
                bundle.setOnSelectionChanged(null);
                bundle.deselect();
            });
        }
        this.currentGroup = null;
    }

    /**
     * Llamado desde el mixin justo después de que vanilla ejecuta
     * selectTab(...). Clickear cualquier pestaña de arriba (aunque sea la
     * misma ya activa) siempre deselecciona cualquier bundle activa y
     * vuelve a la vista resuelta (todos los items, deduplicados), porque
     * vanilla repuebla menu.items ahí adentro pisando lo que nosotros
     * pusimos con updateItems().
     */
    public void onVanillaTabSelected(CreativeModeTab tab) {
        if (this.currentGroup != null && this.currentGroup.getTab() == tab) {
            this.currentGroup.getTabs().forEach(BundledTab::deselect);
            this.updateItems();
        }
    }

    private void onSwitchCreativeTab(CreativeModeTab tab) {
        BundledTabGroup group = BundledTabsAPI.getGroup(tab);
        if (group != this.currentGroup) {
            this.rebuildForGroup(group);
        }
        if (group != null) {
            this.updateWidgets();
            this.updateItems();
        }
    }

    private void rebuildForGroup(BundledTabGroup group) {
        this.tabWidgets.forEach(this.removeWidget);
        this.tabWidgets.clear();
        if (this.scrollUpButton != null) {
            this.removeWidget.accept(this.scrollUpButton);
            this.scrollUpButton = null;
        }
        if (this.scrollDownButton != null) {
            this.removeWidget.accept(this.scrollDownButton);
            this.scrollDownButton = null;
        }
        if (this.currentGroup != null) {
            this.currentGroup.getTabs().forEach(bundle -> {
                bundle.setOnSelectionChanged(null);
                bundle.deselect();
            });
        }

        this.currentGroup = group;
        this.scroll = 0;
        if (group == null) {
            return;
        }

        HolderLookup.Provider registries = Minecraft.getInstance().level.registryAccess();
        group.getTabs().forEach(bundle -> bundle.populate(registries));

        int guiLeft = this.getGuiLeft();
        int guiTop = this.getGuiTop();

        for (BundledTab bundle : group.getTabs()) {
            Tab tab = new Tab(guiLeft - 23, guiTop + 7, bundle, group, button -> {
                if (bundle.isSelected()) {
                    bundle.deselect();
                } else {
                    group.getTabs().forEach(BundledTab::deselect);
                    bundle.select();
                }
                this.updateItems();
            });
            tab.visible = false;
            this.tabWidgets.add(tab);
            this.addWidget.accept(tab);
        }

        this.scrollUpButton = new ScrollButton(guiLeft - 24, guiTop + 6, 32, group, button -> {
            if (this.scroll > 0) {
                this.scroll--;
                this.updateWidgets();
            }
        });
        this.scrollDownButton = new ScrollButton(guiLeft - 24, guiTop + 108, 52, group, button -> {
            if (this.scroll < this.getMaxScroll()) {
                this.scroll++;
                this.updateWidgets();
            }
        });
        this.addWidget.accept(this.scrollUpButton);
        this.addWidget.accept(this.scrollDownButton);
    }

    private void updateWidgets() {
        this.tabWidgets.forEach(w -> w.visible = false);
        int guiTop = this.getGuiTop();
        for (int i = this.scroll; i < this.scroll + VISIBLE_CATEGORIES && i < this.tabWidgets.size(); i++) {
            Tab tab = this.tabWidgets.get(i);
            tab.setY(guiTop + 18 * (i - this.scroll) + 18);
            tab.visible = true;
        }
        boolean hasGroup = this.currentGroup != null;
        if (this.scrollUpButton != null) this.scrollUpButton.visible = hasGroup && this.scroll > 0;
        if (this.scrollDownButton != null) this.scrollDownButton.visible = hasGroup && this.scroll < this.getMaxScroll();
    }

    private void updateItems() {
        if (this.currentGroup == null) {
            return;
        }
        List<ItemStack> display = new ArrayList<>();
        List<BundledTab> bundles = this.currentGroup.getTabs();
        boolean hasSelection = bundles.stream().anyMatch(BundledTab::isSelected);

        if (hasSelection) {
            // Con una (o más) pestaña seleccionada: se muestran sus items
            // tal cual, sin resolver duplicados entre pestañas.
            for (BundledTab bundle : bundles) {
                if (!bundle.isSelected()) continue;
                for (ItemStack stack : bundle.getDisplayItems()) {
                    display.add(stack.copy());
                }
            }
        } else {
            // Sin ninguna pestaña seleccionada se muestran todos los items
            // juntos, deduplicados por tipo de item.
            HashSet<Item> seenItems = new HashSet<>();
            for (BundledTab bundle : bundles) {
                for (ItemStack stack : bundle.getDisplayItems()) {
                    if (seenItems.add(stack.getItem())) {
                        display.add(stack.copy());
                    }
                }
            }
        }

        CreativeModeInventoryScreen.ItemPickerMenu menu =
                (CreativeModeInventoryScreen.ItemPickerMenu) this.screen.getMenu();
        NonNullList<ItemStack> menuItems = ((ItemPickerMenuAccessor) menu).madnesscore$getItems();
        menuItems.clear();
        menuItems.addAll(display);
        menu.scrollTo(0.0f);
    }

    private int getMaxScroll() {
        return Math.max(0, this.tabWidgets.size() - VISIBLE_CATEGORIES);
    }

    private CreativeModeTab getSelectedTab() {
        return CreativeModeInventoryScreenAccessor.madnesscore$getSelectedTab();
    }

    public static class ScrollButton extends Button {
        private final BundledTabGroup group;
        private final int uOffset;

        private ScrollButton(int x, int y, int uOffset, BundledTabGroup group, OnPress onPress) {
            super(x, y, 18, 20, Component.empty(), onPress, DEFAULT_NARRATION);
            this.uOffset = uOffset;
            this.group = group;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int textureY = this.isHovered ? 12 : 0;
            // texture() se re-evalúa cada frame: si el mod que registró el
            // grupo devuelve una ResourceLocation dinámica (ej. según la
            // dimensión), el cambio se refleja al toque sin reabrir el menú.
            graphics.blit(this.group.getTexture(), this.getX(), this.getY(), this.uOffset, textureY, 18, 11);
        }
    }

    public static class Tab extends Button {
        private final BundledTab bundle;
        private final BundledTabGroup group;

        private Tab(int x, int y, BundledTab bundle, BundledTabGroup group, OnPress onPress) {
            super(x, y, 16, 16, Component.empty(), onPress, DEFAULT_NARRATION);
            this.bundle = bundle;
            this.group = group;
            this.setTooltip(Tooltip.create(bundle.getTooltip()));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation texture = this.group.getTexture();
            if (this.bundle.isSelected()) {
                graphics.blit(texture, this.getX() - 8, this.getY() - 3, 35, 23, 32, 22);
            }
            graphics.renderItem(this.bundle.getIcon(), this.getX(), this.getY());
            if (this.isHoveredOrFocused() && !this.bundle.isSelected()) {
                graphics.blit(texture, this.getX(), this.getY(), 7, 130, 16, 16);
            }
        }
    }
}
