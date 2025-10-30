package net.vergusha.elysiumharvest.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.screen.QazanScreen;

@EventBusSubscriber(modid = ElysiumHarvest.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ElysiumHarvest.QAZAN_MENU.get(), QazanScreen::new);
    }
}
