package net.vergusha.elysiumharvest.client;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.screen.QazanScreen;

public class ClientModEvents {
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ElysiumHarvest.QAZAN_MENU.get(), QazanScreen::new);
    }
}
