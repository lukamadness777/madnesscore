package dev.lukamadness.madnesscore.common.client.bundledtabs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class BundledTabGroup {
    private final Supplier<CreativeModeTab> tab;
    private final Supplier<ResourceLocation> texture;
    private final List<BundledTab> tabs = new ArrayList<>();

    BundledTabGroup(Supplier<CreativeModeTab> tab, Supplier<ResourceLocation> texture) {
        this.tab = Objects.requireNonNull(tab, "tab");
        this.texture = Objects.requireNonNull(texture, "texture");
    }

    public CreativeModeTab getTab() {
        return this.tab.get();
    }

    public ResourceLocation getTexture() {
        return this.texture.get();
    }

    public BundledTabGroup addTab(BundledTab bundledTab) {
        this.tabs.add(Objects.requireNonNull(bundledTab, "bundledTab"));
        return this;
    }

    public List<BundledTab> getTabs() {
        return Collections.unmodifiableList(this.tabs);
    }
}
