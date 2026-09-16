package dev.lukamadness.madnesscore.common.api.appearance;

import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import dev.lukamadness.madnesscore.common.network.AppearanceStore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public final class PlayerAppearanceProvider implements EntityAppearanceProvider {
    @Override
    public boolean supports(Entity entity) {
        return entity instanceof ServerPlayer;
    }

    @Override
    public Optional<EntityAppearance> resolve(MinecraftServer server, Entity entity) {
        ServerPlayer player = (ServerPlayer) entity;
        AppearanceConfigPayload cfg = AppearanceStore.get(player.getUUID());
        if (cfg == null) return Optional.empty();

        EntityAppearance.HairLength length = EntityAppearance.HairLength.byOrdinal(cfg.hairType());
        return Optional.of(new EntityAppearance(
                0xFF000000 | cfg.skinColor(),
                0xFF000000 | cfg.hairColor(),
                0xFF000000 | cfg.eyeColor(),
                length
        ));
    }
}
