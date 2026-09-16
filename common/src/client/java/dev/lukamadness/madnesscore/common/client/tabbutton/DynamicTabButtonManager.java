package dev.lukamadness.madnesscore.common.client.tabbutton;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class DynamicTabButtonManager {
    private static final int BUTTON_SIZE = 26;
    private static final int BUTTON_SPACING = 30;

    /**
     * Corrimiento extra hacia la izquierda cuando la pestaña actual tiene un bundled tab activo
     * (ver {@code BundledTabSelector}/{@code BundledTabsAPI}): la franja de bundled tabs ocupa
     * los 30px inmediatamente a la izquierda del inventario (de {@code guiLeft - 30} a {@code
     * guiLeft}), así que los dynamic tab buttons tienen que correrse más allá de esa franja para
     * no superponerse.
     */
    public static final int BUNDLED_TAB_EXTRA_OFFSET = 28;

    private static final List<DynamicTabButtonSource> SOURCES = new ArrayList<>();
    private static final LinkedHashMap<String, ActiveButton> ACTIVE = new LinkedHashMap<>();

    private DynamicTabButtonManager() {}

    public static void register(DynamicTabButtonSource source) {
        SOURCES.add(source);
    }

    public static boolean refresh(LocalPlayer player) {
        LinkedHashMap<String, ActiveButton> current = new LinkedHashMap<>();

        for (DynamicTabButtonSource source : SOURCES) {
            for (Map.Entry<String, ItemStack> entry : source.getActiveInstances(player).entrySet()) {
                String key = source.sourceId() + "::" + entry.getKey();
                current.put(key, new ActiveButton(source, entry.getKey(), entry.getValue()));
            }
        }

        boolean changed = false;

        Iterator<String> it = ACTIVE.keySet().iterator();
        while (it.hasNext()) {
            if (!current.containsKey(it.next())) {
                it.remove();
                changed = true;
            }
        }

        for (Map.Entry<String, ActiveButton> entry : current.entrySet()) {
            if (!ACTIVE.containsKey(entry.getKey())) {
                ACTIVE.put(entry.getKey(), entry.getValue());
                changed = true;
            }
        }

        return changed;
    }

    public static List<ActiveButton> getOrderedActiveButtons() {
        return new ArrayList<>(ACTIVE.values());
    }

    /** Sin bundled tab activo (comportamiento de siempre). Ver el overload de 4 args. */
    public static List<DynamicTabButtonWidget> installButtons(int guiLeft, int guiTop, Consumer<AbstractWidget> addWidget) {
        return installButtons(guiLeft, guiTop, false, addWidget);
    }

    /**
     * @param bundledTabActive true si la pestaña actualmente seleccionada tiene un bundled tab
     *                         activo (su franja se dibuja en guiLeft-30..guiLeft) — en ese caso
     *                         los botones se corren {@link #BUNDLED_TAB_EXTRA_OFFSET}px más a
     *                         la izquierda para no quedar tapados por esa franja.
     * @return los widgets creados, para que el caller los pueda reposicionar más adelante (ver
     *         {@code MixinCreativeModeInventoryScreen#selectTab}, donde el bundled tab activo
     *         puede cambiar sin que la screen se reinicialice entera).
     */
    public static List<DynamicTabButtonWidget> installButtons(int guiLeft, int guiTop, boolean bundledTabActive,
                                                                Consumer<AbstractWidget> addWidget) {
        int extraOffset = bundledTabActive ? BUNDLED_TAB_EXTRA_OFFSET : 0;
        List<ActiveButton> buttons = getOrderedActiveButtons();
        List<DynamicTabButtonWidget> installed = new ArrayList<>(buttons.size());

        for (int i = 0; i < buttons.size(); i++) {
            ActiveButton entry = buttons.get(i);

            DynamicTabButtonWidget button = new DynamicTabButtonWidget(
                    guiLeft - 30 - extraOffset, guiTop + 4 + (i * BUTTON_SPACING), BUTTON_SIZE, BUTTON_SIZE,
                    entry.source().label(),
                    entry.source().buttonIcon(),
                    false,
                    () -> entry.source().onClick(entry.instanceKey())
            );

            addWidget.accept(button);
            installed.add(button);
        }

        return installed;
    }

    public record ActiveButton(DynamicTabButtonSource source, String instanceKey, ItemStack stack) {}
}