package dev.lukamadness.madnesscore.common.client.slots;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Comportamiento de render (Fase 4) de un item equipado en un slot. Registrar implementaciones
 * con {@link SlotRendererRegistry#registerRenderer}; sin renderer registrado, el item equipado
 * simplemente no dibuja nada extra sobre la entidad (igual que Trinkets: el modelo 3D es
 * responsabilidad del item, esto solo provee el enganche al pipeline de render).
 * <p>
 * Portado de dev.emi.trinkets.api.client.TrinketRenderer, adaptado a la API de render de 1.21.1
 * (previa al refactor de "entity render state" que Trinkets usa en versiones mas nuevas de MC):
 * se recibe la {@link LivingEntity} directamente en vez de un {@code LivingEntityRenderState}.
 * <p>
 * NOTA: los nombres exactos de {@code RenderLayer}/{@code RenderLayerParent}/{@code EntityModel}
 * corresponden a las mappings oficiales de Mojang para 1.21.1 seg\u00fan mi conocimiento; conviene
 * confirmarlos contra el jar decompilado en el IDE antes de compilar, ya que no tengo forma de
 * verificarlos en este entorno.
 */
public interface SlotRenderer<T extends LivingEntity> {

    void render(ItemStack stack, SlotReference slotReference, EntityModel<T> contextModel,
                PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                float netHeadYaw, float headPitch);
}