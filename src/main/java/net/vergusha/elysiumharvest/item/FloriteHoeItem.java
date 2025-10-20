package net.vergusha.elysiumharvest.item;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

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

        boolean performedAction = false;
        int durabilityLoss = 0;

        // Обрабатываем область 3х3 вокруг нажатого блока
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos targetPos = clickedPos.offset(x, 0, z);
                BlockState targetState = level.getBlockState(targetPos);

                // Сначала пробуем собрать урожай
                if (tryHarvestCrop(level, targetPos, targetState, player)) {
                    performedAction = true;
                    durabilityLoss++;
                }
                // Если не удалось собрать урожай, пробуем вспахать
                else if (canTillBlock(targetState)) {
                    // Проверяем, что над блоком есть воздух
                    BlockState aboveState = level.getBlockState(targetPos.above());
                    if (aboveState.isAir()) {
                        // Вспахиваем блок
                        level.setBlock(targetPos, Blocks.FARMLAND.defaultBlockState(), 11);

                        // Воспроизводим звук
                        level.playSound(player, targetPos, SoundEvents.HOE_TILL,
                                SoundSource.BLOCKS, 1.0F, 1.0F);

                        performedAction = true;
                        durabilityLoss++;
                    }
                }
            }
        }

        // Применяем износ инструмента
        if (performedAction && player != null) {
            context.getItemInHand().hurtAndBreak(durabilityLoss, player,
                    player.getEquipmentSlotForItem(context.getItemInHand()));
        }

        return performedAction ? InteractionResult.SUCCESS : InteractionResult.PASS;
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

    /**
     * Пытается собрать урожай и автоматически пересадить его
     * 
     * @return true если урожай был собран
     */
    private boolean tryHarvestCrop(Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.player.Player player) {
        Block block = state.getBlock();

        // Проверяем, является ли блок культурой
        if (block instanceof CropBlock cropBlock) {
            // Проверяем, полностью ли выросла культура
            if (cropBlock.isMaxAge(state)) {
                if (level instanceof ServerLevel serverLevel) {
                    // Получаем дроп с культуры
                    LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.BLOCK_STATE, state)
                            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);

                    if (player != null) {
                        lootBuilder.withParameter(LootContextParams.THIS_ENTITY, player);
                    }

                    List<ItemStack> drops = state.getDrops(lootBuilder);

                    boolean foundSeeds = false;

                    // Выдаём дроп игроку
                    for (ItemStack drop : drops) {
                        if (!foundSeeds && areSeedsForCrop(drop.getItem(), block)) {
                            // Это семена - уменьшаем количество на 1 для пересадки
                            if (drop.getCount() > 1) {
                                drop.shrink(1);
                                if (player != null) {
                                    player.addItem(drop);
                                } else {
                                    spawnItemInWorld(level, pos, drop);
                                }
                            }
                            foundSeeds = true;
                        } else {
                            // Выдаём остальной дроп
                            if (player != null) {
                                player.addItem(drop);
                            } else {
                                spawnItemInWorld(level, pos, drop);
                            }
                        }
                    }

                    // Воспроизводим звук сбора урожая
                    level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

                    // Пересаживаем культуру (возвращаем в начальное состояние)
                    level.setBlock(pos, cropBlock.getStateForAge(0), 11);
                }
                return true;
            }
        }

        // Также обрабатываем другие типы культур
        if (isHarvestableCrop(state)) {
            if (level instanceof ServerLevel serverLevel) {
                // Получаем дроп
                LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.BLOCK_STATE, state)
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);

                if (player != null) {
                    lootBuilder.withParameter(LootContextParams.THIS_ENTITY, player);
                }

                List<ItemStack> drops = state.getDrops(lootBuilder);

                // Выдаём дроп игроку
                for (ItemStack drop : drops) {
                    if (player != null) {
                        player.addItem(drop);
                    } else {
                        spawnItemInWorld(level, pos, drop);
                    }
                }

                // Воспроизводим звук
                level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

                // Пересаживаем
                level.setBlock(pos, getResetStateForCrop(state), 11);
            }
            return true;
        }

        return false;
    }

    /**
     * Получает семена для данной культуры
     */
    private ItemStack getSeedsFromCrop(Block crop) {
        if (crop == Blocks.WHEAT)
            return new ItemStack(Items.WHEAT_SEEDS);
        if (crop == Blocks.CARROTS)
            return new ItemStack(Items.CARROT);
        if (crop == Blocks.POTATOES)
            return new ItemStack(Items.POTATO);
        if (crop == Blocks.BEETROOTS)
            return new ItemStack(Items.BEETROOT_SEEDS);
        if (crop == Blocks.NETHER_WART)
            return new ItemStack(Items.NETHER_WART);
        return ItemStack.EMPTY;
    }

    /**
     * Проверяет, являются ли предметы семенами для данной культуры
     */
    private boolean areSeedsForCrop(Item item, Block crop) {
        if (crop == Blocks.WHEAT && item == Items.WHEAT_SEEDS)
            return true;
        if (crop == Blocks.CARROTS && item == Items.CARROT)
            return true;
        if (crop == Blocks.POTATOES && item == Items.POTATO)
            return true;
        if (crop == Blocks.BEETROOTS && item == Items.BEETROOT_SEEDS)
            return true;
        if (crop == Blocks.NETHER_WART && item == Items.NETHER_WART)
            return true;
        return false;
    }

    /**
     * Проверяет, является ли культура готовой к сбору урожая
     */
    private boolean isHarvestableCrop(BlockState state) {
        Block block = state.getBlock();

        // Нижний адский нарост
        if (block == Blocks.NETHER_WART) {
            IntegerProperty ageProperty = (IntegerProperty) state.getBlock().getStateDefinition()
                    .getProperty("age");
            if (ageProperty != null) {
                return state.getValue(ageProperty) >= 3;
            }
        }

        // Какао-бобы
        if (block == Blocks.COCOA) {
            IntegerProperty ageProperty = (IntegerProperty) state.getBlock().getStateDefinition()
                    .getProperty("age");
            if (ageProperty != null) {
                return state.getValue(ageProperty) >= 2;
            }
        }

        return false;
    }

    /**
     * Получает начальное состояние для пересадки культуры
     */
    private BlockState getResetStateForCrop(BlockState state) {
        Block block = state.getBlock();
        IntegerProperty ageProperty = (IntegerProperty) block.getStateDefinition().getProperty("age");

        if (ageProperty != null) {
            return state.setValue(ageProperty, 0);
        }

        return state;
    }

    /**
     * Создаёт предмет в мире
     */
    private void spawnItemInWorld(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    stack);
            level.addFreshEntity(itemEntity);
        }
    }
}
