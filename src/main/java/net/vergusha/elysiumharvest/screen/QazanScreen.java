package net.vergusha.elysiumharvest.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.menu.QazanMenu;

public class QazanScreen extends AbstractContainerScreen<QazanMenu> {
    private static final ResourceLocation TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(ElysiumHarvest.MODID, "textures/gui/qazan.png");

    public QazanScreen(QazanMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // TODO: Fix for 1.21.10 - GuiGraphics.blit() API significantly changed
        // Rendering simple background for now
        // Removed renderBackground() call that caused infinite recursion
        
        // Отрисовка прогресс-бара
        // TODO: Re-implement when correct GUI rendering API is found
        int progress = this.menu.getScaledProgress();
        // if (progress > 0) {
        //     // Progress bar rendering disabled until API is fixed
        // }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
