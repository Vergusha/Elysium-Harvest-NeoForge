package net.vergusha.elysiumharvest.item;

import javax.annotation.Nonnull;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class FloriteAxeItem extends Item {
    public FloriteAxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canPerformAction(@Nonnull ItemStack stack, @Nonnull ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(itemAbility);
    }

    @Override
    public @Nonnull InteractionResult useOn(@Nonnull UseOnContext context) {
        for (ItemAbility ability : ItemAbilities.DEFAULT_AXE_ACTIONS) {
            var currentState = context.getLevel().getBlockState(context.getClickedPos());
            var result = currentState.getToolModifiedState(context, ability, false);
            if (result != null && !result.equals(currentState)) {
                context.getLevel().setBlock(context.getClickedPos(), result, 11);

                if (ability == ItemAbilities.AXE_STRIP) {
                    context.getLevel().playSound(context.getPlayer(), context.getClickedPos(),
                            SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else if (ability == ItemAbilities.AXE_SCRAPE) {
                    context.getLevel().playSound(context.getPlayer(), context.getClickedPos(),
                            SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else if (ability == ItemAbilities.AXE_WAX_OFF) {
                    context.getLevel().playSound(context.getPlayer(), context.getClickedPos(),
                            SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                var player = context.getPlayer();
                if (player != null) {
                    context.getItemInHand().hurtAndBreak(1, player,
                            player.getEquipmentSlotForItem(context.getItemInHand()));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
