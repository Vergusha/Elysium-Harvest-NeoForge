package net.vergusha.elysiumharvest.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.menu.QazanMenu;
import net.vergusha.elysiumharvest.recipe.QazanRecipe;

public class QazanBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int INGREDIENT_SLOTS = 6;
    public static final int RESULT_SLOT = 6;
    public static final int BOWL_SLOT = 7;
    public static final int CONTAINER_SIZE = 8;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int cookingProgress = 0;
    private int cookingTotalTime = 200;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookingProgress;
                case 1 -> cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookingProgress = value;
                case 1 -> cookingTotalTime = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public QazanBlockEntity(BlockPos pos, BlockState state) {
        super(ElysiumHarvest.QAZAN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new QazanMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.elysiumharvest.qazan");
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack itemstack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!itemstack.isEmpty()) {
            this.markUpdated();
        }
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack itemstack = ContainerHelper.takeItem(this.items, slot);
        if (!itemstack.isEmpty()) {
            this.markUpdated();
        }
        return itemstack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.markUpdated();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.markUpdated();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("CookingProgress", this.cookingProgress);
        output.putInt("CookingTotalTime", this.cookingTotalTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.cookingProgress = input.getIntOr("CookingProgress", 0);
        this.cookingTotalTime = input.getIntOr("CookingTotalTime", 200);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QazanBlockEntity blockEntity) {
        if (level == null || level.isClientSide()) {
            return;
        }

        boolean isHeated = isHeatedFromBelow(level, pos);

        if (isHeated && blockEntity.canCook()) {
            blockEntity.cookingProgress++;
            if (blockEntity.cookingProgress >= blockEntity.cookingTotalTime) {
                blockEntity.cookItem();
                blockEntity.cookingProgress = 0;
            }
            blockEntity.markUpdated();
        } else if (blockEntity.cookingProgress > 0) {
            blockEntity.cookingProgress = Math.max(0, blockEntity.cookingProgress - 2);
            blockEntity.markUpdated();
        }
    }

    private static boolean isHeatedFromBelow(Level level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.is(Blocks.CAMPFIRE) || belowState.is(Blocks.SOUL_CAMPFIRE)) {
            return belowState.getValue(BlockStateProperties.LIT);
        }
        return false;
    }

    private boolean canCook() {
        if (this.hasNoIngredients()) {
            return false;
        }

        QazanRecipe recipe = this.getMatchingRecipe();
        if (recipe == null) {
            return false;
        }

        if (!this.hasRequiredContainer(recipe.result())) {
            return false;
        }

        ItemStack resultSlot = this.items.get(RESULT_SLOT);
        ItemStack recipeResult = recipe.result();
        if (resultSlot.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(resultSlot, recipeResult)) {
            return false;
        }
        return resultSlot.getCount() + recipeResult.getCount() <= resultSlot.getMaxStackSize();
    }

    private void cookItem() {
        QazanRecipe recipe = this.getMatchingRecipe();
        if (recipe == null) {
            return;
        }

        ItemStack result = recipe.result().copy();
        ItemStack resultSlot = this.items.get(RESULT_SLOT);
        if (resultSlot.isEmpty()) {
            this.items.set(RESULT_SLOT, result);
        } else {
            resultSlot.grow(result.getCount());
        }

        this.items.get(BOWL_SLOT).shrink(1);

        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            if (!this.items.get(i).isEmpty()) {
                this.items.get(i).shrink(1);
            }
        }
    }

    private boolean hasNoIngredients() {
        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            if (!this.items.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private QazanRecipe getMatchingRecipe() {
        if (this.level == null || this.level.isClientSide()) {
            return null;
        }

        RecipeInput recipeInput = new RecipeInput() {
            @Override
            public ItemStack getItem(int slot) {
                return slot < INGREDIENT_SLOTS ? items.get(slot) : ItemStack.EMPTY;
            }

            @Override
            public int size() {
                return INGREDIENT_SLOTS;
            }
        };

        try {
            return this.level.getServer().getRecipeManager()
                    .getRecipeFor(ElysiumHarvest.QAZAN_RECIPE_TYPE.get(), recipeInput, this.level)
                    .map(holder -> holder.value())
                    .orElse(null);
        } catch (RuntimeException e) {
            ElysiumHarvest.LOGGER.error("Failed to resolve qazan recipe at {}", this.getBlockPos(), e);
            return null;
        }
    }

    public ItemStack extractResult() {
        ItemStack result = this.items.get(RESULT_SLOT).copy();
        this.items.set(RESULT_SLOT, ItemStack.EMPTY);
        this.markUpdated();
        return result;
    }

    public boolean hasResult() {
        return !this.items.get(RESULT_SLOT).isEmpty();
    }

    public static boolean isContainerItemForResult(ItemStack containerStack, ItemStack resultStack) {
        if (containerStack.isEmpty() || resultStack.isEmpty()) {
            return false;
        }

        return resultStack.is(ElysiumHarvest.GINGER_TEA.get())
                ? containerStack.is(Items.GLASS_BOTTLE)
                : containerStack.is(Items.BOWL);
    }

    private boolean hasRequiredContainer(ItemStack resultStack) {
        return isContainerItemForResult(this.items.get(BOWL_SLOT), resultStack);
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
