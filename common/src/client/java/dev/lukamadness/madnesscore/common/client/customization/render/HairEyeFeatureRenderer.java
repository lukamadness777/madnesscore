package dev.lukamadness.madnesscore.common.client.customization.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lukamadness.madnesscore.common.api.appearance.HairEyeColorMode;
import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;
import dev.lukamadness.madnesscore.common.client.customization.network.AppearanceCache;
import dev.lukamadness.madnesscore.common.client.customization.network.AppearanceConfigPayloadHelper;
import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;

/**
 * Dibuja las máscaras de pelo y ojos ({@code hairPixels}/{@code eyePixels} de
 * {@code CustomizationConfig}) encima del skin del jugador, según el {@link HairEyeColorMode}
 * elegido para cada parte:
 * <ul>
 *   <li>{@code SOLID}/{@code MULTIPLY}: la máscara ({@link AppearanceMaskTexture}) es blanca opaca
 *       en los píxeles marcados; el color real lo pone el tinte que se pasa a
 *       {@code Model#renderToBuffer}.</li>
 *   <li>{@code GLOW_CUSTOM}: igual que SOLID pero con {@link MadnessCoreRenderTypes#glow}
 *       (aditivo, full-bright, tipo ojos de Enderman) tiñendo con el color elegido.</li>
 *   <li>{@code GLOW_SELF}: la máscara ya trae horneado el color que el skin tenía ahí; se dibuja
 *       en modo glow sin tinte adicional (blanco = "no tocar el color horneado").</li>
 * </ul>
 * <p>
 * Para el jugador local lee directo de {@link CustomizationConfig} (no depende del roundtrip de
 * red); para el resto de jugadores usa lo último recibido en {@link AppearanceCache}. Si todavía
 * no hay nada cacheado para ese jugador (no llegó su payload aún), no dibuja nada ese frame.
 */
public class HairEyeFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public HairEyeFeatureRenderer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                        float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                        float netHeadYaw, float headPitch) {
        if (!(entity instanceof AbstractClientPlayer player) || player.isInvisible()) {
            return;
        }

        Resolved appearance = resolve(player);
        if (appearance == null) {
            return;
        }

        ResourceLocation skin = player.getSkin().texture();
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0f);
        UUID uuid = player.getUUID();

        renderPart(poseStack, bufferSource, packedLight, overlay, uuid, AppearanceMaskTexture.Part.HAIR,
                appearance.hairPixels, appearance.hairColorMode, appearance.hairColor, skin);
        renderPart(poseStack, bufferSource, packedLight, overlay, uuid, AppearanceMaskTexture.Part.EYE,
                appearance.eyePixels, appearance.eyeColorMode, appearance.eyeColor, skin);
    }

    private void renderPart(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int overlay,
                             UUID uuid, AppearanceMaskTexture.Part part, Map<String, boolean[][]> pixels,
                             HairEyeColorMode mode, int color, ResourceLocation skin) {
        if (pixels.isEmpty() || mode == null) {
            return;
        }

        ResourceLocation mask = AppearanceMaskTexture.getOrBuild(uuid, part, pixels, mode, skin);

        RenderType renderType;
        int light;
        int tint;
        switch (mode) {
            case MULTIPLY -> {
                renderType = MadnessCoreRenderTypes.multiply(mask);
                light = packedLight;
                tint = 0xFF000000 | (color & 0xFFFFFF);
            }
            case GLOW_CUSTOM -> {
                renderType = MadnessCoreRenderTypes.glow(mask);
                light = LightTexture.FULL_BRIGHT;
                tint = 0xFF000000 | (color & 0xFFFFFF);
            }
            case GLOW_SELF -> {
                renderType = MadnessCoreRenderTypes.glow(mask);
                light = LightTexture.FULL_BRIGHT;
                tint = 0xFFFFFFFF;
            }
            default -> { // SOLID
                renderType = MadnessCoreRenderTypes.solid(mask);
                light = packedLight;
                tint = 0xFF000000 | (color & 0xFFFFFF);
            }
        }

        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        this.getParentModel().renderToBuffer(poseStack, buffer, light, overlay, tint);
    }

    private record Resolved(Map<String, boolean[][]> hairPixels, HairEyeColorMode hairColorMode, int hairColor,
                             Map<String, boolean[][]> eyePixels, HairEyeColorMode eyeColorMode, int eyeColor) {}

    private Resolved resolve(AbstractClientPlayer player) {
        UUID uuid = player.getUUID();
        Minecraft client = Minecraft.getInstance();

        // Jugador local: leemos la config directo, sin depender de que ya haya vuelto del servidor.
        if (client.player != null && client.player.getUUID().equals(uuid)) {
            CustomizationConfig cfg = CustomizationConfig.get();
            return new Resolved(
                    cfg.hairPixels, cfg.hairColorMode, cfg.hairColor,
                    cfg.eyePixels, cfg.eyeColorMode, cfg.eyeColor);
        }

        AppearanceConfigPayload payload = AppearanceCache.get(uuid);
        if (payload == null) {
            return null;
        }
        return new Resolved(
                AppearanceConfigPayloadHelper.unpackHairPixels(payload),
                HairEyeColorMode.byOrdinal(payload.hairColorMode()),
                payload.hairColor(),
                AppearanceConfigPayloadHelper.unpackEyePixels(payload),
                HairEyeColorMode.byOrdinal(payload.eyeColorMode()),
                payload.eyeColor());
    }
}
