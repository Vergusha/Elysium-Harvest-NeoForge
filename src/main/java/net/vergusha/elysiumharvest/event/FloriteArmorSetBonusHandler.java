package net.vergusha.elysiumharvest.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.vergusha.elysiumharvest.ElysiumHarvest;

@EventBusSubscriber(modid = ElysiumHarvest.MODID)
public class FloriteArmorSetBonusHandler {

    private static final int GROWTH_RADIUS = 3; // Радиус действия эффекта
    private static final int TICK_INTERVAL = 20; // Проверка каждую секунду (20 тиков)

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Проверяем только на сервере и каждые 20 тиков
        if (!(player.level() instanceof ServerLevel) || player.level().getGameTime() % TICK_INTERVAL != 0) {
            return;
        }

        // Проверяем, носит ли игрок полный набор флоритовой брони
        if (!isWearingFullFloriteArmor(player)) {
            return;
        }

        // Добавляем визуальный эффект вокруг игрока
        spawnSetBonusParticles(player);

        // Ускоряем рост растений вокруг игрока
        accelerateCropGrowth(player);
    }

    private static boolean isWearingFullFloriteArmor(Player player) {
        Item helmet = player.getItemBySlot(EquipmentSlot.HEAD).getItem();
        Item chestplate = player.getItemBySlot(EquipmentSlot.CHEST).getItem();
        Item leggings = player.getItemBySlot(EquipmentSlot.LEGS).getItem();
        Item boots = player.getItemBySlot(EquipmentSlot.FEET).getItem();

        return helmet == ElysiumHarvest.FLORITE_HELMET.get() &&
                chestplate == ElysiumHarvest.FLORITE_CHESTPLATE.get() &&
                leggings == ElysiumHarvest.FLORITE_LEGGINGS.get() &&
                boots == ElysiumHarvest.FLORITE_BOOTS.get();
    }

    private static void accelerateCropGrowth(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos playerPos = player.blockPosition();

        // Проходим по всем блокам вокруг игрока
        for (int x = -GROWTH_RADIUS; x <= GROWTH_RADIUS; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -GROWTH_RADIUS; z <= GROWTH_RADIUS; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    Block block = state.getBlock();

                    // Проверяем, является ли блок культурным растением (исключаем траву и цветы)
                    if (isCropBlock(block) && block instanceof BonemealableBlock bonemealable) {
                        // Проверяем, может ли растение расти
                        if (bonemealable.isValidBonemealTarget(level, pos, state)) {
                            // С шансом 30% ускоряем рост (как костная мука)
                            if (level.random.nextFloat() < 0.3f) {
                                bonemealable.performBonemeal(level, level.random, pos, state);

                                // Добавляем частицы роста
                                level.levelEvent(2005, pos, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Проверяет, является ли блок культурным растением (crops)
     * Исключает траву, цветы и другие декоративные растения
     */
    private static boolean isCropBlock(Block block) {
        return block instanceof CropBlock || // Пшеница, морковь, картофель, свекла и т.д.
                block instanceof StemBlock || // Тыквы и арбузы (стебли)
                block instanceof NetherWartBlock || // Адский нарост
                block instanceof SweetBerryBushBlock || // Сладкие ягоды
                block instanceof CocoaBlock; // Какао-бобы
    }

    private static void spawnSetBonusParticles(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        double x = player.getX();
        double y = player.getY() + 0.5;
        double z = player.getZ();

        // Создаём круговые частицы вокруг игрока
        for (int i = 0; i < 3; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double radius = 1.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    x + offsetX, y, z + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
