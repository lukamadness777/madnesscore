package dev.lukamadness.madnesscore.common.client.config.structure;

import dev.lukamadness.madnesscore.common.client.api.config.ConfigState;
import dev.lukamadness.madnesscore.common.client.api.config.option.FlagHook;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.BiConsumer;

public class FlagHookImpl implements FlagHook {
    private final BiConsumer<Collection<ResourceLocation>, ConfigState> hook;
    private final Collection<ResourceLocation> triggers;

    public FlagHookImpl(BiConsumer<Collection<ResourceLocation>, ConfigState> hook, Collection<ResourceLocation> triggers) {
        this.hook = hook;
        this.triggers = triggers;
    }

    @Override
    public Collection<ResourceLocation> getTriggers() {
        return this.triggers;
    }

    @Override
    public void accept(Collection<ResourceLocation> flags, ConfigState state) {
        this.hook.accept(flags, state);
    }
}
