package dev.lukamadness.madnesscore.common.registry.menu;

import dev.lukamadness.madnesscore.common.content.technology.blocks.AlloySmelteryBlockEntity;
import dev.lukamadness.madnesscore.common.content.technology.screen.AlloySmelteryScreenHandler;
import dev.lukamadness.madnesscore.common.content.technology.screen.CompressorScreenHandler;
import dev.lukamadness.madnesscore.common.content.technology.screen.EnergyConverterScreenHandler;
import dev.lukamadness.madnesscore.common.content.technology.screen.HeatGeneratorScreenHandler;
import dev.lukamadness.madnesscore.common.content.tailoring.blocks.TailoringTableBlockEntity;
import dev.lukamadness.madnesscore.common.content.tailoring.screen.TailoringTableScreenHandler;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;

import java.util.function.Supplier;

public class ModMenus {
    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(
            String id,
            MenuFactory<T> factory
    ) {
        return RegistryHelperLoader.INSTANCE.registerMenu(id, factory);
    }

    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(
            String namespace,
            String id,
            MenuFactory<T> factory
    ) {
        return RegistryHelperLoader.INSTANCE.registerMenu(namespace, id, factory);
    }

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

    public static final Supplier<MenuType<EnergyConverterScreenHandler>> ENERGY_CONVERTER = register(
            "energy_converter",
            (windowId, inv) -> new EnergyConverterScreenHandler(windowId, inv)
    );

    public static final Supplier<MenuType<CompressorScreenHandler>> COMPRESSOR = register(
            "compressor",
            (windowId, inv) -> new CompressorScreenHandler(
                    windowId, inv, new SimpleContainer(2), new SimpleContainerData(4))
    );

    public static final Supplier<MenuType<TailoringTableScreenHandler>> TAILORING_TABLE = register(
            "tailoring_table",
            (windowId, inv) -> new TailoringTableScreenHandler(
                    windowId, inv, new SimpleContainer(TailoringTableBlockEntity.INPUT_SLOTS))
    );

    public static void init() {
    }
}