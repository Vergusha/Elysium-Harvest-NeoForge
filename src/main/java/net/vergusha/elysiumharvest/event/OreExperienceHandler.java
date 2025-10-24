package net.vergusha.elysiumharvest.event;

import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.vergusha.elysiumharvest.ElysiumHarvest;

/**
 * Ensures Florite ore does not award extra experience when broken.
 */
@EventBusSubscriber(modid = ElysiumHarvest.MODID)
public class OreExperienceHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Block block = event.getState().getBlock();

        // Check if the broken block is Deepslate Florite Ore
        if (block == ElysiumHarvest.DEEPSLATE_FLORITE_ORE.get()) {
            // XP drops were intentionally removed; nothing to do here.
        }
    }
}
