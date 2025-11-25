package net.vergusha.elysiumharvest.event;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.vergusha.elysiumharvest.ElysiumHarvest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Обработчик посадки семян на вспаханную землю
 */
@EventBusSubscriber(modid = ElysiumHarvest.MODID)
public class SeedPlantingHandler {

    // Карта соответствия семян и блоков культур
    private static final Map<Supplier<? extends Item>, Supplier<? extends Block>> SEED_TO_CROP = new HashMap<>();

    static {
        // Семена -> Блоки культур
        SEED_TO_CROP.put(() -> ElysiumHarvest.TOMATO_SEEDS.get(), () -> ElysiumHarvest.TOMATO_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.ONION.get(), () -> ElysiumHarvest.ONION_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.CUCUMBER_SEEDS.get(), () -> ElysiumHarvest.CUCUMBER_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.CABBAGE_SEEDS.get(), () -> ElysiumHarvest.CABBAGE_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.GARLIC.get(), () -> ElysiumHarvest.GARLIC_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.BELL_PEPPER_SEEDS.get(), () -> ElysiumHarvest.BELL_PEPPER_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.EGGPLANT_SEEDS.get(), () -> ElysiumHarvest.EGGPLANT_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.CORN_SEEDS.get(), () -> ElysiumHarvest.CORN_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.BROCCOLI_SEEDS.get(), () -> ElysiumHarvest.BROCCOLI_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.LETTUCE_SEEDS.get(), () -> ElysiumHarvest.LETTUCE_CROP.get());
        SEED_TO_CROP.put(() -> ElysiumHarvest.GINGER.get(), () -> ElysiumHarvest.GINGER_CROP.get());
    }

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
            return;
        }

        UseOnContext context = event.getUseOnContext();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (stack.isEmpty()) {
            return;
        }

        // Проверяем, что кликнули на вспаханную землю
        BlockState clickedState = level.getBlockState(pos);
        if (clickedState.getBlock() != Blocks.FARMLAND) {
            return;
        }

        // Позиция над грядкой
        BlockPos cropPos = pos.above();

        // Проверяем, что над грядкой воздух
        if (!level.getBlockState(cropPos).isAir()) {
            return;
        }

        // Ищем соответствующий блок культуры для этих семян
        for (Map.Entry<Supplier<? extends Item>, Supplier<? extends Block>> entry : SEED_TO_CROP.entrySet()) {
            Item seedItem = entry.getKey().get();
            if (stack.getItem() == seedItem) {
                Block cropBlock = entry.getValue().get();

                if (!level.isClientSide()) {
                    // Сажаем культуру
                    level.setBlock(cropPos, cropBlock.defaultBlockState(), 3);

                    // Уменьшаем количество семян
                    if (player != null && !player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }

                // Проигрываем звук посадки
                level.playSound(player, cropPos, SoundEvents.CROP_PLANTED,
                        SoundSource.BLOCKS, 1.0F, 1.0F);

                event.cancelWithResult(InteractionResult.SUCCESS);
                return;
            }
        }
    }
}
