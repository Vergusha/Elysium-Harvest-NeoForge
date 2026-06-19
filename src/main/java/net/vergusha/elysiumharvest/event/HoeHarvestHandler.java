package net.vergusha.elysiumharvest.event;

import java.util.List;

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

@EventBusSubscriber(modid = ElysiumHarvest.MODID)
public class HoeHarvestHandler {

    @SubscribeEvent
    public static void onHoeUse(UseItemOnBlockEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = event.getItemStack();
        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();

        if (heldItem.getItem() instanceof HoeItem && !(heldItem.getItem() instanceof FloriteHoeItem)) {
            BlockState targetState = level.getBlockState(clickedPos);
            if (tryHarvestCrop(level, clickedPos, targetState, player)) {
                heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
                player.swing(event.getHand());
                event.setCanceled(true);
            }
        }
    }

    private static boolean tryHarvestCrop(Level level, BlockPos pos, BlockState state, Player player) {
        Block block = state.getBlock();

        if (block instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
            if (level instanceof ServerLevel serverLevel) {
                LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.BLOCK_STATE, state)
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);

                if (player != null) {
                    lootBuilder.withParameter(LootContextParams.THIS_ENTITY, player);
                }

                List<ItemStack> drops = state.getDrops(lootBuilder);
                boolean consumedForReplant = false;
                for (ItemStack drop : drops) {
                    if (!consumedForReplant && isReplantingItem(drop.getItem(), block)) {
                        consumedForReplant = true;
                        if (drop.getCount() > 1) {
                            ItemStack remainingDrop = drop.copy();
                            remainingDrop.shrink(1);
                            spawnItemInWorld(level, pos, remainingDrop);
                        }
                    } else {
                        spawnItemInWorld(level, pos, drop);
                    }
                }

                level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, cropBlock.getStateForAge(0), 11);
            }
            return true;
        }

        if (isHarvestableCrop(state)) {
            if (level instanceof ServerLevel serverLevel) {
                LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.BLOCK_STATE, state)
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);

                if (player != null) {
                    lootBuilder.withParameter(LootContextParams.THIS_ENTITY, player);
                }

                List<ItemStack> drops = state.getDrops(lootBuilder);
                for (ItemStack drop : drops) {
                    spawnItemInWorld(level, pos, drop);
                }

                level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, getResetStateForCrop(state), 11);
            }
            return true;
        }

        return false;
    }

    private static boolean isReplantingItem(Item item, Block crop) {
        Item expectedSeed = getSeedItemForCrop(crop);
        return expectedSeed != null && item == expectedSeed;
    }

    private static Item getSeedItemForCrop(Block crop) {
        if (crop == Blocks.WHEAT)
            return Items.WHEAT_SEEDS;
        if (crop == Blocks.CARROTS)
            return Items.CARROT;
        if (crop == Blocks.POTATOES)
            return Items.POTATO;
        if (crop == Blocks.BEETROOTS)
            return Items.BEETROOT_SEEDS;
        if (crop == Blocks.NETHER_WART)
            return Items.NETHER_WART;
        if (crop == ElysiumHarvest.TOMATO_CROP.get())
            return ElysiumHarvest.TOMATO_SEEDS.get();
        if (crop == ElysiumHarvest.ONION_CROP.get())
            return ElysiumHarvest.ONION.get();
        if (crop == ElysiumHarvest.CUCUMBER_CROP.get())
            return ElysiumHarvest.CUCUMBER_SEEDS.get();
        if (crop == ElysiumHarvest.CABBAGE_CROP.get())
            return ElysiumHarvest.CABBAGE_SEEDS.get();
        if (crop == ElysiumHarvest.GARLIC_CROP.get())
            return ElysiumHarvest.GARLIC.get();
        if (crop == ElysiumHarvest.BELL_PEPPER_CROP.get())
            return ElysiumHarvest.BELL_PEPPER_SEEDS.get();
        if (crop == ElysiumHarvest.EGGPLANT_CROP.get())
            return ElysiumHarvest.EGGPLANT_SEEDS.get();
        if (crop == ElysiumHarvest.CORN_CROP.get())
            return ElysiumHarvest.CORN_SEEDS.get();
        if (crop == ElysiumHarvest.BROCCOLI_CROP.get())
            return ElysiumHarvest.BROCCOLI_SEEDS.get();
        if (crop == ElysiumHarvest.LETTUCE_CROP.get())
            return ElysiumHarvest.LETTUCE_SEEDS.get();
        if (crop == ElysiumHarvest.GINGER_CROP.get())
            return ElysiumHarvest.GINGER.get();
        return null;
    }

    private static boolean isHarvestableCrop(BlockState state) {
        Block block = state.getBlock();

        if (block == Blocks.NETHER_WART) {
            IntegerProperty ageProperty = (IntegerProperty) state.getBlock().getStateDefinition().getProperty("age");
            if (ageProperty != null) {
                return state.getValue(ageProperty) >= 3;
            }
        }

        if (block == Blocks.COCOA) {
            IntegerProperty ageProperty = (IntegerProperty) state.getBlock().getStateDefinition().getProperty("age");
            if (ageProperty != null) {
                return state.getValue(ageProperty) >= 2;
            }
        }

        return false;
    }

    private static BlockState getResetStateForCrop(BlockState state) {
        Block block = state.getBlock();
        IntegerProperty ageProperty = (IntegerProperty) block.getStateDefinition().getProperty("age");

        if (ageProperty != null) {
            return state.setValue(ageProperty, 0);
        }

        return state;
    }

    private static void spawnItemInWorld(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    stack);
            level.addFreshEntity(itemEntity);
        }
    }
}
