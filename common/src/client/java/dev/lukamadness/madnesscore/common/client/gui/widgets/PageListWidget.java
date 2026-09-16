package dev.lukamadness.madnesscore.common.client.gui.widgets;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import dev.lukamadness.madnesscore.common.client.config.ConfigManager;
import dev.lukamadness.madnesscore.common.client.config.MadnessConfigScreen;
import dev.lukamadness.madnesscore.common.client.config.structure.ExternalPage;
import dev.lukamadness.madnesscore.common.client.config.structure.ModOptions;
import dev.lukamadness.madnesscore.common.client.config.structure.OptionPage;
import dev.lukamadness.madnesscore.common.client.config.structure.Page;
import dev.lukamadness.madnesscore.common.client.gui.ColorTheme;
import dev.lukamadness.madnesscore.common.client.gui.Colors;
import dev.lukamadness.madnesscore.common.client.gui.Layout;
import dev.lukamadness.madnesscore.common.client.gui.options.control.AbstractScrollable;
import dev.lukamadness.madnesscore.common.client.gui.options.control.ExternalButtonControl;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PageListWidget extends AbstractScrollable {
    private final MadnessConfigScreen parent;
    private EntryWidget selected;
    private final Reference2ReferenceMap<Page, PageEntryWidget<?>> pageToWidget = new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceMap<Page, ModOptions> pageToMod = new Reference2ReferenceOpenHashMap<>();
    private final ReferenceSet<ModOptions> expandedMods = new ReferenceOpenHashSet<>();
    private boolean defaultsInitialized = false;

    public PageListWidget(Dim2i position, MadnessConfigScreen parent) {
        super(position);
        this.parent = parent;
        this.rebuild();
    }

    private void rebuild() {
        int x = this.getX();
        int y = this.getY();
        int width = this.getWidth();
        int height = this.getHeight();

        this.clearChildren();
        this.pageToWidget.clear();
        this.pageToMod.clear();
        this.scrollbar = this.addRenderableChild(new ScrollbarWidget(new Dim2i(this.getLimitX() - Layout.SCROLLBAR_WIDTH, y, Layout.SCROLLBAR_WIDTH, height), false, false));

        int entryHeight = Layout.pageEntryHeight(this.font);
        var headerHeight = Layout.pageHeaderHeight(this.font);
        int listHeight = 0;
        boolean isFirstMod = true;
        for (var modOptions : ConfigManager.CONFIG.getModOptions()) {
            if (modOptions.pages().isEmpty()) {
                continue;
            }

            for (Page page : modOptions.pages()) {
                this.pageToMod.put(page, modOptions);
            }

            if (!this.defaultsInitialized && isFirstMod) {
                this.expandedMods.add(modOptions);
            }

            var theme = modOptions.theme();
            boolean expanded = this.expandedMods.contains(modOptions);

            if (!isFirstMod) {
                listHeight += Layout.TEXT_LINE_SPACING;
            }
            isFirstMod = false;

            var headerDim = new Dim2i(x, y + listHeight, width, headerHeight);
            var modHeaderStart = headerDim.y();
            CenteredFlatWidget header = new HeaderEntryWidget(headerDim, modOptions, theme, expanded);
            listHeight += headerHeight;

            this.addRenderableChild(header);

            if (!expanded) {
                continue;
            }

            for (Page page : modOptions.pages()) {
                PageEntryWidget<?> pageWidget;
                Dim2i widgetDim = new Dim2i(x + Layout.PAGE_ENTRY_INDENT, y + listHeight, width - Layout.PAGE_ENTRY_INDENT, entryHeight);

                var scrollTargetStart = widgetDim.y();
                if (modHeaderStart != -1) {
                    scrollTargetStart = modHeaderStart;
                    modHeaderStart = -1;
                }

                if (page instanceof OptionPage optionPage) {
                    pageWidget = new OptionPageEntryWidget(widgetDim, optionPage, theme, scrollTargetStart);
                } else if (page instanceof ExternalPage externalPage) {
                    pageWidget = new ExternalPageEntryWidget(widgetDim, externalPage, theme, scrollTargetStart);
                } else {
                    throw new IllegalStateException("Unknown page type: " + page.getClass());
                }

                this.pageToWidget.put(page, pageWidget);
                listHeight += entryHeight;

                this.addRenderableChild(pageWidget);
            }
        }

        this.defaultsInitialized = true;
        this.scrollbar.setScrollbarContext(listHeight + Layout.INNER_MARGIN);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackgroundGradient(graphics, this.getX(), this.getY(), this.getLimitX(), this.getLimitY());
        graphics.enableScissor(this.getX(), this.getY(), this.getLimitX(), this.getLimitY());
        super.render(graphics, mouseX, mouseY, delta);
        graphics.disableScissor();
    }

    public static void renderBackgroundGradient(GuiGraphics graphics, int x1, int y1, int x2, int y2) {
        graphics.fillGradient(x1, y1, x2, y2, Colors.BACKGROUND_LIGHT, Colors.BACKGROUND_DEFAULT);
    }

    private void switchSelectedWidget(EntryWidget widget) {
        if (widget == null) {
            return;
        }

        if (widget != this.selected) {
            if (this.selected != null) {
                this.selected.setSelected(false);
            }
            this.selected = widget;
            this.selected.setSelected(true);
        }

        int widgetTop = this.selected.getScrollTargetStart();
        int widgetBottom = widgetTop + this.selected.getHeight();
        int viewTop = this.getY() + this.scrollbar.getScrollAmount();
        int viewBottom = viewTop + this.getHeight();
        if (widgetTop < viewTop) {
            this.scrollbar.scrollTo(widgetTop - this.getY());
        } else if (widgetBottom > viewBottom) {
            this.scrollbar.scrollTo(widgetBottom - this.getY() - this.getHeight());
        }
    }

    private void toggleMod(ModOptions modOptions) {
        if (this.expandedMods.contains(modOptions)) {
            this.expandedMods.remove(modOptions);
            this.rebuild();
            return;
        }

        this.expandedMods.add(modOptions);
        this.rebuild();

        var pages = modOptions.pages();
        var firstPage = pages.isEmpty() ? null : pages.getFirst();
        if (firstPage != null) {
            this.switchSelectedWidget(this.pageToWidget.get(firstPage));
        }
        this.parent.showAllPages(firstPage);
    }

    public void switchSelected(Page page) {
        var owningMod = this.pageToMod.get(page);
        if (owningMod != null && this.expandedMods.add(owningMod)) {
            this.rebuild();
        }

        this.switchSelectedWidget(this.pageToWidget.get(page));
    }

    private abstract class EntryWidget extends CenteredFlatWidget {
        EntryWidget(Dim2i dim, Component label, boolean isSelectable, ColorTheme theme) {
            super(dim, label, isSelectable, theme);
        }

        EntryWidget(Dim2i dim, Component label, Component subtitle, boolean isSelectable, ColorTheme theme) {
            super(dim, label, subtitle, isSelectable, theme);
        }

        public int getScrollTargetStart() {
            return super.getY();
        }

        @Override
        public int getY() {
            return super.getY() - PageListWidget.this.scrollbar.getScrollAmount();
        }
    }

    private class HeaderEntryWidget extends EntryWidget {
        private final ModOptions modOptions;
        private final ResourceLocation icon;
        private final boolean iconMonochrome;

        HeaderEntryWidget(Dim2i dim, ModOptions modOptions, ColorTheme theme, boolean expanded) {
            super(dim, Component.literal(modOptions.name()), Component.literal(modOptions.version()), true, theme);
            this.modOptions = modOptions;
            this.icon = modOptions.icon();
            this.iconMonochrome = modOptions.iconMonochrome();
        }

        @Override
        protected int renderIcon(GuiGraphics graphics, int textColor) {
            if (this.icon == null) {
                return super.renderIcon(graphics, textColor);
            }

            return MadnessConfigScreen.renderIconWithSpacing(graphics, this.icon, textColor, this.iconMonochrome,
                    this.getX(), this.getY(), this.getHeight(), Layout.ICON_MARGIN);
        }

        @Override
        void onAction() {
            PageListWidget.this.toggleMod(this.modOptions);
        }
    }

    private abstract class PageEntryWidget<P extends Page> extends EntryWidget {
        final P page;
        final int scrollTargetStart;

        PageEntryWidget(Dim2i dim, P page, Component label, ColorTheme theme, int scrollTargetStart) {
            super(dim, label, true, theme);
            this.page = page;
            this.scrollTargetStart = scrollTargetStart;
        }
    }

    private class OptionPageEntryWidget extends PageEntryWidget<Page> {
        OptionPageEntryWidget(Dim2i dim, Page page, ColorTheme theme, int scrollTargetStart) {
            super(dim, page, page.name(), theme, scrollTargetStart);
        }

        @Override
        public int getScrollTargetStart() {
            return this.scrollTargetStart;
        }

        @Override
        void onAction() {
            PageListWidget.this.switchSelectedWidget(this);

            var owningMod = PageListWidget.this.pageToMod.get(this.page);
            if (owningMod != null) {
                PageListWidget.this.parent.filterToPage(owningMod, this.page);
            }
        }
    }

    private class ExternalPageEntryWidget extends PageEntryWidget<ExternalPage> {
        ExternalPageEntryWidget(Dim2i dim, ExternalPage page, ColorTheme theme, int scrollTargetStart) {
            super(dim, page, Component.literal(ExternalButtonControl.EXTERNAL_PAGE_PREFIX).append(page.name()), theme, scrollTargetStart);
        }

        @Override
        void onAction() {
            this.page.currentScreenConsumer().accept(PageListWidget.this.parent);
        }
    }
}
