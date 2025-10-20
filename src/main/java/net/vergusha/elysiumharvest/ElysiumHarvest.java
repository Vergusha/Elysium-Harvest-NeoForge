package net.vergusha.elysiumharvest;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.vergusha.elysiumharvest.item.FloriteAxeItem;
import net.vergusha.elysiumharvest.item.FloriteHoeItem;
import net.vergusha.elysiumharvest.item.FloritePickaxeItem;
import net.vergusha.elysiumharvest.item.FloriteShovelItem;
import net.vergusha.elysiumharvest.item.FloriteSwordItem;
import net.vergusha.elysiumharvest.effect.FloriteSetBonusEffect;

import java.util.EnumMap;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ElysiumHarvest.MODID)
public class ElysiumHarvest {
        // Define mod id in a common place for everything to reference
        public static final String MODID = "elysiumharvest";
        // Directly reference a slf4j logger
        public static final Logger LOGGER = LogUtils.getLogger();
        // Create a Deferred Register to hold Blocks which will all be registered under
        // the "elysiumharvest" namespace
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
        // Create a Deferred Register to hold Items which will all be registered under
        // the "elysiumharvest" namespace
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
        // Create a Deferred Register to hold CreativeModeTabs which will all be
        // registered under the "elysiumharvest" namespace
        public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
                        .create(Registries.CREATIVE_MODE_TAB, MODID);
        // Create a Deferred Register to hold MobEffects which will all be registered
        // under
        // the "elysiumharvest" namespace
        public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister
                        .create(Registries.MOB_EFFECT, MODID);

        // Florite Tool Material - based on documentation
        public static final ToolMaterial FLORITE_TOOL_MATERIAL = new ToolMaterial(
                        BlockTags.INCORRECT_FOR_IRON_TOOL, // Same mining level as iron
                        500, // Durability (iron is 250)
                        6.5f, // Mining speed (iron is 6)
                        2.5f, // Attack damage bonus (iron is 2)
                        14, // Enchantability
                        ItemTags.IRON_TOOL_MATERIALS // Repair ingredient tag
        );

        // Florite Armor Material - based on documentation
        public static final ArmorMaterial FLORITE_ARMOR_MATERIAL = new ArmorMaterial(
                        15, // Durability multiplier (iron uses 15)
                        Util.make(new EnumMap<>(ArmorType.class), map -> {
                                map.put(ArmorType.BOOTS, 2);
                                map.put(ArmorType.LEGGINGS, 5);
                                map.put(ArmorType.CHESTPLATE, 6);
                                map.put(ArmorType.HELMET, 2);
                                map.put(ArmorType.BODY, 5);
                        }),
                        14, // Enchantability
                        SoundEvents.ARMOR_EQUIP_IRON,
                        0.0f, // Toughness
                        0.0f, // Knockback resistance
                        ItemTags.IRON_TOOL_MATERIALS, // Repair ingredient tag
                        ResourceKey.create(EquipmentAssets.ROOT_ID,
                                        ResourceLocation.fromNamespaceAndPath(MODID, "florite")));

