package dev.lukamadness.madnesscore.common.registry.gamerule;

import net.minecraft.world.level.GameRules;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ModGameRules {

    private ModGameRules() {}

    public static final GameRules.Key<GameRules.BooleanValue> RESET_SPECIES_ON_DEATH =
            GameRules.register("madnesscoreResetSpeciesOnDeath", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> RESET_BLOODLINES_ON_DEATH =
            GameRules.register("madnesscoreResetBloodlinesOnDeath", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(false));

    public static final GameRules.Key<GameRules.BooleanValue> RESET_FAMILIES_ON_DEATH =
            GameRules.register("madnesscoreResetFamiliesOnDeath", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(false));

    public static void init() {
    }
}