package net.vergusha.elysiumharvest.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.menu.QazanMenu;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class QazanScreen extends AbstractContainerScreen<QazanMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            ElysiumHarvest.MODID, "textures/gui/qazan.png");
    private static final int PANEL_WIDTH = 76;
    private static final int PANEL_HEIGHT_PADDING = 8;
    private static final int PANEL_ENTRY_SIZE = 18;
    private static final int PANEL_ENTRY_GAP = 4;
    private static final int PANEL_COLUMNS = 2;
    private static final int PANEL_ROWS = 5;
    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_GAP = 4;
    private static final int PANEL_GAP = 4;
    private static final int[][] INGREDIENT_SLOT_POSITIONS = {
            { 44, 17 },
            { 62, 17 },
            { 80, 17 },
            { 44, 35 },
            { 62, 35 },
            { 80, 35 }
    };
    private static final int RESULT_SLOT_X = 116;
    private static final int RESULT_SLOT_Y = 26;

    private final List<DisplayedRecipe> displayedRecipes = new ArrayList<>();
    private ImageButton recipeBookButton;
    private boolean recipesPanelOpen;
    private DisplayedRecipe selectedRecipe;

    public QazanScreen(QazanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.loadDisplayedRecipes();

        this.recipeBookButton = this.addRenderableWidget(new ImageButton(
                this.leftPos - BUTTON_WIDTH - BUTTON_GAP,
                this.topPos + 5,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                RecipeBookComponent.RECIPE_BUTTON_SPRITES,
                button -> this.recipesPanelOpen = !this.recipesPanelOpen,
                CommonComponents.EMPTY));
        this.recipeBookButton.setTooltip(Tooltip.create(Component.literal("Qazan recipes")));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND,
                this.leftPos, this.topPos,
                0, 0,
                this.imageWidth, this.imageHeight,
                256, 256);

        int progress = this.menu.getScaledProgress();
        if (progress > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    BACKGROUND,
                    this.leftPos + 103, this.topPos + 26,
                    176, 0,
                    progress, 16,
                    256, 256);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.recipesPanelOpen) {
            this.renderRecipesPanel(graphics, mouseX, mouseY);
            this.renderGhostRecipe(graphics);
            this.renderRecipePanelTooltip(graphics, mouseX, mouseY);
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (this.recipesPanelOpen && event.button() == 0) {
            DisplayedRecipe recipe = this.getRecipeAt(event.x(), event.y());
            if (recipe != null) {
                this.selectedRecipe = recipe;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void renderRecipesPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int panelX = this.getPanelX();
        int panelY = this.topPos;
        int panelHeight = this.imageHeight;

        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, 0xFF2A2A2A);
        graphics.fill(panelX + 1, panelY + 1, panelX + PANEL_WIDTH - 1, panelY + panelHeight - 1, 0xFFC6C6C6);
        graphics.fill(panelX + 4, panelY + 4, panelX + PANEL_WIDTH - 4, panelY + panelHeight - 4, 0xFF8B8B8B);
        graphics.drawCenteredString(this.font, Component.literal("Recipes"), panelX + PANEL_WIDTH / 2, panelY + 8, 0x202020);

        int maxRecipes = PANEL_COLUMNS * PANEL_ROWS;
        for (int index = 0; index < Math.min(this.displayedRecipes.size(), maxRecipes); index++) {
            DisplayedRecipe recipe = this.displayedRecipes.get(index);
            int entryX = this.getPanelEntryX(index);
            int entryY = this.getPanelEntryY(index);
            boolean hovered = this.isPointWithin(entryX, entryY, PANEL_ENTRY_SIZE, PANEL_ENTRY_SIZE, mouseX, mouseY);
            boolean selected = recipe.equals(this.selectedRecipe);
            int borderColor = selected ? 0xFFFFD54F : (hovered ? 0xFFFFFFFF : 0xFF4A4A4A);
            int fillColor = selected ? 0xFF6A6A6A : 0xFFB2B2B2;

            graphics.fill(entryX, entryY, entryX + PANEL_ENTRY_SIZE, entryY + PANEL_ENTRY_SIZE, borderColor);
            graphics.fill(entryX + 1, entryY + 1, entryX + PANEL_ENTRY_SIZE - 1, entryY + PANEL_ENTRY_SIZE - 1, fillColor);
            graphics.renderFakeItem(recipe.result(), entryX + 1, entryY + 1);
            graphics.renderItemDecorations(this.font, recipe.result(), entryX + 1, entryY + 1);
        }
    }

    private void renderGhostRecipe(GuiGraphics graphics) {
        if (this.selectedRecipe == null) {
            return;
        }

        for (int slot = 0; slot < Math.min(INGREDIENT_SLOT_POSITIONS.length, this.selectedRecipe.ingredients().size()); slot++) {
            if (this.menu.getSlot(slot).hasItem()) {
                continue;
            }

            ItemStack ingredient = this.selectedRecipe.ingredients().get(slot);
            if (ingredient.isEmpty()) {
                continue;
            }

            int slotX = this.leftPos + INGREDIENT_SLOT_POSITIONS[slot][0];
            int slotY = this.topPos + INGREDIENT_SLOT_POSITIONS[slot][1];
            graphics.renderFakeItem(ingredient, slotX, slotY);
            graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x66FFFFFF);
        }

        if (!this.menu.getSlot(6).hasItem() && !this.selectedRecipe.result().isEmpty()) {
            int resultX = this.leftPos + RESULT_SLOT_X;
            int resultY = this.topPos + RESULT_SLOT_Y;
            graphics.renderFakeItem(this.selectedRecipe.result(), resultX, resultY);
            graphics.fill(resultX, resultY, resultX + 16, resultY + 16, 0x66FFFFFF);
        }
    }

    private void renderRecipePanelTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        DisplayedRecipe hoveredRecipe = this.getRecipeAt(mouseX, mouseY);
        if (hoveredRecipe == null) {
            return;
        }

        List<Component> tooltip = new ArrayList<>();
        MutableComponent title = hoveredRecipe.result().getHoverName().copy();
        tooltip.add(title);
        for (ItemStack ingredient : hoveredRecipe.ingredients()) {
            tooltip.add(Component.literal("- ").append(ingredient.getHoverName()));
        }
        graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
    }

    private DisplayedRecipe getRecipeAt(double mouseX, double mouseY) {
        int maxRecipes = PANEL_COLUMNS * PANEL_ROWS;
        for (int index = 0; index < Math.min(this.displayedRecipes.size(), maxRecipes); index++) {
            int entryX = this.getPanelEntryX(index);
            int entryY = this.getPanelEntryY(index);
            if (this.isPointWithin(entryX, entryY, PANEL_ENTRY_SIZE, PANEL_ENTRY_SIZE, mouseX, mouseY)) {
                return this.displayedRecipes.get(index);
            }
        }
        return null;
    }

    private void loadDisplayedRecipes() {
        this.displayedRecipes.clear();

        Map<ResourceLocation, Resource> resources = this.minecraft.getResourceManager().listResources(
                "recipe",
                location -> location.getNamespace().equals(ElysiumHarvest.MODID) && location.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (!root.has("type") || !"elysiumharvest:qazan_cooking".equals(root.get("type").getAsString())) {
                    continue;
                }

                List<ItemStack> ingredients = this.parseIngredients(root.getAsJsonArray("ingredients"));
                ItemStack result = this.parseResult(root.getAsJsonObject("result"));
                if (!result.isEmpty()) {
                    this.displayedRecipes.add(new DisplayedRecipe(entry.getKey(), ingredients, result));
                }
            } catch (IOException | RuntimeException exception) {
                ElysiumHarvest.LOGGER.warn("Failed to load qazan recipe preview from {}", entry.getKey(), exception);
            }
        }

        this.displayedRecipes.sort(Comparator.comparing(recipe -> recipe.result().getHoverName().getString()));
        if (this.selectedRecipe != null) {
            ResourceLocation selectedId = this.selectedRecipe.id();
            this.selectedRecipe = this.displayedRecipes.stream()
                    .filter(recipe -> recipe.id().equals(selectedId))
                    .findFirst()
                    .orElse(null);
        }
        if (this.selectedRecipe == null && !this.displayedRecipes.isEmpty()) {
            this.selectedRecipe = this.displayedRecipes.get(0);
        }
    }

    private List<ItemStack> parseIngredients(JsonArray ingredientsJson) {
        List<ItemStack> ingredients = new ArrayList<>();
        for (JsonElement element : ingredientsJson) {
            ItemStack ingredient = ItemStack.EMPTY;
            if (element.isJsonPrimitive()) {
                ingredient = this.resolveItemStack(ResourceLocation.parse(element.getAsString()), 1);
            } else if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("item")) {
                    int count = object.has("count") ? object.get("count").getAsInt() : 1;
                    ingredient = this.resolveItemStack(ResourceLocation.parse(object.get("item").getAsString()), count);
                }
            }

            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    private ItemStack parseResult(JsonObject resultJson) {
        if (!resultJson.has("id")) {
            return ItemStack.EMPTY;
        }
        int count = resultJson.has("count") ? resultJson.get("count").getAsInt() : 1;
        return this.resolveItemStack(ResourceLocation.parse(resultJson.get("id").getAsString()), count);
    }

    private ItemStack resolveItemStack(ResourceLocation itemId, int count) {
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, Math.max(1, count));
    }

    private int getPanelX() {
        return this.leftPos - PANEL_WIDTH - BUTTON_WIDTH - BUTTON_GAP - PANEL_GAP;
    }

    private int getPanelEntryX(int index) {
        int column = index % PANEL_COLUMNS;
        return this.getPanelX() + 8 + column * (PANEL_ENTRY_SIZE + PANEL_ENTRY_GAP);
    }

    private int getPanelEntryY(int index) {
        int row = index / PANEL_COLUMNS;
        return this.topPos + 22 + row * (PANEL_ENTRY_SIZE + PANEL_ENTRY_GAP);
    }

    private boolean isPointWithin(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private record DisplayedRecipe(ResourceLocation id, List<ItemStack> ingredients, ItemStack result) {
    }
}
