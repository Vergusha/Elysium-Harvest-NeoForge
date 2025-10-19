package net.vergusha.elysiumharvest.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.vergusha.elysiumharvest.ElysiumHarvest;

@EventBusSubscriber(modid = ElysiumHarvest.MODID, value = Dist.CLIENT)
public class ArmorTooltipHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();

        // Проверяем, является ли предмет частью флоритовой брони
        if (item == ElysiumHarvest.FLORITE_HELMET.get() ||
                item == ElysiumHarvest.FLORITE_CHESTPLATE.get() ||
                item == ElysiumHarvest.FLORITE_LEGGINGS.get() ||
                item == ElysiumHarvest.FLORITE_BOOTS.get()) {

            // Добавляем пустую строку для отступа
            event.getToolTip().add(Component.empty());

            // Добавляем подсказку о бонусе набора
            event.getToolTip().add(Component.translatable("tooltip.elysiumharvest.florite_armor_set")
                    .withStyle(ChatFormatting.GREEN));
        }
    }
}