        // Creates a new Block with the id "elysiumharvest:example_block", combining the
        // namespace and path
        public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block",
                        BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
        // Creates a new BlockItem with the id "elysiumharvest:example_block", combining
        // the namespace and path
        public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block",
                        EXAMPLE_BLOCK);

        // Creates a new food item with the id "elysiumharvest:example_id", nutrition 1
        // and saturation 2
        public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item",
                        new Item.Properties().food(new FoodProperties.Builder()
                                        .alwaysEdible().nutrition(1).saturationModifier(2f).build()));
        // Deepslate Florite Ore Block - drops florite items when mined
        public static final DeferredBlock<Block> DEEPSLATE_FLORITE_ORE = BLOCKS.registerSimpleBlock(
                        "deepslate_florite_ore",
                        BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DEEPSLATE)
                                        .requiresCorrectToolForDrops()
                                        .strength(4.5f, 3.0f)
                                        .sound(SoundType.DEEPSLATE)); // Florite Block - crafted from 9 florite items
        public static final DeferredBlock<Block> FLORITE_BLOCK = BLOCKS.registerSimpleBlock("florite_block",
                        BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.METAL)
                                        .requiresCorrectToolForDrops()
                                        .strength(5.0f, 6.0f));

        // Florite Item - drops from deepslate florite ore
        public static final DeferredItem<Item> FLORITE = ITEMS.registerSimpleItem("florite");

        // Florite Ingot - smelted from florite
        public static final DeferredItem<Item> FLORITE_INGOT = ITEMS.registerSimpleItem("florite_ingot");

        // Florite Armor - humanoid armor based on documentation
        public static final DeferredItem<Item> FLORITE_HELMET = ITEMS.registerItem("florite_helmet",
                        props -> new Item(props.humanoidArmor(FLORITE_ARMOR_MATERIAL, ArmorType.HELMET)));
        public static final DeferredItem<Item> FLORITE_CHESTPLATE = ITEMS.registerItem("florite_chestplate",
                        props -> new Item(props.humanoidArmor(FLORITE_ARMOR_MATERIAL, ArmorType.CHESTPLATE)));
        public static final DeferredItem<Item> FLORITE_LEGGINGS = ITEMS.registerItem("florite_leggings",
                        props -> new Item(props.humanoidArmor(FLORITE_ARMOR_MATERIAL, ArmorType.LEGGINGS)));
        public static final DeferredItem<Item> FLORITE_BOOTS = ITEMS.registerItem("florite_boots",
                        props -> new Item(props.humanoidArmor(FLORITE_ARMOR_MATERIAL, ArmorType.BOOTS)));

        // Florite Set Bonus Effect - визуальный эффект для полного сета брони
        public static final DeferredHolder<MobEffect, MobEffect> FLORITE_SET_BONUS_EFFECT = MOB_EFFECTS.register(
                        "florite_set_bonus", FloriteSetBonusEffect::new);

        // Florite Tools - using custom classes with ItemAbility support
        public static final DeferredItem<Item> FLORITE_SWORD = ITEMS.registerItem("florite_sword",
                        props -> new FloriteSwordItem(props.sword(FLORITE_TOOL_MATERIAL, 3, -2.4f)));
        public static final DeferredItem<Item> FLORITE_PICKAXE = ITEMS.registerItem("florite_pickaxe",
                        props -> new FloritePickaxeItem(props.pickaxe(FLORITE_TOOL_MATERIAL, 1, -2.8f)));
        public static final DeferredItem<Item> FLORITE_AXE = ITEMS.registerItem("florite_axe",
                        props -> new FloriteAxeItem(props.axe(FLORITE_TOOL_MATERIAL, 6.0f, -3.1f)));
        public static final DeferredItem<Item> FLORITE_SHOVEL = ITEMS.registerItem("florite_shovel",
                        props -> new FloriteShovelItem(props.shovel(FLORITE_TOOL_MATERIAL, 1.5f, -3.0f)));
        public static final DeferredItem<Item> FLORITE_HOE = ITEMS.registerItem("florite_hoe",
                        props -> new FloriteHoeItem(props.hoe(FLORITE_TOOL_MATERIAL, -1.0f, -3.0f)));

        // Block Items
        public static final DeferredItem<BlockItem> DEEPSLATE_FLORITE_ORE_ITEM = ITEMS.registerSimpleBlockItem(
                        "deepslate_florite_ore",
                        DEEPSLATE_FLORITE_ORE);
        public static final DeferredItem<BlockItem> FLORITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("florite_block",
                        FLORITE_BLOCK);

        // Creates a creative tab with the id "elysiumharvest:example_tab" for the
        // example item, that is placed after the combat tab
        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS
                        .register("example_tab", () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.elysiumharvest")) // The language key
                                                                                                   // for the title of
                                                                                                   // your
                                                                                                   // CreativeModeTab
                                        .withTabsBefore(CreativeModeTabs.COMBAT)
                                        .icon(() -> FLORITE.get().getDefaultInstance())
                                        .displayItems((parameters, output) -> {
                                                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab.
                                                                                   // For your own tabs, this
                                                                                   // method is preferred over the event
                                                output.accept(FLORITE.get());
                                                output.accept(FLORITE_INGOT.get());
                                                output.accept(FLORITE_SWORD.get());
                                                output.accept(FLORITE_PICKAXE.get());
                                                output.accept(FLORITE_AXE.get());
                                                output.accept(FLORITE_SHOVEL.get());
                                                output.accept(FLORITE_HOE.get());
                                                output.accept(FLORITE_HELMET.get());
                                                output.accept(FLORITE_CHESTPLATE.get());
                                                output.accept(FLORITE_LEGGINGS.get());
                                                output.accept(FLORITE_BOOTS.get());
                                                output.accept(DEEPSLATE_FLORITE_ORE_ITEM.get());
                                                output.accept(FLORITE_BLOCK_ITEM.get());
                                        }).build());

        // The constructor for the mod class is the first code that is run when your mod
        // is loaded.
        // FML will recognize some parameter types like IEventBus or ModContainer and
        // pass them in automatically.
        public ElysiumHarvest(IEventBus modEventBus, ModContainer modContainer) {
                // Register the commonSetup method for modloading
                modEventBus.addListener(this::commonSetup);
                // Register the Deferred Register to the mod event bus so blocks get registered
                BLOCKS.register(modEventBus);
                // Register the Deferred Register to the mod event bus so items get registered
                ITEMS.register(modEventBus);
                // Register the Deferred Register to the mod event bus so tabs get registered
                CREATIVE_MODE_TABS.register(modEventBus);
                // Register the Deferred Register to the mod event bus so mob effects get
                // registered
                MOB_EFFECTS.register(modEventBus);

                // Register ourselves for server and other game events we are interested in.
                // Note that this is necessary if and only if we want *this* class
                // (ElysiumHarvest) to respond directly to events.
                // Do not add this line if there are no @SubscribeEvent-annotated functions in
                // this class, like onServerStarting() below.
                NeoForge.EVENT_BUS.register(this);

                // Register the item to a creative tab
                modEventBus.addListener(this::addCreative);

                // Register our mod's ModConfigSpec so that FML can create and load the config
                // file for us
                modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        }

        private void commonSetup(FMLCommonSetupEvent event) {
                // Some common setup code
                LOGGER.info("HELLO FROM COMMON SETUP");

                if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
                        LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
                }

                LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

                Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
        }

        // Add the example block item to the building blocks tab
        private void addCreative(BuildCreativeModeTabContentsEvent event) {
                if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
                        event.accept(EXAMPLE_BLOCK_ITEM);
                        event.accept(FLORITE_BLOCK_ITEM);
                }

                if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
                        event.accept(DEEPSLATE_FLORITE_ORE_ITEM);
                }

                if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                        event.accept(FLORITE);
                        event.accept(FLORITE_INGOT);
                }
        }

        // You can use SubscribeEvent and let the Event Bus discover methods to call
        @SubscribeEvent
        public void onServerStarting(ServerStartingEvent event) {
                // Do something when the server starts
                LOGGER.info("HELLO from server starting");
        }
}
