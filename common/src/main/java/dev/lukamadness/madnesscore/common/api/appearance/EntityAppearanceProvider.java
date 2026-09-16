package dev.lukamadness.madnesscore.common.api.appearance;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

/**
 * Fuente de datos de apariencia para un tipo de entidad. Se registran en {@link EntityAppearanceApi}
 * en orden de prioridad: el primero que devuelva un resultado presente gana.
 */
public interface EntityAppearanceProvider {
    boolean supports(Entity entity);

    Optional<EntityAppearance> resolve(MinecraftServer server, Entity entity);

    /**
     * Intenta escribir esta apariencia en la entidad (ej. MCA la guarda en la genética/dyes del
     * aldeano). Devuelve true si este provider pudo aplicarla. Los providers de solo lectura
     * (vanilla, etc.) se quedan con el default: no hacen nada y devuelven false.
     */
    default boolean apply(MinecraftServer server, Entity entity, EntityAppearance appearance) {
        return false;
    }
}