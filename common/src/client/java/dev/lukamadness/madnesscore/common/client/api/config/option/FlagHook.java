package dev.lukamadness.madnesscore.common.client.api.config.option;

import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.BiConsumer;

public interface FlagHook extends BiConsumer<Collection<ResourceLocation>, ConfigState> {
    Collection<ResourceLocation> getTriggers();
}
