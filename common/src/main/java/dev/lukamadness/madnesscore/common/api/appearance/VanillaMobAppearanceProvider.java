package dev.lukamadness.madnesscore.common.api.appearance;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.Villager;

import java.util.Optional;

/**
 * Fallback vanilla: se usa cuando ningún provider más específico (ej. integración de un mod de
 * apariencia de aldeanos como Minecraft Comes Alive) devolvió resultado para esa entidad.
 */
public final class VanillaMobAppearanceProvider implements EntityAppearanceProvider {
    @Override
    public boolean supports(Entity entity) {
        return entity instanceof Villager
                || entity instanceof Vindicator
                || entity instanceof Evoker
                || entity instanceof Illusioner
                || entity instanceof Pillager;
    }

    @Override
    public Optional<EntityAppearance> resolve(MinecraftServer server, Entity entity) {
        if (entity instanceof Villager) return Optional.of(VanillaMobAppearance.VILLAGER);
        if (entity instanceof Vindicator) return Optional.of(VanillaMobAppearance.VINDICATOR);
        if (entity instanceof Evoker) return Optional.of(VanillaMobAppearance.EVOKER);
        if (entity instanceof Illusioner) return Optional.of(VanillaMobAppearance.ILLUSIONER);
        if (entity instanceof Pillager) return Optional.of(VanillaMobAppearance.PILLAGER);
        return Optional.empty();
    }
}
