package dev.lukamadness.madnesscore.common.registry.fluid;

/**
 * Registro de fluidos custom del mod. Vacio por ahora: dejar aca los
 * campos estaticos siguiendo el ejemplo de abajo.
 * <p>
 * <pre>{@code
 * public static final FluidEntry<SimpleFlowingFluid> OIL = FluidRegistryHelper.INSTANCE.registerFluid(
 *         "oil",
 *         ModFluidProperties.of(
 *                 RegistryHelper.id("block/oil_still"),
 *                 RegistryHelper.id("block/oil_flow")
 *         ).tintColor(0xFF1A1A1A).viscosity(1500).density(1200)
 * );
 *
 * // Uso posterior:
 * OIL.source().get();   // Fluid (still)
 * OIL.flowing().get();  // Fluid (flowing)
 * OIL.block().get();    // LiquidBlock, para poder registrarlo si hace falta un BlockItem, etc.
 * OIL.bucket().get();   // Item del bucket ("oil_bucket")
 * }</pre>
 */
public class ModFluids {

    public static void init() {
        // Fuerza la carga de la clase para que los FluidEntry de arriba se registren
    }
}
