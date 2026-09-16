package dev.lukamadness.madnesscore.common.client.gui.widgets;

import dev.lukamadness.madnesscore.common.client.config.ConfigManager;
import dev.lukamadness.madnesscore.common.client.config.search.SearchQuerySession;
import dev.lukamadness.madnesscore.common.client.config.structure.Option;
import dev.lukamadness.madnesscore.common.client.gui.ButtonTheme;
import dev.lukamadness.madnesscore.common.client.gui.Colors;
import dev.lukamadness.madnesscore.common.client.gui.Layout;
import dev.lukamadness.madnesscore.common.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class SearchWidget extends AbstractParentWidget {
    private static final int MAX_ORDER_DIST_ERROR = 2;

    private static final ButtonTheme CLEAR_BUTTON_THEME = new ButtonTheme(
            Colors.FOREGROUND, Colors.FOREGROUND, Colors.FOREGROUND_DISABLED,
            Colors.BACKGROUND_MEDIUM, Colors.BACKGROUND_LIGHT, Colors.BACKGROUND_LIGHT);

    private final Consumer<List<Option.OptionNameSource>> onSearchResults;
    private final SearchQuerySession searchQuerySession;
    private String query = "";

    private EditBox searchBox;
    private FlatButtonWidget clearButton;
    private int lastRebuildWidth = -1;

    public SearchWidget(Consumer<List<Option.OptionNameSource>> onSearchResults, Dim2i dim) {
        super(dim);
        this.onSearchResults = onSearchResults;
        this.searchQuerySession = ConfigManager.CONFIG.startSearchQuery();
    }

    public void updateWidgetWidth(int width) {
        if (width != this.lastRebuildWidth) {
            this.lastRebuildWidth = width;
            this.rebuildForWidth(width);
        }
    }

    private void rebuildForWidth(int width) {
        this.clearChildren();

        int x = this.getX();
        int y = this.getY();

        int searchBoxWidth = width - Layout.BUTTON_SHORT;
        this.clearButton = new FlatButtonWidget(
                new Dim2i(x + searchBoxWidth, y, Layout.BUTTON_SHORT, Layout.BUTTON_SHORT),
                Component.literal("×"),
                this::clearSearch,
                true,
                false,
                CLEAR_BUTTON_THEME
        );

        this.searchBox = new EditBox(
                this.font,
                x + Layout.INNER_MARGIN,
                y + Layout.BUTTON_SHORT / 2 - this.font.lineHeight / 2,
                searchBoxWidth - Layout.BUTTON_SHORT,
                Layout.BUTTON_SHORT,
                Component.translatable("madnesscore.config.options.search")
        );

        this.searchBox.setMaxLength(200);
        this.searchBox.setBordered(false);
        this.searchBox.setResponder(this::triggerSearch);
        this.searchBox.setHint(
                Component.translatable("madnesscore.config.options.search.hint")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));

        this.addChild(this.searchBox);
        this.addChild(this.clearButton);

        this.updateClearButtonVisibility();
    }

    private void updateClearButtonVisibility() {
        this.clearButton.setVisible(!this.query.isEmpty());
    }

    private void clearSearch() {
        this.searchBox.setValue("");
        this.query = "";
        this.updateClearButtonVisibility();
        this.search();
        this.setFocused(null);
    }

    private void triggerSearch(String text) {
        if (text.equals(this.query)) {
            return;
        }

        this.query = text.stripLeading();
        this.updateClearButtonVisibility();
        this.search();
    }

    @SuppressWarnings("unchecked")
    private void search() {
        var results = this.searchQuerySession.getSearchResults(this.query);

        for (int i = 0; i < results.size(); i++) {
            var result = results.get(i);
            result.setResultIndex(i);

            if (!(result instanceof Option.OptionNameSource)) {
                throw new UnsupportedOperationException("Unsupported search text source type: " + result.getClass().getName());
            }
        }

        List<Option.OptionNameSource> typedResults = (List<Option.OptionNameSource>) results;

        this.improveGrouping(typedResults);
        this.onSearchResults.accept(typedResults);
    }

    private void improveGrouping(List<Option.OptionNameSource> searchResults) {
        var length = searchResults.size();
        for (int i = 1; i < length - 1; i++) {
            var prev = searchResults.get(i - 1);
            var curr = searchResults.get(i);
            var next = searchResults.get(i + 1);

            if (Math.abs(i - prev.getResultIndex()) > MAX_ORDER_DIST_ERROR ||
                    Math.abs(i + 1 - next.getResultIndex()) > MAX_ORDER_DIST_ERROR) {
                continue;
            }

            var prevCurrScore = this.getGroupScore(prev, curr);
            var prevNextScore = this.getGroupScore(prev, next);

            if (prevNextScore > prevCurrScore) {
                searchResults.set(i, next);
                searchResults.set(i + 1, curr);
            }
        }
    }

    private int getGroupScore(Option.OptionNameSource a, Option.OptionNameSource b) {
        if (a.getModOptions() != b.getModOptions()) {
            return 0;
        }
        if (a.getPage() != b.getPage()) {
            return 1;
        }
        if (a.getOptionGroup() != b.getOptionGroup()) {
            return 2;
        }
        return 3;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(this.getX(), this.getY(), this.getX() + this.lastRebuildWidth, this.getLimitY(), Colors.BACKGROUND_DEFAULT);

        this.searchBox.render(graphics, mouseX, mouseY, delta);
        this.clearButton.render(graphics, mouseX, mouseY, delta);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && this.getFocused() == this.searchBox) {
            this.clearSearch();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.searchBox.charTyped(codePoint, modifiers);
    }

    public boolean isSearching() {
        return this.searchBox.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);

        if (focused) {
            this.setFocused(this.searchBox);
        }
    }
}
