package dev.lukamadness.madnesscore.common.api.appearance;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Punto de entrada único para consultar la apariencia (piel/pelo/ojos) de cualquier entidad:
 * jugadores, aldeanos, illagers, o lo que registre un addon (ej. una integración con Minecraft
 * Comes Alive que devuelva los colores reales calculados por MCA en vez del default vanilla).
 */
public final class EntityAppearanceApi {
    private static final List<EntityAppearanceProvider> PROVIDERS = new ArrayList<>();

    static {
        registerProvider(new PlayerAppearanceProvider());
        registerProvider(new VanillaMobAppearanceProvider());
    }

    private EntityAppearanceApi() {}

    /**
     * Registra un provider con MAYOR prioridad que los ya registrados (queda primero en la cola).
     * Así, una integración opcional (ej. MCA) se registra después de los defaults de madnesscore
     * y los reemplaza para las entidades que sí sabe resolver, sin tocar este archivo.
     */
    public static void registerProvider(EntityAppearanceProvider provider) {
        PROVIDERS.add(0, provider);
    }

    public static Optional<EntityAppearance> getAppearance(MinecraftServer server, Entity entity) {
        for (EntityAppearanceProvider provider : PROVIDERS) {
            if (provider.supports(entity)) {
                Optional<EntityAppearance> result = provider.resolve(server, entity);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Intenta poner esta apariencia en la entidad. Recorre los providers en el mismo orden que
     * {@link #getAppearance}: el primero que sepa cómo escribirla (ej. la integración de MCA) gana.
     * Devuelve false si ningún provider registrado sabe escribir apariencia para esa entidad.
     */
    public static boolean setAppearance(MinecraftServer server, Entity entity, EntityAppearance appearance) {
        for (EntityAppearanceProvider provider : PROVIDERS) {
            if (provider.supports(entity) && provider.apply(server, entity, appearance)) {
                return true;
            }
        }
        return false;
    }

    public static boolean setAppearance(MinecraftServer server, UUID uuid, EntityAppearance appearance) {
        Entity entity = findEntity(server, uuid);
        return entity != null && setAppearance(server, entity, appearance);
    }

    public static Optional<EntityAppearance> getAppearance(MinecraftServer server, UUID uuid) {
        Entity entity = server.getAllLevels().iterator().hasNext()
                ? findEntity(server, uuid)
                : null;
        return entity == null ? Optional.empty() : getAppearance(server, entity);
    }

    private static Entity findEntity(MinecraftServer server, UUID uuid) {
        for (var level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) return entity;
        }
        return null;
    }
}