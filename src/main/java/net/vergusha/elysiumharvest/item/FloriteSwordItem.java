package net.vergusha.elysiumharvest.item;

import javax.annotation.Nonnull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class FloriteSwordItem extends Item {
    public FloriteSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        return super.getDestroySpeed(stack, state);
    }
}
