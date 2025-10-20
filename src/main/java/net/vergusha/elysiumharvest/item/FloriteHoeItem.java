package net.vergusha.elysiumharvest.item;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class FloriteHoeItem extends Item {
    public FloriteHoeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canPerformAction(@Nonnull ItemStack stack, @Nonnull ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility);
    }

    @Override
    public @Nonnull InteractionResult useOn(@Nonnull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        var player = context.getPlayer();

        boolean tilledAny = false;
        int durabilityLoss = 0;

        // Вспахиваем область 3х3 вокруг нажатого блока
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos targetPos = clickedPos.offset(x, 0, z);
                BlockState targetState = level.getBlockState(targetPos);

                // Проверяем, можно ли вспахать этот блок
                if (canTillBlock(targetState)) {
                    // Проверяем, что над блоком есть воздух
                    BlockState aboveState = level.getBlockState(targetPos.above());
                    if (aboveState.isAir()) {
                        // Вспахиваем блок
                        level.setBlock(targetPos, Blocks.FARMLAND.defaultBlockState(), 11);

                        // Воспроизводим звук
                        level.playSound(player, targetPos, SoundEvents.HOE_TILL,
                                SoundSource.BLOCKS, 1.0F, 1.0F);

                        tilledAny = true;
                        durabilityLoss++;
                    }
                }
            }
        }

        // Применяем износ инструмента
        if (tilledAny && player != null) {
            context.getItemInHand().hurtAndBreak(durabilityLoss, player,
                    player.getEquipmentSlotForItem(context.getItemInHand()));
        }

        return tilledAny ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Проверяет, можно ли вспахать данный блок
     */
    private boolean canTillBlock(BlockState state) {
        return state.is(Blocks.DIRT) ||
                state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.DIRT_PATH) ||
                state.is(Blocks.COARSE_DIRT);
    }
}
