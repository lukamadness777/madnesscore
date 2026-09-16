package dev.lukamadness.madnesscore.common.client.render.clothing.renderer;

import dev.lukamadness.madnesscore.common.client.render.clothing.model.DyeableLegginsModel;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableLegginsItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DyeableGeoArmorRenderer;
import software.bernie.geckolib.util.Color;

public class DyeableLegginsRenderer extends DyeableGeoArmorRenderer<DyeableLegginsItem> {
    public DyeableLegginsRenderer() {
        super(new DyeableLegginsModel());
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        this.setAllVisible(true);
    }

    @Override
    protected boolean isBoneDyeable(GeoBone bone) {
        String name = bone.getName();
        return !name.equals("logoLayer") && !name.equals("someOtherLayer");
    }

    @Override
    protected @NotNull Color getColorForBone(GeoBone bone) {
        if (bone.getName().equals("logoLayer")) {
            return Color.WHITE;
        }

        if (this.currentStack != null) {
            DyedItemColor comp = this.currentStack.get(DataComponents.DYED_COLOR);
            if (comp != null) {
                return new Color(0xFF000000 | comp.rgb());
            }
        }

        return Color.WHITE;
    }
}
