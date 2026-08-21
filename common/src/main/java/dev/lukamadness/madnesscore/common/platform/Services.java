package dev.lukamadness.madnesscore.common.platform;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.platform.services.IPlatformHelper;
import dev.lukamadness.madnesscore.common.platform.services.ISlotAttachment;
import dev.lukamadness.madnesscore.common.platform.services.ISlotNetwork;

import java.util.ServiceLoader;

// Service loaders are a built-in Java feature that allow us to locate implementations of an interface that vary from one
// environment to another. In the context of MultiLoader we use this feature to access a mock API in the common code that
// is swapped out for the platform specific implementation at runtime.
public class Services {

    // In this example we provide a platform helper which provides information about what platform the mod is running on.
    // For example this can be used to check if the code is running on Forge vs Fabric, or to ask the modloader if another
    // mod is loaded.
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    // Resuelve como se adjunta el LivingEntitySlotComponent a una entidad: mixin en Fabric,
    // Data Attachment en NeoForge. Ver dev.lukamadness.madnesscore.common.slots.SlotsApi.
    public static final ISlotAttachment SLOT_ATTACHMENT = load(ISlotAttachment.class);

    // Resuelve como se envian/registran los paquetes de red del sistema de slots (Fase 4):
    // Fabric Networking API vs PacketDistributor de NeoForge. Ver
    // dev.lukamadness.madnesscore.common.slots.network.SlotNetworking.
    public static final ISlotNetwork SLOT_NETWORK = load(ISlotNetwork.class);

    // This code is used to load a service for the current environment. Your implementation of the service must be defined
    // manually by including a text file in META-INF/services named with the fully qualified class name of the service.
    // Inside the file you should write the fully qualified class name of the implementation to load for the platform. For
    // example our file on Forge points to ForgePlatformHelper while Fabric points to FabricPlatformHelper.
    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        MadnessCoreCommon.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}