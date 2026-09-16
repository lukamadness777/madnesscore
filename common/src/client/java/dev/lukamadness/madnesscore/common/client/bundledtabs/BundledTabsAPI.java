package dev.lukamadness.madnesscore.common.client.bundledtabs;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class BundledTabsAPI {
    private BundledTabsAPI() {}

    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/bundled_tabs/interface.png");

    private static final Map<CreativeModeTab, BundledTabGroup> GROUPS = new IdentityHashMap<>();

    public static BundledTabGroup registerGroup(Supplier<CreativeModeTab> tab, Supplier<ResourceLocation> texture) {
        CreativeModeTab resolved = tab.get();
        return GROUPS.computeIfAbsent(resolved, ignored -> new BundledTabGroup(tab, texture));
    }

    public static BundledTabGroup registerGroup(Supplier<CreativeModeTab> tab) {
        return registerGroup(tab, () -> DEFAULT_TEXTURE);
    }

    public static void addTab(Supplier<CreativeModeTab> tab, BundledTab bundledTab) {
        registerGroup(tab).addTab(bundledTab);
    }

    public static BundledTabGroup getGroup(CreativeModeTab tab) {
        return GROUPS.get(tab);
    }
}
