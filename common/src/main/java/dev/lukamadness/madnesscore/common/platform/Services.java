package dev.lukamadness.madnesscore.common.platform;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.platform.services.IAppearanceNetwork;
import dev.lukamadness.madnesscore.common.platform.services.IPlatformHelper;
import dev.lukamadness.madnesscore.common.platform.services.ISlotAttachment;
import dev.lukamadness.madnesscore.common.platform.services.ISlotNetwork;
import dev.lukamadness.madnesscore.common.platform.services.ITailoringNetwork;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static final ISlotAttachment SLOT_ATTACHMENT = load(ISlotAttachment.class);

    public static final ISlotNetwork SLOT_NETWORK = load(ISlotNetwork.class);

    public static final IAppearanceNetwork APPEARANCE_NETWORK = load(IAppearanceNetwork.class);

    public static final ITailoringNetwork TAILORING_NETWORK = load(ITailoringNetwork.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        MadnessCoreCommon.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
