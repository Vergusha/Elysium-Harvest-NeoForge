package net.vergusha.elysiumharvest.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Harvest Stew - пищевой предмет, похожий на mushroom_stew
 */
public class HarvestStewItem extends Item {
    public HarvestStewItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }
}
