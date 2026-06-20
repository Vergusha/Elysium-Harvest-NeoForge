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
import net.minecraft.world.item.Items;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.blockentity.QazanBlockEntity;
import net.vergusha.elysiumharvest.menu.QazanMenu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QazanScreen extends AbstractContainerScreen<QazanMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            ElysiumHarvest.MODID, "textures/gui/qazan.png");
    private static final ResourceLocation RECIPES_PANEL = ResourceLocation.fromNamespaceAndPath(
            ElysiumHarvest.MODID, "textures/gui/qazan_recipes.png");
    private static final int GUI_WIDTH = 176;
    private static final int BACKGROUND_WIDTH = 182;
    private static final int BACKGROUND_HEIGHT = 166;
    private static final int PANEL_WIDTH = 68;
    private static final int PANEL_HEIGHT = 166;
    private static final int PANEL_ENTRY_SIZE = 18;
    private static final int PANEL_ENTRY_GAP = 4;
    private static final int PANEL_COLUMNS = 2;
    private static final int PANEL_ROWS = 5;
    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 18;
    private static final int PANEL_GAP = 0;
    private static final int CONTAINER_SLOT_X = 116;
    private static final int CONTAINER_SLOT_Y = 52;
    private static final int[][] INGREDIENT_SLOT_POSITIONS = {
            { 40, 17 },
            { 58, 17 },
            { 76, 17 },
            { 40, 35 },
            { 58, 35 },
            { 76, 35 }
    };
    private static final int RESULT_SLOT_X = 116;
    private static final int RESULT_SLOT_Y = 26;
    private static final int PROGRESS_X = 100;
    private static final int PROGRESS_Y = 16;
    private static final int PROGRESS_WIDTH = 6;
    private static final int PROGRESS_HEIGHT = 36;
    private static final int PROGRESS_TEXTURE_U = 176;
    private static final int PROGRESS_TEXTURE_V = 0;
    private static final List<String> QAZAN_RECIPE_FILES = List.of(
            "borscht.json",
            "broccoli_soup.json",
            "corn_soup.json",
            "ginger_tea.json",
            "harvest_stew_from_qazan.json",
            "mushroom_stew_upgraded.json",
            "roasted_vegetables.json",
            "salad.json",
            "stew.json",
            "vegetable_soup.json");

    private final List<DisplayedRecipe> displayedRecipes = new ArrayList<>();
    private ImageButton recipeBookButton;
    private boolean recipesPanelOpen;
    private DisplayedRecipe selectedRecipe;

    public QazanScreen(QazanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = BACKGROUND_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.loadDisplayedRecipes();
        this.titleLabelX = 30;

        this.recipeBookButton = this.addRenderableWidget(new ImageButton(
                this.leftPos + 7,
                this.topPos + 4,
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
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        this.renderCookingProgress(graphics, this.menu.getScaledProgress());
    }

    private void renderCookingProgress(GuiGraphics graphics, int progress) {
        if (progress <= 0) {
            return;
        }

        int fillHeight = Math.max(1, Math.min(PROGRESS_HEIGHT, progress * PROGRESS_HEIGHT / 24));
        int targetY = this.topPos + PROGRESS_Y + PROGRESS_HEIGHT - fillHeight;
        int sourceV = PROGRESS_TEXTURE_V + PROGRESS_HEIGHT - fillHeight;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND,
                this.leftPos + PROGRESS_X,
                targetY,
                PROGRESS_TEXTURE_U,
                sourceV,
                PROGRESS_WIDTH,
                fillHeight,
                BACKGROUND_WIDTH,
                BACKGROUND_HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.recipesPanelOpen) {
            this.renderRecipesPanel(graphics, mouseX, mouseY);
            this.renderGhostRecipe(graphics);
            this.renderRecipePanelTooltip(graphics, mouseX, mouseY);
        }
        this.renderContainerSlotTooltip(graphics, mouseX, mouseY);
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
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                RECIPES_PANEL,
                panelX,
                panelY,
                0,
                0,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                PANEL_WIDTH,
                PANEL_HEIGHT);
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

        if (!this.menu.getSlot(QazanBlockEntity.RESULT_SLOT).hasItem() && !this.selectedRecipe.result().isEmpty()) {
            int resultX = this.leftPos + RESULT_SLOT_X;
            int resultY = this.topPos + RESULT_SLOT_Y;
            graphics.renderFakeItem(this.selectedRecipe.result(), resultX, resultY);
            graphics.fill(resultX, resultY, resultX + 16, resultY + 16, 0x66FFFFFF);
        }

        if (!this.menu.getSlot(QazanBlockEntity.BOWL_SLOT).hasItem()) {
            ItemStack requiredContainer = this.getRequiredContainer(this.selectedRecipe.result());
            int containerX = this.leftPos + CONTAINER_SLOT_X;
            int containerY = this.topPos + CONTAINER_SLOT_Y;
            graphics.renderFakeItem(requiredContainer, containerX, containerY);
            graphics.fill(containerX, containerY, containerX + 16, containerY + 16, 0x66FFFFFF);
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
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Container: ").append(this.getRequiredContainer(hoveredRecipe.result()).getHoverName()));
        graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
    }

    private void renderContainerSlotTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!this.isHovering(CONTAINER_SLOT_X, CONTAINER_SLOT_Y, 16, 16, mouseX, mouseY)
                || this.menu.getSlot(QazanBlockEntity.BOWL_SLOT).hasItem()) {
            return;
        }

        ItemStack container = this.selectedRecipe == null
                ? new ItemStack(Items.BOWL)
                : this.getRequiredContainer(this.selectedRecipe.result());
        graphics.setComponentTooltipForNextFrame(this.font, List.of(
                Component.literal("Container slot"),
                container.getHoverName()), mouseX, mouseY);
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
        Set<ResourceLocation> loadedRecipeIds = new LinkedHashSet<>();

        Map<ResourceLocation, Resource> resources = this.minecraft.getResourceManager().listResources(
                "recipe",
                location -> location.getNamespace().equals(ElysiumHarvest.MODID) && location.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                this.tryAddRecipe(entry.getKey(), reader, loadedRecipeIds);
            } catch (IOException | RuntimeException exception) {
                ElysiumHarvest.LOGGER.warn("Failed to load qazan recipe preview from {}", entry.getKey(), exception);
            }
        }

        if (this.displayedRecipes.isEmpty()) {
            for (String fileName : QAZAN_RECIPE_FILES) {
                ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(ElysiumHarvest.MODID, "recipe/" + fileName);
                this.tryLoadRecipeFromClasspath(recipeId, loadedRecipeIds);
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

    private void tryLoadRecipeFromClasspath(ResourceLocation recipeId, Set<ResourceLocation> loadedRecipeIds) {
        String classpathPath = "/data/" + recipeId.getNamespace() + "/" + recipeId.getPath();
        try (InputStream stream = QazanScreen.class.getResourceAsStream(classpathPath)) {
            if (stream == null) {
                ElysiumHarvest.LOGGER.warn("Qazan recipe preview resource not found: {}", classpathPath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                this.tryAddRecipe(recipeId, reader, loadedRecipeIds);
            }
        } catch (IOException | RuntimeException exception) {
            ElysiumHarvest.LOGGER.warn("Failed to load qazan recipe preview from classpath {}", classpathPath, exception);
        }
    }

    private void tryAddRecipe(ResourceLocation recipeId, BufferedReader reader, Set<ResourceLocation> loadedRecipeIds) {
        if (!loadedRecipeIds.add(recipeId)) {
            return;
        }

        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        if (!root.has("type") || !"elysiumharvest:qazan_cooking".equals(root.get("type").getAsString())) {
            return;
        }

        List<ItemStack> ingredients = this.parseIngredients(root.getAsJsonArray("ingredients"));
        ItemStack result = this.parseResult(root.getAsJsonObject("result"));
        if (!result.isEmpty()) {
            this.displayedRecipes.add(new DisplayedRecipe(recipeId, ingredients, result));
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

    private ItemStack getRequiredContainer(ItemStack result) {
        return result.is(ElysiumHarvest.GINGER_TEA.get()) ? new ItemStack(Items.GLASS_BOTTLE) : new ItemStack(Items.BOWL);
    }

    private int getPanelX() {
        return this.leftPos - PANEL_WIDTH - PANEL_GAP;
    }

    private int getPanelEntryX(int index) {
        int column = index % PANEL_COLUMNS;
        int contentWidth = PANEL_COLUMNS * PANEL_ENTRY_SIZE + (PANEL_COLUMNS - 1) * PANEL_ENTRY_GAP;
        int leftPadding = (PANEL_WIDTH - contentWidth) / 2;
        return this.getPanelX() + leftPadding + column * (PANEL_ENTRY_SIZE + PANEL_ENTRY_GAP);
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
