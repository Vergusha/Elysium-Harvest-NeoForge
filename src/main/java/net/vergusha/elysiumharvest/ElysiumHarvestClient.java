package net.vergusha.elysiumharvest;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.vergusha.elysiumharvest.client.ClientModEvents;

public class ElysiumHarvestClient {
    public ElysiumHarvestClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(ElysiumHarvestClient::onClientSetup);
        modEventBus.addListener(ClientModEvents::registerScreens);
    }

    static void onClientSetup(FMLClientSetupEvent event) {
        ElysiumHarvest.LOGGER.info("HELLO FROM CLIENT SETUP");
    }
}
