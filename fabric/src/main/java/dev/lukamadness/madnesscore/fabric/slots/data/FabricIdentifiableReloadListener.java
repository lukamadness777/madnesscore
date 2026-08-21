package dev.lukamadness.madnesscore.fabric.slots.data;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Fabric API (fabric-resource-loader-v0 1.3.x+) solo acepta registrar reload listeners que
 * implementen {@link IdentifiableResourceReloadListener} (para poder ordenar por dependencias),
 * pero {@code SlotGroupReloadListener}/{@code EntitySlotReloadListener} viven en el modulo
 * {@code common} y no pueden depender de clases de Fabric API (rompería la compilacion en
 * NeoForge). Este wrapper delega toda la logica real al listener comun y solo aporta el id/las
 * dependencias que Fabric necesita.
 */
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