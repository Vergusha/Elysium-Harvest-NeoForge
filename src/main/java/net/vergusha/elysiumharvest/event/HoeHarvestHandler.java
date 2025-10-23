package net.vergusha.elysiumharvest.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.item.FloriteHoeItem;

import java.util.List;

/**
 * Обработчик сбора урожая для всех мотыг (кроме Флоритовой)
 * Флоритовая мотыга имеет свою реализацию в FloriteHoeItem
 */
@EventBusSubscriber(modid = ElysiumHarvest.MODID)
public class HoeHarvestHandler {

    @SubscribeEvent
    public static void onHoeUse(UseItemOnBlockEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = event.getItemStack();
        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();

        // Проверяем, что в руке мотыга (но не Флоритовая - у неё своя логика)
        if (heldItem.getItem() instanceof HoeItem && !(heldItem.getItem() instanceof FloriteHoeItem)) {
            BlockState targetState = level.getBlockState(clickedPos);

            // Пытаемся собрать урожай (только 1 блок)
            if (tryHarvestCrop(level, clickedPos, targetState, player)) {
                // Применяем износ инструмента
                heldItem.hurtAndBreak(1, player,
                        player.getEquipmentSlotForItem(heldItem));
            }
        }
    }

    /**
     * Пытается собрать урожай и автоматически пересадить его
     * 
     * @return true если урожай был собран
     */
    private static boolean tryHarvestCrop(Level level, BlockPos pos, BlockState state, Player player) {
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
     * Проверяет, являются ли предметы семенами для данной культуры
     */
    private static boolean areSeedsForCrop(Item item, Block crop) {
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
    private static boolean isHarvestableCrop(BlockState state) {
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
    private static BlockState getResetStateForCrop(BlockState state) {
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
    private static void spawnItemInWorld(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    stack);
            level.addFreshEntity(itemEntity);
        }
    }
}
