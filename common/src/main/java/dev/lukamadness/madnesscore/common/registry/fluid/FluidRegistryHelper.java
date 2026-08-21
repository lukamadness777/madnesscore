package dev.lukamadness.madnesscore.common.registry.fluid;

import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ServiceLoader;
import java.util.function.UnaryOperator;

/**
 * API de registro de fluidos multiplataforma (Fabric + NeoForge, sin
 * Architectury). Registra en un solo paso: fluido source, fluido flowing,
 * el LiquidBlock y (opcional) el bucket item.
 * <p>
 * En NeoForge ademas registra el FluidType requerido por esa plataforma;
 * en Fabric no hace falta (Fabric no tiene el concepto de FluidType), pero
 * las texturas/tinte de {@link ModFluidProperties} se usan igual para
 * registrar el FluidRenderHandler client-side.
 */
public interface FluidRegistryHelper {

    FluidRegistryHelper INSTANCE = ServiceLoader.load(FluidRegistryHelper.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                    "No se encontro una implementacion de FluidRegistryHelper (falta el archivo META-INF/services)"));

    /**
     * @param id                 nombre de la variante source (ej: "oil"). La variante flowing
     *                           se registra automaticamente como "flowing_" + id, igual que vanilla.
     * @param properties         texturas/tinte/densidad/viscosidad/luminosidad del fluido.
     * @param blockPropertiesOp  permite ajustar el BlockBehaviour.Properties del LiquidBlock
     *                           (por defecto es una copia de las propiedades del agua).
     * @param withBucket         si es true tambien registra {@code "<id>_bucket"}.
     */
    FluidEntry<SimpleFlowingFluid> registerFluid(
            String id,
            ModFluidProperties properties,
            UnaryOperator<BlockBehaviour.Properties> blockPropertiesOp,
            boolean withBucket
    );

    default FluidEntry<SimpleFlowingFluid> registerFluid(String id, ModFluidProperties properties) {
        return registerFluid(id, properties, UnaryOperator.identity(), true);
    }

    default FluidEntry<SimpleFlowingFluid> registerFluidNoBucket(String id, ModFluidProperties properties) {
        return registerFluid(id, properties, UnaryOperator.identity(), false);
    }
}
