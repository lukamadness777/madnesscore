package dev.lukamadness.madnesscore.common.client.bundledtabs;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class BundledTab {
    private final Component tooltip;
    private final ItemStack icon;
    private final List<ItemStack> displayItems;
    @Nullable
    private final BiConsumer<HolderLookup.Provider, Output> populationLogic;
    private boolean populated;

    @Nullable
    private Runnable onSelectionChanged;
    private boolean selected;

    private BundledTab(Component tooltip, ItemStack icon, @Nullable BiConsumer<HolderLookup.Provider, Output> populationLogic) {
        this.tooltip = tooltip;
        this.icon = icon;
        this.displayItems = new ArrayList<>();
        this.populationLogic = populationLogic;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Component getTooltip() {
        return this.tooltip;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public List<ItemStack> getDisplayItems() {
        return Collections.unmodifiableList(this.displayItems);
    }

    public void select() {
        this.selected = true;
        this.fireSelectionChanged();
    }

    public void deselect() {
        this.selected = false;
        this.fireSelectionChanged();
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setOnSelectionChanged(@Nullable Runnable callback) {
        this.onSelectionChanged = callback;
    }

    private void fireSelectionChanged() {
        if (this.onSelectionChanged != null) {
            this.onSelectionChanged.run();
        }
    }

    public void populate(HolderLookup.Provider provider) {
        if (this.populated || this.populationLogic == null) {
            return;
        }
        this.populationLogic.accept(provider, new Output() {
            @Override
            public void accept(ItemLike item) {
                BundledTab.this.displayItems.add(new ItemStack(item));
            }

            @Override
            public void accept(ItemStack stack) {
                BundledTab.this.displayItems.add(stack);
            }
        });
        this.populated = true;
    }

    public static class Builder {
        private Component title;
        private ItemStack icon;
        private BiConsumer<HolderLookup.Provider, Output> populationLogic;

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder icon(ItemStack icon) {
            this.icon = icon;
            return this;
        }

        public Builder icon(Supplier<ItemStack> icon) {
            this.icon = icon.get();
            return this;
        }

        public Builder displayItems(BiConsumer<HolderLookup.Provider, Output> logic) {
            this.populationLogic = logic;
            return this;
        }

        public BundledTab build() {
            if (this.title == null) {
                this.title = Component.empty();
            }
            if (this.icon == null) {
                this.icon = ItemStack.EMPTY;
            }
            return new BundledTab(this.title, this.icon, this.populationLogic);
        }
    }

    public interface Output {
        void accept(ItemLike item);

        void accept(ItemStack stack);
    }
}
