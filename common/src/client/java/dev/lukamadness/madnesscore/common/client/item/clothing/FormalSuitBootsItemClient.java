package dev.lukamadness.madnesscore.common.client.item.clothing;

import dev.lukamadness.madnesscore.common.client.render.clothing.renderer.FormalSuitBootsRenderer;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitBootsItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.function.Consumer;

public class FormalSuitBootsItemClient extends FormalSuitBootsItem implements GeoItem {
    public FormalSuitBootsItemClient(Holder<ArmorMaterial> material, Properties properties) {
        super(material, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private FormalSuitBootsRenderer armorRenderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                return null;
            }

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(
                    @Nullable T livingEntity,
                    ItemStack stack,
                    @Nullable EquipmentSlot slot,
                    @Nullable HumanoidModel<T> original
            ) {
                if (this.armorRenderer == null) {
                    this.armorRenderer = new FormalSuitBootsRenderer();
                }
                return this.armorRenderer;
            }
        });
    }
}
