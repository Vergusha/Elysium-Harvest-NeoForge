package net.vergusha.elysiumharvest.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.vergusha.elysiumharvest.ElysiumHarvest;

import java.util.List;

/**
 * Handles wild crop generation in the world.
 * Wild crops can spawn in plains, forests, and other suitable biomes.
 */
public class ModWorldGeneration {

    public static final String MODID = ElysiumHarvest.MODID;

    // Resource keys for configured features
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_TOMATOES = createConfiguredKey("wild_tomatoes");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_ONIONS = createConfiguredKey("wild_onions");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_CUCUMBERS = createConfiguredKey("wild_cucumbers");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_CABBAGES = createConfiguredKey("wild_cabbages");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_GARLIC = createConfiguredKey("wild_garlic");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_BELL_PEPPERS = createConfiguredKey(
            "wild_bell_peppers");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_EGGPLANTS = createConfiguredKey("wild_eggplants");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_CORN = createConfiguredKey("wild_corn");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_BROCCOLI = createConfiguredKey("wild_broccoli");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_LETTUCE = createConfiguredKey("wild_lettuce");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_GINGER = createConfiguredKey("wild_ginger");

    // Resource keys for placed features
    public static final ResourceKey<PlacedFeature> WILD_TOMATOES_PLACED = createPlacedKey("wild_tomatoes");
    public static final ResourceKey<PlacedFeature> WILD_ONIONS_PLACED = createPlacedKey("wild_onions");
    public static final ResourceKey<PlacedFeature> WILD_CUCUMBERS_PLACED = createPlacedKey("wild_cucumbers");
    public static final ResourceKey<PlacedFeature> WILD_CABBAGES_PLACED = createPlacedKey("wild_cabbages");
    public static final ResourceKey<PlacedFeature> WILD_GARLIC_PLACED = createPlacedKey("wild_garlic");
    public static final ResourceKey<PlacedFeature> WILD_BELL_PEPPERS_PLACED = createPlacedKey("wild_bell_peppers");
    public static final ResourceKey<PlacedFeature> WILD_EGGPLANTS_PLACED = createPlacedKey("wild_eggplants");
    public static final ResourceKey<PlacedFeature> WILD_CORN_PLACED = createPlacedKey("wild_corn");
    public static final ResourceKey<PlacedFeature> WILD_BROCCOLI_PLACED = createPlacedKey("wild_broccoli");
    public static final ResourceKey<PlacedFeature> WILD_LETTUCE_PLACED = createPlacedKey("wild_lettuce");
    public static final ResourceKey<PlacedFeature> WILD_GINGER_PLACED = createPlacedKey("wild_ginger");

    private static ResourceKey<ConfiguredFeature<?, ?>> createConfiguredKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, name));
    }

    private static ResourceKey<PlacedFeature> createPlacedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, name));
    }
}
