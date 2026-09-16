package dev.lukamadness.madnesscore.common.content.tailoring.screen;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.tailoring.network.TailoringCandidatesPacket;
import dev.lukamadness.madnesscore.common.content.tailoring.recipe.TailoringRecipe;
import dev.lukamadness.madnesscore.common.content.tailoring.recipe.TailoringRecipeInput;
import dev.lukamadness.madnesscore.common.platform.Services;
import dev.lukamadness.madnesscore.common.registry.menu.ModMenus;
import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class TailoringTableScreenHandler extends AbstractContainerMenu {
    public static final int INPUT_SLOTS = TailoringRecipeInput.SLOT_COUNT;

    private final Container container;
    private final ContainerLevelAccess access;
    private final DataSlot selectedRecipeIndex = DataSlot.standalone();
    private final Level level;
    private final Player player;
    private final ResultContainer resultContainer = new ResultContainer();
    private final Slot resultSlot;

    private List<TailoringRecipe.Candidate> candidates = List.of();
    private Runnable slotUpdateListener = () -> {};

    public TailoringTableScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(INPUT_SLOTS));
    }

    public TailoringTableScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, resolveInventory(playerInventory, pos));
    }

    private static Container resolveInventory(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof Container inv) return inv;
        return new SimpleContainer(INPUT_SLOTS);
    }

    public TailoringTableScreenHandler(int syncId, Inventory playerInventory, Container container) {
        super(ModMenus.TAILORING_TABLE.get(), syncId);
        checkContainerSize(container, INPUT_SLOTS);
        this.container = container;
        this.level = playerInventory.player.level();
        this.player = playerInventory.player;
        this.access = ContainerLevelAccess.create(level, playerInventory.player.blockPosition());

        container.startOpen(playerInventory.player);

        int[][] inputPos = { {14, 26}, {32, 26}, {14, 44}, {32, 44} };
        for (int i = 0; i < INPUT_SLOTS; i++) {
            this.addSlot(new Slot(container, i, inputPos[i][0], inputPos[i][1]) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    TailoringTableScreenHandler.this.slotsChanged(this.container);
                }
            });
        }

        this.resultSlot = this.addSlot(new Slot(resultContainer, 0, 145, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                onResultTake();
                super.onTake(player, stack);
            }
        });

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));

        this.addDataSlot(selectedRecipeIndex);
    }

    private boolean initialCandidatesComputed = false;

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (!initialCandidatesComputed && !level.isClientSide()) {
            initialCandidatesComputed = true;
            slotsChanged(container);
        }
    }

    public void registerUpdateListener(Runnable listener) {
        this.slotUpdateListener = listener;
    }

    private TailoringRecipeInput currentInput() {
        List<ItemStack> stacks = new ArrayList<>(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) stacks.add(container.getItem(i));
        return new TailoringRecipeInput(stacks);
    }

    @Override
    public void slotsChanged(Container container) {
        if (!level.isClientSide()) {
            TailoringRecipeInput input = currentInput();

            List<RecipeHolder<TailoringRecipe>> matchingRecipes =
                    level.getRecipeManager().getRecipesFor(ModRecipes.TAILORING.get(), input, level);

            List<TailoringRecipe.Candidate> newCandidates = new ArrayList<>();
            for (RecipeHolder<TailoringRecipe> holder : matchingRecipes) {
                newCandidates.addAll(holder.value().computeCandidates(input));
            }

            if (!newCandidates.equals(this.candidates)) {
                this.candidates = newCandidates;

                if (player instanceof ServerPlayer serverPlayer) {
                    List<ItemStack> results = new ArrayList<>(candidates.size());
                    for (TailoringRecipe.Candidate candidate : candidates) results.add(candidate.result());
                    Services.TAILORING_NETWORK.sendToPlayer(serverPlayer,
                            new TailoringCandidatesPacket(containerId, results));
                }
            }

            int selected = selectedRecipeIndex.get();
            if (candidates.isEmpty()) {
                selected = -1;
            } else if (selected < 0 || selected >= candidates.size()) {
                selected = candidates.size() == 1 ? 0 : -1;
            }
            selectedRecipeIndex.set(selected);
        }

        updateResultSlot();
        slotUpdateListener.run();
    }

    public void setCandidatesFromNetwork(List<ItemStack> results) {
        List<TailoringRecipe.Candidate> newCandidates = new ArrayList<>(results.size());
        for (ItemStack result : results) {
            newCandidates.add(new TailoringRecipe.Candidate(null, result, List.of()));
        }
        this.candidates = newCandidates;
        updateResultSlot();
        slotUpdateListener.run();
    }

    private void updateResultSlot() {
        int selected = selectedRecipeIndex.get();
        if (selected >= 0 && selected < candidates.size()) {
            resultContainer.setItem(0, candidates.get(selected).result().copy());
        } else {
            resultContainer.setItem(0, ItemStack.EMPTY);
        }
        resultContainer.setChanged();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < candidates.size()) {
            selectedRecipeIndex.set(id);
            updateResultSlot();
            return true;
        }

        MadnessCoreCommon.LOG.warn(
                "[tailoring] click rechazado: id={} pero candidates.size()={} (jugador={})",
                id, candidates.size(), player.getName().getString());
        return false;
    }

    private void onResultTake() {
        int selected = selectedRecipeIndex.get();
        if (selected < 0 || selected >= candidates.size()) return;

        TailoringRecipe.Candidate candidate = candidates.get(selected);
        for (TailoringRecipe.SlotUse use : candidate.consumption()) {
            container.removeItem(use.slot(), use.count());
        }
        container.setChanged();
    }

    public int getSelectedRecipeIndex() {
        return selectedRecipeIndex.get();
    }

    public List<TailoringRecipe.Candidate> getCandidates() {
        return candidates;
    }

    public int getNumRecipes() {
        return candidates.size();
    }

    public boolean hasInput() {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (!container.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
        // The result slot only ever holds a *preview* of a candidate's output (see updateResultSlot()).
        // It is never actually granted to the player until they explicitly take it from the slot,
        // which is when onResultTake() consumes the real ingredients from `container`.
        // Previously this called clearContainer(player, resultContainer), which hands the preview
        // stack to the player (or drops it) on close even though nothing was ever consumed -
        // duplicating the output item for free. Just discard the preview instead.
        if (!level.isClientSide()) {
            resultContainer.setItem(0, ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            newStack = original.copy();
            if (slotIndex == resultSlot.index) {
                if (!moveItemStackTo(original, INPUT_SLOTS + 1, INPUT_SLOTS + 1 + 36, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(original, newStack);
            } else if (slotIndex < INPUT_SLOTS) {
                if (!moveItemStackTo(original, INPUT_SLOTS + 1, INPUT_SLOTS + 1 + 36, true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(original, 0, INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
