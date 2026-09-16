package dev.lukamadness.madnesscore.common.client.tabbutton;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SimpleDynamicTabButtonSource implements DynamicTabButtonSource {

    private final String sourceId;
    private final Function<LocalPlayer, Map<String, ItemStack>> activeInstancesFn;
    private final Supplier<Component> labelFn;
    private final Supplier<ItemStack> iconFn;
    private final Consumer<String> onClickFn;

    private SimpleDynamicTabButtonSource(Builder builder) {
        this.sourceId = builder.sourceId;
        this.activeInstancesFn = builder.activeInstancesFn;
        this.labelFn = builder.labelFn;
        this.iconFn = builder.iconFn;
        this.onClickFn = builder.onClickFn;
    }

    public static Builder builder(String sourceId) {
        return new Builder(sourceId);
    }

    @Override
    public String sourceId() {
        return this.sourceId;
    }

    @Override
    public Map<String, ItemStack> getActiveInstances(LocalPlayer player) {
        return this.activeInstancesFn.apply(player);
    }

    @Override
    public Component label() {
        return this.labelFn.get();
    }

    @Override
    public ItemStack buttonIcon() {
        return this.iconFn.get();
    }

    @Override
    public void onClick(String instanceKey) {
        this.onClickFn.accept(instanceKey);
    }

    public static final class Builder {
        private final String sourceId;
        private Function<LocalPlayer, Map<String, ItemStack>> activeInstancesFn;
        private Supplier<Component> labelFn;
        private Supplier<ItemStack> iconFn;
        private Consumer<String> onClickFn;

        private Builder(String sourceId) {
            this.sourceId = Objects.requireNonNull(sourceId, "sourceId");
        }

        public Builder activeInstances(Function<LocalPlayer, Map<String, ItemStack>> fn) {
            this.activeInstancesFn = Objects.requireNonNull(fn, "activeInstances");
            return this;
        }

        public Builder label(Component label) {
            Objects.requireNonNull(label, "label");
            this.labelFn = () -> label;
            return this;
        }

        public Builder label(Supplier<Component> labelFn) {
            this.labelFn = Objects.requireNonNull(labelFn, "labelFn");
            return this;
        }

        public Builder icon(ItemStack icon) {
            Objects.requireNonNull(icon, "icon");
            this.iconFn = icon::copy;
            return this;
        }

        public Builder icon(Supplier<ItemStack> iconFn) {
            this.iconFn = Objects.requireNonNull(iconFn, "iconFn");
            return this;
        }

        public Builder onClick(Consumer<String> onClickFn) {
            this.onClickFn = Objects.requireNonNull(onClickFn, "onClick");
            return this;
        }

        public SimpleDynamicTabButtonSource build() {
            Objects.requireNonNull(this.activeInstancesFn, "activeInstances(...) es obligatorio");
            Objects.requireNonNull(this.labelFn, "label(...) es obligatorio");
            Objects.requireNonNull(this.iconFn, "icon(...) es obligatorio");
            Objects.requireNonNull(this.onClickFn, "onClick(...) es obligatorio");
            return new SimpleDynamicTabButtonSource(this);
        }
    }
}
