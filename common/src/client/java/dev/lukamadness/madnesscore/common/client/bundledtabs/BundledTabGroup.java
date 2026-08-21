package dev.lukamadness.madnesscore.common.client.bundledtabs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Toda la configuración de la barra lateral de bundle tabs para UN
 * {@link CreativeModeTab}: qué {@link BundledTab}s tiene y con qué textura
 * se dibuja el selector.
 * <p>
 * La textura es un {@link Supplier}, no un valor fijo: se vuelve a evaluar
 * en cada frame que el selector está visible, así que el usuario de la API
 * puede devolver siempre la misma ResourceLocation (ícono estático) o
 * calcularla en el momento (ej. según la dimensión actual del jugador, la
 * hora del día, un logro desbloqueado, etc.). El archivo debe respetar el
 * mismo layout de UV que la textura por defecto de Madness Core para que
 * el selector se vea bien (mismas proporciones, solo cambia el dibujo).
 */
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
