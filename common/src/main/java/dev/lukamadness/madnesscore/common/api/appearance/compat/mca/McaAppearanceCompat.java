package dev.lukamadness.madnesscore.common.api.appearance.compat.mca;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.appearance.EntityAppearanceApi;
import dev.lukamadness.madnesscore.common.platform.Services;

/**
 * Punto de entrada de la integración opcional con Minecraft Comes Alive. Sigue el mismo patrón que
 * {@code ModDyeDepotItems}: si MCA no está instalado, {@link #init()} no hace nada y ninguna clase
 * de este paquete que referencie net.conczin.mca.* llega a cargarse (McaAppearanceProvider solo se
 * instancia acá adentro del if).
 */
public final class McaAppearanceCompat {
    public static final String MCA_MOD_ID = "mca";

    private McaAppearanceCompat() {}

    public static boolean isEnabled() {
        return Services.PLATFORM.isModLoaded(MCA_MOD_ID);
    }

    public static void init() {
        if (!isEnabled()) return;

        EntityAppearanceApi.registerProvider(new McaAppearanceProvider());
        MadnessCoreCommon.LOG.info("Minecraft Comes Alive detectado: aldeanos MCA usarán su apariencia genética real en EntityAppearanceApi");
    }
}