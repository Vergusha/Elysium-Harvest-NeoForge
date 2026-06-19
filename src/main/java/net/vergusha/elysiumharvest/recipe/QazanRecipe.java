package net.vergusha.elysiumharvest.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.vergusha.elysiumharvest.ElysiumHarvest;
import net.vergusha.elysiumharvest.blockentity.QazanBlockEntity;

import java.util.ArrayList;
import java.util.List;

public record QazanRecipe(
        List<Ingredient> ingredients,
        ItemStack result) implements Recipe<RecipeInput> {

    @Override
    public boolean matches(RecipeInput container, Level level) {
        if (level.isClientSide()) {
            return false;
        }

        // Собираем все предметы из контейнера
        List<ItemStack> containerItems = new ArrayList<>();
        for (int i = 0; i < Math.min(container.size(), QazanBlockEntity.INGREDIENT_SLOTS); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                containerItems.add(stack);
            }
        }

        // Проверяем соответствие ингредиентов
        List<Ingredient> remainingIngredients = new ArrayList<>(ingredients);

        for (ItemStack containerItem : containerItems) {
            boolean matched = false;
            for (int i = 0; i < remainingIngredients.size(); i++) {
                if (remainingIngredients.get(i).test(containerItem)) {
                    remainingIngredients.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false; // Есть лишний предмет
            }
        }

        return remainingIngredients.isEmpty(); // Все ингредиенты использованы
    }

    public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
        return result.copy();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<QazanRecipe> getSerializer() {
        return ElysiumHarvest.QAZAN_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<QazanRecipe> getType() {
        return ElysiumHarvest.QAZAN_RECIPE_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return new RecipeBookCategory();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    public static class Serializer implements RecipeSerializer<QazanRecipe> {
        // Use Ingredient.CODEC directly - it already handles string item IDs in 1.21
        private static final MapCodec<QazanRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(QazanRecipe::ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(QazanRecipe::result)).apply(instance, QazanRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, QazanRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork,
                Serializer::fromNetwork);

        @Override
        public MapCodec<QazanRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, QazanRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static QazanRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            int ingredientCount = buffer.readVarInt();
            List<Ingredient> ingredients = new ArrayList<>();
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            return new QazanRecipe(ingredients, result);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, QazanRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}
