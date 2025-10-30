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
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.blockentity.QazanBlockEntity;

public class QazanMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    // Для клиента
    public QazanMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(QazanBlockEntity.CONTAINER_SIZE), new SimpleContainerData(2));
    }

    // Для сервера
    public QazanMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ElysiumHarvest.QAZAN_MENU.get(), containerId);
        checkContainerSize(container, QazanBlockEntity.CONTAINER_SIZE);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        // Слоты для ингредиентов (6 слотов в 2 ряда по 3)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(container, col + row * 3, 44 + col * 18, 17 + row * 18));
            }
        }

        // Слот для результата
        this.addSlot(new ResultSlot(container, QazanBlockEntity.RESULT_SLOT, 116, 26));

        // Инвентарь игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Хотбар игрока
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();
            
            if (index == QazanBlockEntity.RESULT_SLOT) {
                // Результат -> инвентарь игрока
                if (!this.moveItemStackTo(slotItem, 7, 43, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotItem, itemstack);
            } else if (index >= 7) {
                // Инвентарь игрока -> слоты ингредиентов
                if (!this.moveItemStackTo(slotItem, 0, 6, false)) {
                    if (index < 34) {
                        if (!this.moveItemStackTo(slotItem, 34, 43, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotItem, 7, 34, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(slotItem, 7, 43, false)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotItem.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotItem);
        }

        return itemstack;
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

    // Специальный слот только для результата (нельзя положить предмет)
    private static class ResultSlot extends Slot {
        public ResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
