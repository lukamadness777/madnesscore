package dev.lukamadness.madnesscore.common.client.render.clothing.renderer;

import dev.lukamadness.madnesscore.common.client.render.clothing.model.FormalSuitModel;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalColors;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitItem;
import dev.lukamadness.madnesscore.common.registry.component.ModDataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DyeableGeoArmorRenderer;
import software.bernie.geckolib.util.Color;

public class FormalSuitRenderer extends DyeableGeoArmorRenderer<FormalSuitItem> {
    private static final String SUIT_PREFIX = "dyeableSuit";
    private static final String SHIRT_PREFIX = "dyeableShirt";
    private static final String TIE_BONE = "dyeableTie";
    private static final String LEGS_LEFT_BONE = "dyeable3";
    private static final String LEGS_RIGHT_BONE = "dyeable4";

    public FormalSuitRenderer() {
        super(new FormalSuitModel());
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        this.setAllVisible(true);
    }

    @Override
    protected boolean isBoneDyeable(GeoBone bone) {
        return resolveRole(bone.getName()) != null;
    }

    @Override
    protected @NotNull Color getColorForBone(GeoBone bone) {
        if (this.currentStack == null) {
            return Color.WHITE;
        }

        FormalColors colors = this.currentStack.getOrDefault(ModDataComponents.FORMAL_COLORS.get(), FormalColors.DEFAULT);
        Role role = resolveRole(bone.getName());
        if (role == null) {
            return Color.WHITE;
        }

        return switch (role) {
            case SUIT -> new Color(0xFF000000 | colors.suitColor());

            case TIE -> new Color(0xFF000000 | (colors.tieVisible() ? colors.tieColor() : colors.shirtColor()));
            case SHIRT -> new Color(0xFF000000 | colors.shirtColor());
        };
    }

    private enum Role { SUIT, TIE, SHIRT }

    private static Role resolveRole(String boneName) {
        if (boneName.equals(TIE_BONE)) return Role.TIE;
        if (boneName.equals(LEGS_LEFT_BONE) || boneName.equals(LEGS_RIGHT_BONE)) return Role.SUIT;
        if (boneName.startsWith(SHIRT_PREFIX)) return Role.SHIRT;
        if (boneName.startsWith(SUIT_PREFIX)) return Role.SUIT;
        return null;
    }
}
