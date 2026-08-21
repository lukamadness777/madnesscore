package dev.lukamadness.madnesscore.common.registry.menu;

import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.screen.AlloySmelteryScreenHandler;
import dev.lukamadness.madnesscore.common.content.technology.screen.EnergyConverterScreenHandler;
import dev.lukamadness.madnesscore.common.content.technology.screen.HeatGeneratorScreenHandler;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;

import java.util.function.Supplier;

/**
 * Registro multiplataforma de MenuType (pantallas / GUIs).
 * <p>
 * El sistema de slots (Trinkets-style) ya no usa un MenuType propio: sus slots se inyectan
 * directamente en {@code InventoryMenu} (ver {@code MixinInventoryMenu}), el mismo contenedor
 * vainilla del inventario normal, en vez de abrir una pantalla aparte.
 * <p>
 * Ejemplo de uso si en el futuro se necesita registrar un menu propio:
 * <pre>{@code
 * public static final Supplier<MenuType<ExampleMenu>> EXAMPLE_MENU = register(
 *         "example_menu",
 *         (windowId, inv) -> new ExampleMenu(windowId, inv)
 * );
 * }</pre>
 */
public class ModMenus {

    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> Supplier<MenuType<T>> register(
            String id,
            MenuFactory<T> factory
    ) {
        return RegistryHelper.INSTANCE.registerMenu(id, factory);
    }

    /**
     * Factory "dummy" registrada como MenuSupplier vanilla. MenuType.MenuSupplier solo
     * recibe (windowId, Inventory) — no puede cargar el BlockPos real del generador. Esta
     * instancia nunca debería usarse para abrir la pantalla de verdad: cada plataforma abre
     * el menu real llamando directamente al constructor
     * {@code HeatGeneratorScreenHandler(syncId, playerInventory, pos)} a través de su propio
     * mecanismo de datos extendidos (Fabric: ExtendedScreenHandlerType con PacketByteBuf;
     * NeoForge: IMenuTypeExtension con RegistryFriendlyByteBuf), pasando el MenuType de acá
     * como tipo registrado. Este factory solo existe para satisfacer el registro vanilla.
     */
    @SuppressWarnings("unchecked")
    public static final Supplier<MenuType<HeatGeneratorScreenHandler>> HEAT_GENERATOR = register(
            "heat_generator",
            (windowId, inv) -> new HeatGeneratorScreenHandler(
                    windowId, inv, new SimpleContainer(1), new SimpleContainerData(4))
    );

    public static final Supplier<MenuType<AlloySmelteryScreenHandler>> ALLOY_SMELTERY = register(
            "alloy_smeltery",
            (windowId, inv) -> new AlloySmelteryScreenHandler(
                    windowId, inv, new SimpleContainer(AlloySmelteryBlockEntity.TOTAL_SLOTS), new SimpleContainerData(4))
    );

    // Sin slots ni Container: la pantalla es puramente informativa (ver
    // EnergyConverterScreenHandler), así que el factory "dummy" solo necesita el
    // SimpleContainerData con los mismos 4 índices (heat, heatCap, energy, energyCap).
    public static final Supplier<MenuType<EnergyConverterScreenHandler>> ENERGY_CONVERTER = register(
            "energy_converter",
            (windowId, inv) -> new EnergyConverterScreenHandler(windowId, inv)
    );

    public static void init() {
        // Fuerza la carga de la clase para que los Supplier de arriba corran.
    }
}