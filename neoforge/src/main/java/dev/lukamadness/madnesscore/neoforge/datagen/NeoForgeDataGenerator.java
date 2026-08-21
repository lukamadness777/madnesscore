package dev.lukamadness.madnesscore.neoforge.datagen;

import dev.lukamadness.madnesscore.common.datagen.ModItemModelProvider;
import dev.lukamadness.madnesscore.common.datagen.ModRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class NeoForgeDataGenerator {

    // Se llama UNA vez desde MadnessCoreNeoForge(IEventBus), junto al resto de los registerToBus(...)
    public static void registerToBus(IEventBus eventBus) {
        eventBus.addListener(NeoForgeDataGenerator::gatherData);
    }

    private static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput));
    }
}