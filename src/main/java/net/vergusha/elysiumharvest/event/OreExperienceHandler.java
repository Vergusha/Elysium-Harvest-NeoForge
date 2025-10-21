package net.vergusha.elysiumharvest.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.vergusha.elysiumharvest.ElysiumHarvest;

/**
 * Handler for dropping experience from Florite Ore
 */
@EventBusSubscriber(modid = ElysiumHarvest.MODID)
public class OreExperienceHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Block block = event.getState().getBlock();

        // Check if the broken block is Deepslate Florite Ore
        if (block == ElysiumHarvest.DEEPSLATE_FLORITE_ORE.get()) {
            // Check if player is NOT using Silk Touch
            int silkTouchLevel = event.getPlayer().getMainHandItem().getEnchantmentLevel(
                    event.getLevel().holderOrThrow(Enchantments.SILK_TOUCH));

            if (silkTouchLevel == 0) {
                // Drop experience (2-5 range, like iron/copper ore)
                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    BlockPos pos = event.getPos();
                    int xp = 2 + serverLevel.random.nextInt(4); // Random 2-5
                    block.popExperience(serverLevel, pos, xp);
                }
            }
        }
    }
}
