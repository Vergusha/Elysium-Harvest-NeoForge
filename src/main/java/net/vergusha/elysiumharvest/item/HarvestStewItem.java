package net.vergusha.elysiumharvest.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class HarvestStewItem extends Item {
    public HarvestStewItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            ItemStack bowl = new ItemStack(Items.BOWL);
            if (resultStack.isEmpty()) {
                return bowl;
            }
            if (!player.getInventory().add(bowl)) {
                player.drop(bowl, false);
            }
        } else if (resultStack.isEmpty()) {
            return new ItemStack(Items.BOWL);
        }

        return resultStack;
    }
}
