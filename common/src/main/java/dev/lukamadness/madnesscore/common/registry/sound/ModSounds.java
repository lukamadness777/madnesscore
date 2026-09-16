package dev.lukamadness.madnesscore.common.registry.sound;

import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class ModSounds {
    public static Supplier<SoundEvent> register(String id) {
        return RegistryHelperLoader.INSTANCE.registerSound(id);
    }

    public static Supplier<SoundEvent> register(String namespace, String id) {
        return RegistryHelperLoader.INSTANCE.registerSound(namespace, id);
    }

    public static void init() {
    }
}