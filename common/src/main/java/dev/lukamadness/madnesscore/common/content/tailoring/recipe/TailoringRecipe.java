package dev.lukamadness.madnesscore.common.content.tailoring.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukamadness.madnesscore.common.content.tailoring.FabricColors;
import dev.lukamadness.madnesscore.common.content.tailoring.TailoringColors;
import dev.lukamadness.madnesscore.common.content.tailoring.TailoringIngredient;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalColors;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import dev.lukamadness.madnesscore.common.registry.component.ModDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TailoringRecipe implements Recipe<TailoringRecipeInput> {
    public static final int MAX_INPUTS = TailoringRecipeInput.SLOT_COUNT;

    private final List<TailoringIngredient> requirements;
    private final ItemStack output;

    public TailoringRecipe(List<TailoringIngredient> requirements, ItemStack output) {
        if (requirements.size() > MAX_INPUTS) {
            throw new IllegalArgumentException("Máximo " + MAX_INPUTS + " requisitos");
        }
        this.requirements = requirements;
        this.output = output;
    }

    public record SlotUse(int slot, int count) {
    }

    public record Candidate(TailoringRecipe recipe, ItemStack result, List<SlotUse> consumption) {
    }

    public List<Candidate> computeCandidates(TailoringRecipeInput input) {
        List<TailoringIngredient> mandatory = new ArrayList<>();
        List<TailoringIngredient> optional = new ArrayList<>();
        for (TailoringIngredient req : requirements) {
            (req.optionalColor() ? optional : mandatory).add(req);
        }

        boolean[] usedSlots = new boolean[input.size()];
        int[] mandatoryAssignment = new int[mandatory.size()];
        if (!tryAssign(0, mandatory, input, usedSlots, mandatoryAssignment)) {
            return List.of();
        }

        List<SlotUse> mandatoryUse = new ArrayList<>();
        for (int i = 0; i < mandatory.size(); i++) {
            mandatoryUse.add(new SlotUse(mandatoryAssignment[i], mandatory.get(i).count()));
        }

        if (optional.isEmpty()) {
            return List.of(new Candidate(this, output.copy(), mandatoryUse));
        }

        Ingredient colorFilter = optional.get(0).ingredient();
        record AvailableColor(int rgb, int slot, int count) {
        }
        List<AvailableColor> available = new ArrayList<>();
        Set<Integer> seenColors = new LinkedHashSet<>();
        for (int slot = 0; slot < input.size(); slot++) {
            if (usedSlots[slot]) continue;
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty() || !colorFilter.test(stack)) continue;
            var colorOpt = FabricColors.getColor(stack);
            if (colorOpt.isEmpty()) continue;
            int rgb = colorOpt.get();
            if (seenColors.add(rgb)) {
                available.add(new AvailableColor(rgb, slot, 1));
            }
        }

        if (available.isEmpty()) return List.of();

        List<Candidate> candidates = new ArrayList<>();

        if (optional.size() == 1) {
            for (var color : available) {
                ItemStack result = output.copy();
                applyColors(result, List.of(color.rgb()));
                List<SlotUse> use = new ArrayList<>(mandatoryUse);
                use.add(new SlotUse(color.slot(), optional.get(0).count()));
                candidates.add(new Candidate(this, result, use));
            }
            return candidates;
        }

        List<SlotUse> allOptionalUse = new ArrayList<>(mandatoryUse);
        for (var color : available) {
            allOptionalUse.add(new SlotUse(color.slot(), 1));
        }

        int[] indices = new int[optional.size()];
        generateCombinations(available.size(), indices, 0, combo -> {
            List<Integer> colors = new ArrayList<>(combo.length);
            for (int idx : combo) colors.add(available.get(idx).rgb());
            ItemStack result = output.copy();
            applyColors(result, colors);
            candidates.add(new Candidate(this, result, allOptionalUse));
        });

        return candidates;
    }

    private interface ComboConsumer {
        void accept(int[] combo);
    }

    private static void generateCombinations(int optionCount, int[] indices, int pos, ComboConsumer consumer) {
        if (pos >= indices.length) {
            consumer.accept(indices.clone());
            return;
        }
        for (int i = 0; i < optionCount; i++) {
            indices[pos] = i;
            generateCombinations(optionCount, indices, pos + 1, consumer);
        }
    }

    private static void applyColors(ItemStack result, List<Integer> colors) {
        result.set(ModDataComponents.TAILORING_COLORS.get(), new TailoringColors(colors));
        result.set(DataComponents.DYED_COLOR, new DyedItemColor(colors.get(0), false));

        if (result.getItem() == ModItems.FORMAL_SUIT.get() && colors.size() >= 3) {
            result.set(ModDataComponents.FORMAL_COLORS.get(),
                    new FormalColors(colors.get(0), colors.get(1), colors.get(2), true));
        }
    }

    private boolean tryAssign(int reqIndex, List<TailoringIngredient> reqs, TailoringRecipeInput input,
                               boolean[] usedSlots, int[] assignment) {
        if (reqIndex >= reqs.size()) return true;

        TailoringIngredient req = reqs.get(reqIndex);
        for (int slot = 0; slot < input.size(); slot++) {
            if (usedSlots[slot]) continue;
            ItemStack stack = input.getItem(slot);
            if (req.matches(stack)) {
                usedSlots[slot] = true;
                assignment[reqIndex] = slot;
                if (tryAssign(reqIndex + 1, reqs, input, usedSlots, assignment)) {
                    return true;
                }
                usedSlots[slot] = false;
            }
        }
        return false;
    }

    @Override
    public boolean matches(TailoringRecipeInput input, Level level) {
        return !computeCandidates(input).isEmpty();
    }

    @Override
    public ItemStack assemble(TailoringRecipeInput input, HolderLookup.Provider registries) {
        List<Candidate> candidates = computeCandidates(input);
        return candidates.isEmpty() ? ItemStack.EMPTY : candidates.get(0).result();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output;
    }

    public List<TailoringIngredient> getRequirements() {
        return requirements;
    }

    public ItemStack getOutput() {
        return output;
    }

    @Override
    public RecipeSerializer<? extends Recipe<TailoringRecipeInput>> getSerializer() {
        return ModRecipes.TAILORING_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<TailoringRecipeInput>> getType() {
        return ModRecipes.TAILORING.get();
    }

    public static class Serializer implements RecipeSerializer<TailoringRecipe> {
        public static final MapCodec<TailoringRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TailoringIngredient.CODEC.listOf(1, MAX_INPUTS).fieldOf("ingredients").forGetter(TailoringRecipe::getRequirements),
                ItemStack.CODEC.fieldOf("output").forGetter(TailoringRecipe::getOutput)
        ).apply(instance, TailoringRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, TailoringRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(ArrayList::new, TailoringIngredient.STREAM_CODEC), TailoringRecipe::getRequirements,
                ItemStack.STREAM_CODEC, TailoringRecipe::getOutput,
                TailoringRecipe::new
        );

        @Override
        public MapCodec<TailoringRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TailoringRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
