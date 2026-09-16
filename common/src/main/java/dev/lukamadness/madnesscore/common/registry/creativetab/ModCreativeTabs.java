package dev.lukamadness.madnesscore.common.registry.creativetab;

import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final Supplier<CreativeModeTab> MADNESS_CORE_TAB = register(
            "madnesscore_tab",
            () -> RegistryHelperLoader.INSTANCE.creativeTabBuilder()
                    .title(Component.translatable("itemGroup.madnesscore.madnesscore_tab"))
                    .icon(() -> new ItemStack(ModItems.RED_FABRIC.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.RED_FABRIC.get());
                        output.accept(ModItems.HEAT_GENERATOR.get());
                        output.accept(ModItems.ALLOY_SMELTERY.get());
                        output.accept(ModItems.ENERGY_CONVERTER.get());
                        output.accept(ModItems.COMPRESSOR.get());
                    })
    );

    public static Supplier<CreativeModeTab> register(String id, Supplier<CreativeModeTab.Builder> tabBuilder) {
        return RegistryHelperLoader.INSTANCE.registerCreativeTab(id, tabBuilder);
    }

    public static Supplier<CreativeModeTab> register(String namespace, String id, Supplier<CreativeModeTab.Builder> tabBuilder) {
        return RegistryHelperLoader.INSTANCE.registerCreativeTab(namespace, id, tabBuilder);
    }

    public static void init() {
    }
}
