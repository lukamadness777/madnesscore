package dev.lukamadness.madnesscore.common.content.tailoring;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukamadness.madnesscore.common.content.tailoring.recipe.TailoringRecipe;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record TailoringIngredient(boolean optionalColor, Ingredient ingredient, int count) {
    public static final TagKey<Item> COLORABLE_FABRICS_TAG =
            TagKey.create(Registries.ITEM, RegistryHelper.id("tailoring_inputs"));

    public static TailoringIngredient mandatory(Ingredient ingredient, int count) {
        return new TailoringIngredient(false, ingredient, count);
    }

    public static TailoringIngredient optionalColor(int count) {
        return new TailoringIngredient(true, Ingredient.of(COLORABLE_FABRICS_TAG), count);
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && ingredient.test(stack) && stack.getCount() >= count;
    }

    private static TailoringIngredient fromJson(boolean optionalColor, Ingredient ingredient, int count) {
        return optionalColor ? optionalColor(count) : mandatory(ingredient, count);
    }

    public static final Codec<TailoringIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("optional_color", false).forGetter(TailoringIngredient::optionalColor),
            Ingredient.CODEC.optionalFieldOf("ingredient", Ingredient.EMPTY).forGetter(TailoringIngredient::ingredient),
            Codec.INT.optionalFieldOf("count", 1).forGetter(TailoringIngredient::count)
    ).apply(instance, TailoringIngredient::fromJson));

    public static final StreamCodec<RegistryFriendlyByteBuf, TailoringIngredient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TailoringIngredient::optionalColor,
            Ingredient.CONTENTS_STREAM_CODEC, TailoringIngredient::ingredient,
            ByteBufCodecs.VAR_INT, TailoringIngredient::count,
            TailoringIngredient::new
    );
}
