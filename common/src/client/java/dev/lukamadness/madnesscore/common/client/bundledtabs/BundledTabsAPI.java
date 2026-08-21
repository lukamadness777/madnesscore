package dev.lukamadness.madnesscore.common.client.bundledtabs;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Punto de entrada público para agregar "bundle tabs" (carpetas laterales
 * tipo Dark Realms) a cualquier {@link CreativeModeTab}, desde cualquier mod
 * que dependa de Madness Core.
 * <p>
 * Uso desde el client init de otro mod (después de que sus items/tabs ya
 * estén registrados):
 * <pre>{@code
 * BundledTabGroup group = BundledTabsAPI.registerGroup(
 *         MyModCreativeTabs.MY_TAB,
 *         () -> ResourceLocation.fromNamespaceAndPath("mymod", "textures/gui/bundled_tabs/interface.png"));
 *
 * group.addTab(BundledTab.builder()
 *         .title(Component.translatable("bundledTab.mymod.tools"))
 *         .icon(() -> new ItemStack(MyItems.MY_PICKAXE.get()))
 *         .displayItems((provider, output) -> {
 *             output.accept(MyItems.MY_PICKAXE.get());
 *             output.accept(MyItems.MY_AXE.get());
 *         })
 *         .build());
 *
 * group.addTab(BundledTab.builder()
 *         .title(Component.translatable("bundledTab.mymod.blocks"))
 *         .icon(() -> new ItemStack(MyBlocks.MY_STONE.get()))
 *         .displayItems((provider, output) -> output.accept(MyBlocks.MY_STONE.get()))
 *         .build());
 * }</pre>
 * <p>
 * Si no llamás {@link #registerGroup(Supplier, Supplier)} explícitamente,
 * {@link #addTab(Supplier, BundledTab)} crea el grupo solo, usando la
 * textura por defecto de Madness Core.
 * <p>
 * No hace falta ningún otro paso: el selector lee el grupo correspondiente
 * cada vez que el jugador abre/cambia de pestaña en el inventario creativo.
 * Registrar en cualquier momento del client init alcanza.
 * <p>
 * Thread-safety: pensado para registrarse una sola vez durante la
 * inicialización del client (un solo hilo), no para registrar pestañas
 * dinámicamente en pleno gameplay.
 */
public final class BundledTabsAPI {
    private BundledTabsAPI() {}

    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/bundled_tabs/interface.png");

    private static final Map<CreativeModeTab, BundledTabGroup> GROUPS = new IdentityHashMap<>();

    /**
     * Registra (o devuelve, si ya existía) el grupo de bundle tabs para el
     * CreativeModeTab dado, con una textura custom.
     */
    public static BundledTabGroup registerGroup(Supplier<CreativeModeTab> tab, Supplier<ResourceLocation> texture) {
        CreativeModeTab resolved = tab.get();
        return GROUPS.computeIfAbsent(resolved, ignored -> new BundledTabGroup(tab, texture));
    }

    /**
     * Igual que {@link #registerGroup(Supplier, Supplier)} pero usando la
     * textura por defecto de Madness Core.
     */
    public static BundledTabGroup registerGroup(Supplier<CreativeModeTab> tab) {
        return registerGroup(tab, () -> DEFAULT_TEXTURE);
    }

    /**
     * Atajo: agrega un bundle tab directo, creando el grupo (con textura
     * default) si todavía no existía para ese CreativeModeTab.
     */
    public static void addTab(Supplier<CreativeModeTab> tab, BundledTab bundledTab) {
        registerGroup(tab).addTab(bundledTab);
    }

    /**
     * El grupo registrado para ese CreativeModeTab, o {@code null} si nadie
     * registró bundle tabs para él (en cuyo caso el selector no se muestra).
     */
    public static BundledTabGroup getGroup(CreativeModeTab tab) {
        return GROUPS.get(tab);
    }
}
