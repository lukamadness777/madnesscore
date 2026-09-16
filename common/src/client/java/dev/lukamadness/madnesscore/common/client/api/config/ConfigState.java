package dev.lukamadness.madnesscore.common.client.api.config;

import net.minecraft.resources.ResourceLocation;

public interface ConfigState {
    ResourceLocation UPDATE_ON_REBUILD = ResourceLocation.parse("__meta__:update_on_rebuild");

    ResourceLocation UPDATE_ON_APPLY = ResourceLocation.parse("__meta__:update_on_apply");

    boolean readBooleanOption(ResourceLocation id);

    int readIntOption(ResourceLocation id);

    <E extends Enum<E>> E readEnumOption(ResourceLocation id, Class<E> enumClass);
}
