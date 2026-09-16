package dev.lukamadness.madnesscore.common.content.tailoring.clothing;

import dev.lukamadness.madnesscore.common.registry.component.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FormalSuitItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FormalSuitItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.CHESTPLATE, properties);
    }

    private static FormalColors colorsOf(ItemStack stack) {
        FormalColors colors = stack.getOrDefault(ModDataComponents.FORMAL_COLORS.get(), FormalColors.DEFAULT);
        return colors;
    }

    public static int getSuitColor(ItemStack stack) {
        return colorsOf(stack).suitColor();
    }

    public static int getTieColor(ItemStack stack) {
        return colorsOf(stack).tieColor();
    }

    public static int getShirtColor(ItemStack stack) {
        return colorsOf(stack).shirtColor();
    }

    public static boolean isTieVisible(ItemStack stack) {
        return colorsOf(stack).tieVisible();
    }

    public static FormalColors getColors(ItemStack stack) {
        return colorsOf(stack);
    }

    public static void setColors(ItemStack stack, FormalColors colors) {
        stack.set(ModDataComponents.FORMAL_COLORS.get(), colors);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
