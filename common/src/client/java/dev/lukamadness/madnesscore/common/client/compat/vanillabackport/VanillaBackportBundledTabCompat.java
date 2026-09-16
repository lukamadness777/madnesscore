package dev.lukamadness.madnesscore.common.client.compat.vanillabackport;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Compat opcional con VanillaBackport: no depende de su jar (ni en compile ni en runtime).
 * Su creative tab se busca por id en el registro vanilla de CreativeModeTab, así que esto
 * funciona sin importar de dónde venga el mod instalado (CurseForge, Modrinth, jar suelto, etc.).
 * Si VanillaBackport no está presente, la búsqueda simplemente devuelve null y listo.
 */
public final class VanillaBackportBundledTabCompat {
    private static final ResourceLocation VANILLA_BACKPORT_TAB_ID =
            ResourceLocation.fromNamespaceAndPath("vanillabackport", "vanilla_backport");

    private VanillaBackportBundledTabCompat() {}

    public static boolean hasBundledTab(CreativeModeTab tab) {
        if (tab == null) return false;
        CreativeModeTab vbTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(VANILLA_BACKPORT_TAB_ID);
        return vbTab != null && vbTab == tab;
    }
}