package dev.lukamadness.madnesscore.common.client.customization.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * RenderTypes propios para pintar/hacer brillar las máscaras de pelo y ojos generadas por
 * {@link AppearanceMaskTexture}. Los tres modos posibles (ver {@code HairEyeColorMode}):
 *
 * <ul>
 *   <li>{@code solid(texture)}: pisa el color, opaco, se comporta como una capa normal del skin
 *       (afectada por la luz del mundo, igual que {@link RenderType#entityCutoutNoCull(ResourceLocation)}).</li>
 *   <li>{@code multiply(texture)}: multiplica el color de la máscara contra lo que ya está
 *       renderizado debajo (blend {@code DST_COLOR, ZERO}), conservando el sombreado del skin base.
 *       No se le aplica luz propia (se dibuja "sin filtrar") para no oscurecer doble la escena ya
 *       iluminada que está debajo.</li>
 *   <li>{@code glow(texture)}: idéntico a {@link RenderType#eyes(ResourceLocation)} (blend aditivo,
 *       sin escritura de profundidad) — el mismo mecanismo que usan los ojos del Enderman/araña.
 *       El brillo full-bright se logra pasando {@code LightTexture.FULL_BRIGHT} como packedLight al
 *       renderizar, no acá.</li>
 * </ul>
 */
public final class MadnessCoreRenderTypes {
    private MadnessCoreRenderTypes() {}

    private static final RenderStateShard.TransparencyStateShard MULTIPLY_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
            "madnesscore_multiply_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ZERO);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
    );

    public static RenderType solid(ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }

    public static RenderType multiply(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderType.RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(MULTIPLY_TRANSPARENCY)
                .setCullState(RenderType.NO_CULL)
                .setWriteMaskState(RenderType.COLOR_WRITE)
                .createCompositeState(false);

        return RenderType.create("madnesscore_hair_eye_multiply", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, false, true, state);
    }

    public static RenderType glow(ResourceLocation texture) {
        return RenderType.eyes(texture);
    }
}