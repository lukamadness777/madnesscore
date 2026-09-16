package dev.lukamadness.madnesscore.fabric.slots.data;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class FabricIdentifiableReloadListener implements IdentifiableResourceReloadListener {
    private final PreparableReloadListener delegate;
    private final ResourceLocation id;
    private final Collection<ResourceLocation> dependencies;

    public FabricIdentifiableReloadListener(PreparableReloadListener delegate, ResourceLocation id,
                                            Collection<ResourceLocation> dependencies) {
        this.delegate = delegate;
        this.id = id;
        this.dependencies = dependencies;
    }

    @Override
    public ResourceLocation getFabricId() {
        return id;
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return dependencies;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager,
                                          ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return delegate.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler,
                backgroundExecutor, gameExecutor);
    }
}
