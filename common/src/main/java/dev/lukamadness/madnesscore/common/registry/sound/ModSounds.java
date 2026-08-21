package dev.lukamadness.madnesscore.common.registry.sound;

import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * Registro multiplataforma de SoundEvent.
 * Requiere el archivo sounds.json en resources con la misma id.
 * <p>
 * Ejemplo de uso:
 * <pre>{@code
 * public static final Supplier<SoundEvent> EXAMPLE_SOUND = register("example_sound");
 * }</pre>
 */
public class ModSounds {

    private static Supplier<SoundEvent> register(String id) {
        return RegistryHelper.INSTANCE.registerSound(id);
    }

    public static void init() {
    }
}
