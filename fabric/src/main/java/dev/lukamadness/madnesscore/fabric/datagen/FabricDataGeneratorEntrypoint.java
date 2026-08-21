package dev.lukamadness.madnesscore.fabric.datagen;

import dev.lukamadness.madnesscore.common.datagen.ModItemModelProvider;
import dev.lukamadness.madnesscore.common.datagen.ModRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class FabricDataGeneratorEntrypoint implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider((FabricDataGenerator.Pack.Factory<ModItemModelProvider>) ModItemModelProvider::new);
    }
}