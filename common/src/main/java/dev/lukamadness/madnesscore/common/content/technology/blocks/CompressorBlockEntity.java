
package dev.lukamadness.madnesscore.common.content.technology.blocks;

import dev.lukamadness.madnesscore.common.content.technology.energy.EnergyReceiver;
import dev.lukamadness.madnesscore.common.content.technology.energy.ModEnergyStorage;
import dev.lukamadness.madnesscore.common.content.technology.recipe.CompressingRecipe;
import dev.lukamadness.madnesscore.common.content.technology.screen.CompressorScreenHandler;
import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CompressorBlockEntity extends BlockEntity implements Container, MenuProvider, EnergyReceiver {
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    private static final int TICKS_PER_SECOND = 20;
    public static final int ENERGY_CAPACITY = 10_000;
    public static final int MAX_RECEIVE_PER_SECOND = 100;
    public static final int MAX_RECEIVE = MAX_RECEIVE_PER_SECOND / TICKS_PER_SECOND;

    private final ModEnergyStorage energyStorage = new ModEnergyStorage(ENERGY_CAPACITY, MAX_RECEIVE, 0, this::setChanged);

    private int progress;
    private int maxProgress = 200;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergy();
                case 1 -> energyStorage.getCapacity();
                case 2 -> progress;
                case 3 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergy(value);
                case 2 -> progress = value;
                case 3 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() { return 4; }
    };

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPRESSOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CompressorBlockEntity entity) {
        if (level.isClientSide) return;

        SingleRecipeInput input = new SingleRecipeInput(entity.items.get(0));
        Optional<RecipeHolder<CompressingRecipe>> match = level.getRecipeManager()
                .getRecipeFor(ModRecipes.COMPRESSING.get(), input, level);

        boolean dirty = false;

        if (match.isPresent() && !entity.items.get(0).isEmpty()) {
            CompressingRecipe recipe = match.get().value();

            boolean canOutput = canInsertOutput(entity, recipe.getOutput());
            boolean hasEnergy = entity.energyStorage.getEnergy() >= recipe.getEnergyPerTick();

            if (canOutput && hasEnergy) {
                entity.energyStorage.extract(recipe.getEnergyPerTick(), false);
                entity.maxProgress = recipe.getProcessTime();
                entity.progress++;
                dirty = true;

                if (entity.progress >= entity.maxProgress) {
                    entity.craft(recipe);
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

        boolean wasLit = state.getValue(CompressorBlock.LIT);
        boolean isLit = entity.progress > 0;
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(CompressorBlock.LIT, isLit), 3);
        }

        if (dirty) entity.setChanged();
    }

    private static boolean canInsertOutput(CompressorBlockEntity entity, ItemStack output) {
        ItemStack current = entity.items.get(1);
        if (current.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(current, output)
                && current.getCount() + output.getCount() <= current.getMaxStackSize();
    }

    private void craft(CompressingRecipe recipe) {
        items.get(0).shrink(1);
        ItemStack output = recipe.getOutput().copy();
        ItemStack current = items.get(1);
        if (current.isEmpty()) {
            items.set(1, output);
        } else {
            current.grow(output.getCount());
        }
    }

    @Override
    public ModEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        energyStorage.writeNbt(tag, "Energy");
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        energyStorage.readNbt(tag, "Energy");
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
    }

    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return getBlockPos();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new CompressorScreenHandler(syncId, inv, this, containerData);
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.get(0).isEmpty() && items.get(1).isEmpty(); }
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
