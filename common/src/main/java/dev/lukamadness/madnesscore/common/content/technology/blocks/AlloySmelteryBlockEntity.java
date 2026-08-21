// common/tecnology/blocks/AlloySmelteryBlockEntity.java
package dev.lukamadness.madnesscore.common.content.technology.blocks;

import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatEnvironment;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatReceiver;
import dev.lukamadness.madnesscore.common.content.technology.heat.ModHeatStorage;
import dev.lukamadness.madnesscore.common.content.technology.recipe.AlloySmeltingRecipe;
import dev.lukamadness.madnesscore.common.content.technology.recipe.input.AlloySmelteryRecipeInput;
import dev.lukamadness.madnesscore.common.content.technology.screen.AlloySmelteryScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Recreación de IndustrialSmelterBlockEntity, renombrado a Alloy Smeltery. 4 inputs,
 * 4 outputs, pero corre a HEAT directo (HeatReceiver) — no a Energy convertida. Además
 * exige una temperatura mínima ({@link #MIN_TEMPERATURE}, 800°C) para poder procesar:
 * aunque tenga Heat de sobra almacenado, si el sistema todavía no llegó a esa
 * temperatura no funciona (ver HeatReceiver.getMinTemperature() / HeatGeneratorBlockEntity,
 * que es quien sube la temperatura de este bloque mientras le empuja Heat).
 */
public class AlloySmelteryBlockEntity extends BlockEntity implements Container, MenuProvider, HeatReceiver {

    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOTS = 4;
    public static final int TOTAL_SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;

    public static final int HEAT_CAPACITY = 15_000;
    public static final int MAX_HEAT_RECEIVE = 8; // Heat/tick, mismo tope que empuja HeatGeneratorBlockEntity

    /** Temperatura mínima (°C) para poder procesar, tal como se definió en el diseño. */
    public static final double MIN_TEMPERATURE = 800.0;

    // slots 0-3 = input, slots 4-7 = output
    private NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private final ModHeatStorage heatStorage = new ModHeatStorage(HEAT_CAPACITY, MAX_HEAT_RECEIVE, 0, this::setChanged);

    private int progress;
    private int maxProgress = 200;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> heatStorage.getHeat();
                case 1 -> heatStorage.getCapacity();
                case 2 -> progress;
                case 3 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> heatStorage.setHeat(value);
                case 2 -> progress = value;
                case 3 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() { return 4; }
    };

    public AlloySmelteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALLOY_SMELTERY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AlloySmelteryBlockEntity entity) {
        if (level.isClientSide) return;

        boolean wasLit = state.getValue(AlloySmelteryBlock.LIT);
        boolean wasPowered = state.getValue(AlloySmelteryBlock.POWERED);
        boolean dirty = false;

        List<ItemStack> inputStacks = new ArrayList<>(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) inputStacks.add(entity.items.get(i));
        AlloySmelteryRecipeInput recipeInput = new AlloySmelteryRecipeInput(inputStacks);

        Optional<RecipeHolder<AlloySmeltingRecipe>> match = level.getRecipeManager()
                .getRecipeFor(ModRecipes.ALLOY_SMELTING.get(), recipeInput, level);

        if (match.isPresent()) {
            AlloySmeltingRecipe recipe = match.get().value();
            int[] assignment = recipe.findAssignment(recipeInput);

            boolean canOutput = canInsertAllOutputs(entity, recipe.getOutputs());
            // Necesita Heat de sobra Y que el sistema ya haya llegado a MIN_TEMPERATURE —
            // tener Heat almacenado no alcanza si todavía no está lo bastante caliente.
            boolean hasHeat = entity.heatStorage.getHeat() >= recipe.getHeatPerTick();
            boolean hotEnough = entity.isHotEnough();

            if (assignment != null && canOutput && hasHeat && hotEnough) {
                entity.heatStorage.drain(recipe.getHeatPerTick());
                entity.maxProgress = recipe.getProcessTime();
                entity.progress++;
                dirty = true;

                if (entity.progress >= entity.maxProgress) {
                    entity.craft(recipe, assignment);
                    entity.progress = 0;
                }
            } else if (entity.progress > 0) {
                entity.progress = Math.max(0, entity.progress - 2);
                dirty = true;
            }
        } else if (entity.progress != 0) {
            entity.progress = 0;
            dirty = true;
        }

        // Pérdida ambiental de temperatura, mismo ritmo base que el Heat Generator (ver
        // HeatEnvironment) — sin esto, una vez caliente se quedaría a MIN_TEMPERATURE para
        // siempre aunque el Heat Generator se apague o se desconecte.
        double lossMultiplier = HeatEnvironment.lossMultiplier(level, pos);
        entity.heatStorage.coolTowards(ModHeatStorage.AMBIENT_TEMPERATURE,
                HeatEnvironment.BASE_TEMPERATURE_LOSS_PER_TICK * lossMultiplier);

        boolean isLit = entity.progress > 0;
        boolean isPowered = entity.isHotEnough();

        if (wasLit != isLit || wasPowered != isPowered) {
            level.setBlock(pos, state.setValue(AlloySmelteryBlock.LIT, isLit).setValue(AlloySmelteryBlock.POWERED, isPowered), 3);
        }

        if (dirty) entity.setChanged();
    }

    private static boolean canInsertAllOutputs(AlloySmelteryBlockEntity entity, List<ItemStack> outputs) {
        List<ItemStack> simulated = new ArrayList<>(OUTPUT_SLOTS);
        for (int i = 0; i < OUTPUT_SLOTS; i++) simulated.add(entity.items.get(INPUT_SLOTS + i).copy());

        for (ItemStack output : outputs) {
            if (!tryInsert(simulated, output, true)) return false;
        }
        return true;
    }

    private static boolean tryInsert(List<ItemStack> outputSlots, ItemStack toInsert, boolean simulateOnly) {
        for (int i = 0; i < outputSlots.size(); i++) {
            ItemStack current = outputSlots.get(i);
            if (!current.isEmpty()
                    && ItemStack.isSameItemSameComponents(current, toInsert)
                    && current.getCount() + toInsert.getCount() <= current.getMaxStackSize()) {
                if (!simulateOnly) current.grow(toInsert.getCount());
                return true;
            }
        }
        for (int i = 0; i < outputSlots.size(); i++) {
            if (outputSlots.get(i).isEmpty()) {
                if (!simulateOnly) outputSlots.set(i, toInsert.copy());
                return true;
            }
        }
        return false;
    }

    private void craft(AlloySmeltingRecipe recipe, int[] assignment) {
        List<AlloySmeltingRecipe.IngredientEntry> inputs = recipe.getInputs();
        for (int i = 0; i < inputs.size(); i++) {
            int slot = assignment[i];
            items.get(slot).shrink(inputs.get(i).count());
        }

        List<ItemStack> outputSlotsView = items.subList(INPUT_SLOTS, TOTAL_SLOTS);
        for (ItemStack output : recipe.getOutputs()) {
            tryInsert(outputSlotsView, output, false);
        }
    }

    @Override
    public ModHeatStorage getHeatStorage() {
        return heatStorage;
    }

    @Override
    public double getMinTemperature() {
        return MIN_TEMPERATURE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        heatStorage.writeNbt(tag, "Heat");
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        heatStorage.readNbt(tag, "Heat");
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
    }

    public BlockPos getScreenOpeningData() { return getBlockPos(); }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new AlloySmelteryScreenHandler(syncId, inv, this, containerData);
    }

    // --- Container ---
    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack r = ContainerHelper.removeItem(items, slot, amount);
        if (!r.isEmpty()) setChanged();
        return r;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(getBlockPos()) == this
                && player.distanceToSqr(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override public void clearContent() { items.clear(); }
}