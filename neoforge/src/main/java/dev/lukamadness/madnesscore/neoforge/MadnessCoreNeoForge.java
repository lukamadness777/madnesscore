package dev.lukamadness.madnesscore.neoforge;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.neoforge.datagen.NeoForgeDataGenerator;
import dev.lukamadness.madnesscore.neoforge.registry.NeoForgeRegistryHelper;
import dev.lukamadness.madnesscore.neoforge.registry.fluid.NeoForgeFluidRegistryHelper;
import dev.lukamadness.madnesscore.neoforge.slots.SlotAttachments;
import dev.lukamadness.madnesscore.neoforge.slots.SlotEventListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MadnessCoreCommon.MOD_ID)
public class MadnessCoreNeoForge {

    public MadnessCoreNeoForge(IEventBus eventBus) {
        MadnessCoreCommon.LOG.info("Hello NeoForge world!");
        NeoForgeRegistryHelper.registerToBus(eventBus);
        NeoForgeFluidRegistryHelper.registerToBus(eventBus);
        SlotAttachments.registerToBus(eventBus);
        NeoForge.EVENT_BUS.register(new SlotEventListener());
        MadnessCoreCommon.init();
        NeoForgeDataGenerator.registerToBus(eventBus); // <-- ¿está esta línea?
    }
}