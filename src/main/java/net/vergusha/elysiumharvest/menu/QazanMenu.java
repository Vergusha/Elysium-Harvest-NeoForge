package net.vergusha.elysiumharvest.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.blockentity.QazanBlockEntity;

public class QazanMenu extends AbstractContainerMenu {
    private static final int INGREDIENT_SLOT_COUNT = 6;
    private static final int RESULT_SLOT_INDEX = QazanBlockEntity.RESULT_SLOT;
    private static final int CONTAINER_SLOT_INDEX = QazanBlockEntity.BOWL_SLOT;
    private static final int PLAYER_INVENTORY_START = 8;
    private static final int PLAYER_INVENTORY_END = 44;

    private final Container container;
    private final ContainerData data;

    public QazanMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(QazanBlockEntity.CONTAINER_SIZE),
                new SimpleContainerData(2));
    }

    public QazanMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ElysiumHarvest.QAZAN_MENU.get(), containerId);
        checkContainerSize(container, QazanBlockEntity.CONTAINER_SIZE);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new IngredientSlot(container, col + row * 3, 40 + col * 18, 17 + row * 18));
            }
        }

        this.addSlot(new ResultSlot(container, RESULT_SLOT_INDEX, 116, 26));
        this.addSlot(new ContainerSlot(container, CONTAINER_SLOT_INDEX, 116, 52));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getItem();
        movedStack = slotStack.copy();

        if (index == RESULT_SLOT_INDEX || index == CONTAINER_SLOT_INDEX) {
            if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(slotStack, movedStack);
        } else if (index >= PLAYER_INVENTORY_START) {
            if (this.isContainerItem(slotStack)) {
                if (!this.moveItemStackTo(slotStack, CONTAINER_SLOT_INDEX, CONTAINER_SLOT_INDEX + 1, false)) {
                    if (!this.moveItemStackTo(slotStack, 0, INGREDIENT_SLOT_COUNT, false)) {
                        if (index < 35) {
                            if (!this.moveItemStackTo(slotStack, 35, PLAYER_INVENTORY_END, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, 35, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else if (!this.moveItemStackTo(slotStack, 0, INGREDIENT_SLOT_COUNT, false)) {
                if (index < 35) {
                    if (!this.moveItemStackTo(slotStack, 35, PLAYER_INVENTORY_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, 35, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == movedStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotStack);
        return movedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public int getCookingProgress() {
        return this.data.get(0);
    }

    public int getCookingTotalTime() {
        return this.data.get(1);
    }

    public int getScaledProgress() {
        int progress = this.getCookingProgress();
        int total = this.getCookingTotalTime();
        return total != 0 && progress != 0 ? progress * 24 / total : 0;
    }

    private boolean isContainerItem(ItemStack stack) {
        return stack.is(Items.BOWL) || stack.is(Items.GLASS_BOTTLE);
    }

    private static class ResultSlot extends Slot {
        public ResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private static class IngredientSlot extends Slot {
        public IngredientSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !stack.is(Items.BOWL) && !stack.is(Items.GLASS_BOTTLE);
        }
    }

    private static class ContainerSlot extends Slot {
        public ContainerSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.BOWL) || stack.is(Items.GLASS_BOTTLE);
        }
    }
}
