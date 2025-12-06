package net.vergusha.elysiumharvest;

import java.util.EnumMap;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.Util;
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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.vergusha.elysiumharvest.block.ModCropBlock;
import net.vergusha.elysiumharvest.block.QazanBlock;
import net.vergusha.elysiumharvest.block.TallCropBlock;
import net.vergusha.elysiumharvest.blockentity.QazanBlockEntity;
import net.vergusha.elysiumharvest.effect.FloriteSetBonusEffect;
import net.vergusha.elysiumharvest.item.FloriteAxeItem;
import net.vergusha.elysiumharvest.item.FloriteHoeItem;
import net.vergusha.elysiumharvest.item.FloritePickaxeItem;
import net.vergusha.elysiumharvest.item.FloriteShovelItem;
import net.vergusha.elysiumharvest.item.FloriteSwordItem;
import net.vergusha.elysiumharvest.item.HarvestDrinkItem;
import net.vergusha.elysiumharvest.item.HarvestStewItem;
import net.vergusha.elysiumharvest.menu.QazanMenu;
import net.vergusha.elysiumharvest.recipe.QazanRecipe;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ElysiumHarvest.MODID)
public class ElysiumHarvest {

        public static final String MODID = "elysiumharvest";
        public static final Logger LOGGER = LogUtils.getLogger();
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
        public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
                        .create(Registries.CREATIVE_MODE_TAB, MODID);
        public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister
                        .create(Registries.MOB_EFFECT, MODID);
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(Registries.BLOCK_ENTITY_TYPE, MODID);
        public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister
                        .create(Registries.MENU, MODID);
        public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
                        .create(Registries.RECIPE_TYPE, MODID);
        public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
                        .create(Registries.RECIPE_SERIALIZER, MODID);

        // Флоритовый материал
        public static final ToolMaterial FLORITE_TOOL_MATERIAL = new ToolMaterial(
                        BlockTags.INCORRECT_FOR_IRON_TOOL, // Same mining level as iron
                        500, // Прочность
                        13.0f, // Скорость добычи
                        2.5f, // Урон от аттаки
                        14, // Enchantability
                        ItemTags.IRON_TOOL_MATERIALS // Repair ingredient tag
        );

        // Флоритовый материал брони
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

        // Блоки
        public static final DeferredBlock<Block> DEEPSLATE_FLORITE_ORE = BLOCKS.registerSimpleBlock(
                        "deepslate_florite_ore",
                        BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DEEPSLATE)
                                        .requiresCorrectToolForDrops()
                                        .strength(4.5f, 3.0f)
                                        .sound(SoundType.DEEPSLATE));
        public static final DeferredBlock<Block> RAW_FLORITE_BLOCK = BLOCKS.registerSimpleBlock("raw_florite_block",
                        BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.RAW_IRON)
                                        .requiresCorrectToolForDrops()
                                        .strength(5.0f, 6.0f)
                                        .sound(SoundType.STONE));
        public static final DeferredBlock<Block> FLORITE_BLOCK = BLOCKS.registerSimpleBlock("florite_block",
                        BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.METAL)
                                        .requiresCorrectToolForDrops()
                                        .strength(5.0f, 6.0f)
                                        .sound(SoundType.METAL));
        public static final DeferredBlock<QazanBlock> QAZAN = BLOCKS.registerBlock("qazan",
                        QazanBlock::new,
                        BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.METAL)
                                        .strength(3.0f, 6.0f)
                                        .sound(SoundType.METAL)
                                        .requiresCorrectToolForDrops()
                                        .noOcclusion()
                                        .isViewBlocking((state, level, pos) -> false)
                                        .isRedstoneConductor((state, level, pos) -> false)
                                        .isSuffocating((state, level, pos) -> false));

        // ===== КУЛЬТУРЫ (ОВОЩИ/ФРУКТЫ) - ПРЕДМЕТЫ =====
        // Томаты
        public static final DeferredItem<Item> TOMATO = ITEMS.registerItem("tomato",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(3).saturationModifier(0.4f).build())));
        public static final DeferredItem<Item> TOMATO_SEEDS = ITEMS.registerItem("tomato_seeds", Item::new);
        // Лук (семена и урожай - один предмет)
        public static final DeferredItem<Item> ONION = ITEMS.registerItem("onion",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build())));
        // Огурец
        public static final DeferredItem<Item> CUCUMBER = ITEMS.registerItem("cucumber",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(3).saturationModifier(0.3f).build())));
        public static final DeferredItem<Item> CUCUMBER_SEEDS = ITEMS.registerItem("cucumber_seeds", Item::new);
        // Капуста
        public static final DeferredItem<Item> CABBAGE = ITEMS.registerItem("cabbage",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f).build())));
        public static final DeferredItem<Item> CABBAGE_SEEDS = ITEMS.registerItem("cabbage_seeds", Item::new);
        // Чеснок (семена и урожай - один предмет)
        public static final DeferredItem<Item> GARLIC = ITEMS.registerItem("garlic",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(2).saturationModifier(0.2f).build())));
        // Болгарский перец
        public static final DeferredItem<Item> BELL_PEPPER = ITEMS.registerItem("bell_pepper",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(3).saturationModifier(0.4f).build())));
        public static final DeferredItem<Item> BELL_PEPPER_SEEDS = ITEMS.registerItem("bell_pepper_seeds", Item::new);
        // Баклажан
        public static final DeferredItem<Item> EGGPLANT = ITEMS.registerItem("eggplant",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(3).saturationModifier(0.4f).build())));
        public static final DeferredItem<Item> EGGPLANT_SEEDS = ITEMS.registerItem("eggplant_seeds", Item::new);
        // Кукуруза
        public static final DeferredItem<Item> CORN = ITEMS.registerItem("corn",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f).build())));
        public static final DeferredItem<Item> CORN_SEEDS = ITEMS.registerItem("corn_seeds", Item::new);
        // Брокколи
        public static final DeferredItem<Item> BROCCOLI = ITEMS.registerItem("broccoli",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f).build())));
        public static final DeferredItem<Item> BROCCOLI_SEEDS = ITEMS.registerItem("broccoli_seeds", Item::new);
        // Салат
        public static final DeferredItem<Item> LETTUCE = ITEMS.registerItem("lettuce",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build())));
        public static final DeferredItem<Item> LETTUCE_SEEDS = ITEMS.registerItem("lettuce_seeds", Item::new);
        // Имбирь (семена и урожай - один предмет)
        public static final DeferredItem<Item> GINGER = ITEMS.registerItem("ginger",
                        props -> new Item(props.food(
                                        new FoodProperties.Builder().nutrition(2).saturationModifier(0.2f).build())));

        // ===== ГОТОВЫЕ БЛЮДА ДЛЯ КАЗАНА =====
        // Овощной суп
        public static final DeferredItem<Item> VEGETABLE_SOUP = ITEMS.registerItem("vegetable_soup",
                        props -> new HarvestStewItem(props.stacksTo(16).craftRemainder(Items.BOWL)
                                        .food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.9f)
                                                        .build())));
        // Борщ
        public static final DeferredItem<Item> BORSCHT = ITEMS.registerItem("borscht",
                        props -> new HarvestStewItem(props.stacksTo(16).craftRemainder(Items.BOWL)
                                        .food(new FoodProperties.Builder().nutrition(10).saturationModifier(1.2f)
                                                        .build())));
        // Рагу
        public static final DeferredItem<Item> STEW = ITEMS.registerItem("stew",
                        props -> new HarvestStewItem(props.stacksTo(16).craftRemainder(Items.BOWL)
                                        .food(new FoodProperties.Builder().nutrition(9).saturationModifier(1.1f)
                                                        .build())));
        // Грибной суп
        public static final DeferredItem<Item> MUSHROOM_STEW_UPGRADED = ITEMS.registerItem("mushroom_stew_upgraded",
                        props -> new HarvestStewItem(props.stacksTo(16).craftRemainder(Items.BOWL)
                                        .food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.0f)
                                                        .build())));
        // Кукурузная похлёбка
        public static final DeferredItem<Item> CORN_SOUP = ITEMS.registerItem("corn_soup",
                        props -> new HarvestStewItem(props.stacksTo(16).craftRemainder(Items.BOWL)
                                        .food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f)
                                                        .build())));
        // Салат
        public static final DeferredItem<Item> SALAD = ITEMS.registerItem("salad",
                        props -> new HarvestStewItem(props.stacksTo(16).craftRemainder(Items.BOWL)
                                        .food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.7f)
                                                        .build())));
        // Имбирный чай
        public static final DeferredItem<Item> GINGER_TEA = ITEMS.registerItem("ginger_tea",
                        props -> new HarvestDrinkItem(props.stacksTo(16).craftRemainder(Items.GLASS_BOTTLE)
                                        .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f)
                                                        .build())));

        // Свойства для культур
        private static BlockBehaviour.Properties cropProperties() {
                return BlockBehaviour.Properties.of()
                                .mapColor(MapColor.PLANT)
                                .noCollision()
                                .randomTicks()
                                .instabreak()
                                .sound(SoundType.CROP)
                                .pushReaction(PushReaction.DESTROY);
        }

        // ===== КУЛЬТУРЫ (CROP BLOCKS) =====
        // Томаты
        public static final DeferredBlock<ModCropBlock> TOMATO_CROP = BLOCKS.registerBlock("tomato_crop",
                        props -> new ModCropBlock(props, () -> TOMATO_SEEDS.get()), cropProperties());
        // Лук
        public static final DeferredBlock<ModCropBlock> ONION_CROP = BLOCKS.registerBlock("onion_crop",
                        props -> new ModCropBlock(props, () -> ONION.get()), cropProperties());
        // Огурец
        public static final DeferredBlock<ModCropBlock> CUCUMBER_CROP = BLOCKS.registerBlock("cucumber_crop",
                        props -> new ModCropBlock(props, () -> CUCUMBER_SEEDS.get()), cropProperties());
        // Капуста
        public static final DeferredBlock<ModCropBlock> CABBAGE_CROP = BLOCKS.registerBlock("cabbage_crop",
                        props -> new ModCropBlock(props, () -> CABBAGE_SEEDS.get()), cropProperties());
        // Чеснок
        public static final DeferredBlock<ModCropBlock> GARLIC_CROP = BLOCKS.registerBlock("garlic_crop",
                        props -> new ModCropBlock(props, () -> GARLIC.get()), cropProperties());
        // Болгарский перец
        public static final DeferredBlock<ModCropBlock> BELL_PEPPER_CROP = BLOCKS.registerBlock("bell_pepper_crop",
                        props -> new ModCropBlock(props, () -> BELL_PEPPER_SEEDS.get()), cropProperties());
        // Баклажан
        public static final DeferredBlock<ModCropBlock> EGGPLANT_CROP = BLOCKS.registerBlock("eggplant_crop",
                        props -> new ModCropBlock(props, () -> EGGPLANT_SEEDS.get()), cropProperties());
        // Кукуруза (высокий блок)
        public static final DeferredBlock<TallCropBlock> CORN_CROP = BLOCKS.registerBlock("corn_crop",
                        props -> new TallCropBlock(props, () -> CORN_SEEDS.get()), cropProperties());
        // Брокколи
        public static final DeferredBlock<ModCropBlock> BROCCOLI_CROP = BLOCKS.registerBlock("broccoli_crop",
                        props -> new ModCropBlock(props, () -> BROCCOLI_SEEDS.get()), cropProperties());
        // Салат
        public static final DeferredBlock<ModCropBlock> LETTUCE_CROP = BLOCKS.registerBlock("lettuce_crop",
                        props -> new ModCropBlock(props, () -> LETTUCE_SEEDS.get()), cropProperties());
        // Имбирь
        public static final DeferredBlock<ModCropBlock> GINGER_CROP = BLOCKS.registerBlock("ginger_crop",
                        props -> new ModCropBlock(props, () -> GINGER.get()), cropProperties());

        // Флорит (руда)
        public static final DeferredItem<Item> FLORITE = ITEMS.registerSimpleItem("florite");
        // Флорит (слиток)
        public static final DeferredItem<Item> FLORITE_INGOT = ITEMS.registerSimpleItem("florite_ingot");
        // Вишня
        public static final DeferredItem<Item> CHERRY = ITEMS.registerItem("cherry",
                        props -> new Item(props.food(new FoodProperties.Builder()
                                        .nutrition(5)
                                        .saturationModifier(1.0f)
                                        .build())));
        // Урожайная похлебка
        public static final DeferredItem<Item> HARVEST_STEW = ITEMS.registerItem("harvest_stew",
                        props -> new HarvestStewItem(props
                                        .stacksTo(32)
                                        .craftRemainder(Items.BOWL)
                                        .food(new FoodProperties.Builder()
                                                        .nutrition(8)
                                                        .saturationModifier(1.2f)
                                                        .build())));

        // Флоритовая броня
        public static final DeferredItem<Item> FLORITE_HELMET = ITEMS.registerItem("florite_helmet",
                        props -> new Item(props.humanoidArmor(FLORITE_ARMOR_MATERIAL, ArmorType.HELMET)));
        public static final DeferredItem<Item> FLORITE_CHESTPLATE = ITEMS.registerItem("florite_chestplate",
                        props -> new Item(props.humanoidArmor(FLORITE_ARMOR_MATERIAL, ArmorType.CHESTPLATE)));
        public static final DeferredItem<Item> FLORITE_LEGGINGS = ITEMS.registerItem("florite_leggings",
                        props -> new Item(props.humanoidArmor(FLORITE_ARMOR_MATERIAL, ArmorType.LEGGINGS)));
        public static final DeferredItem<Item> FLORITE_BOOTS = ITEMS.registerItem("florite_boots",
                        props -> new Item(props.humanoidArmor(FLORITE_ARMOR_MATERIAL, ArmorType.BOOTS)));

        // Эффект брони
        public static final DeferredHolder<MobEffect, MobEffect> FLORITE_SET_BONUS_EFFECT = MOB_EFFECTS.register(
                        "florite_set_bonus", FloriteSetBonusEffect::new);

        // Флоритовые инструменты
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

        // Блоки (предметы)
        public static final DeferredItem<BlockItem> DEEPSLATE_FLORITE_ORE_ITEM = ITEMS.registerSimpleBlockItem(
                        "deepslate_florite_ore",
                        DEEPSLATE_FLORITE_ORE);
        public static final DeferredItem<BlockItem> RAW_FLORITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
                        "raw_florite_block",
                        RAW_FLORITE_BLOCK);
        public static final DeferredItem<BlockItem> FLORITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("florite_block",
                        FLORITE_BLOCK);
        public static final DeferredItem<BlockItem> QAZAN_ITEM = ITEMS.registerSimpleBlockItem("qazan", QAZAN);

        // BlockEntity
        // TODO: Fix for 1.21.10 - BlockEntityType.Builder doesn't exist
        // Using direct constructor with Set.of() for valid blocks
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QazanBlockEntity>> QAZAN_BLOCK_ENTITY = BLOCK_ENTITIES
                        .register("qazan", () -> new BlockEntityType<>(
                                        QazanBlockEntity::new,
                                        java.util.Set.of(QAZAN.get())));

        // Menu
        public static final DeferredHolder<MenuType<?>, MenuType<QazanMenu>> QAZAN_MENU = MENUS.register("qazan",
                        () -> new MenuType<>(QazanMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

        // Recipe Type
        public static final DeferredHolder<RecipeType<?>, RecipeType<QazanRecipe>> QAZAN_RECIPE_TYPE = RECIPE_TYPES
                        .register("qazan_cooking", () -> new RecipeType<QazanRecipe>() {
                                @Override
                                public String toString() {
                                        return "qazan_cooking";
                                }
                        });

        // Recipe Serializer
        public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<QazanRecipe>> QAZAN_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
                        .register("qazan_cooking", QazanRecipe.Serializer::new);

        // Креатив меню
        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FLORITE_TAB = CREATIVE_MODE_TABS
                        .register("florite_tab", () -> CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup.elysiumharvest"))
                                        .withTabsBefore(CreativeModeTabs.COMBAT)
                                        .icon(() -> FLORITE.get().getDefaultInstance())
                                        .displayItems((parameters, output) -> {
                                                // Флорит и руда
                                                output.accept(FLORITE.get());
                                                output.accept(FLORITE_INGOT.get());
                                                output.accept(DEEPSLATE_FLORITE_ORE_ITEM.get());
                                                output.accept(RAW_FLORITE_BLOCK_ITEM.get());
                                                output.accept(FLORITE_BLOCK_ITEM.get());

                                                // Инструменты и броня
                                                output.accept(FLORITE_SWORD.get());
                                                output.accept(FLORITE_PICKAXE.get());
                                                output.accept(FLORITE_AXE.get());
                                                output.accept(FLORITE_SHOVEL.get());
                                                output.accept(FLORITE_HOE.get());
                                                output.accept(FLORITE_HELMET.get());
                                                output.accept(FLORITE_CHESTPLATE.get());
                                                output.accept(FLORITE_LEGGINGS.get());
                                                output.accept(FLORITE_BOOTS.get());

                                                // Казан
                                                output.accept(QAZAN_ITEM.get());

                                                // Культуры - семена
                                                output.accept(TOMATO_SEEDS.get());
                                                output.accept(CUCUMBER_SEEDS.get());
                                                output.accept(CABBAGE_SEEDS.get());
                                                output.accept(BELL_PEPPER_SEEDS.get());
                                                output.accept(EGGPLANT_SEEDS.get());
                                                output.accept(CORN_SEEDS.get());
                                                output.accept(BROCCOLI_SEEDS.get());
                                                output.accept(LETTUCE_SEEDS.get());

                                                // Культуры - овощи (семена и урожай - одно)
                                                output.accept(ONION.get());
                                                output.accept(GARLIC.get());
                                                output.accept(GINGER.get());

                                                // Культуры - урожай
                                                output.accept(TOMATO.get());
                                                output.accept(CUCUMBER.get());
                                                output.accept(CABBAGE.get());
                                                output.accept(BELL_PEPPER.get());
                                                output.accept(EGGPLANT.get());
                                                output.accept(CORN.get());
                                                output.accept(BROCCOLI.get());
                                                output.accept(LETTUCE.get());

                                                // Еда
                                                output.accept(CHERRY.get());
                                                output.accept(HARVEST_STEW.get());
                                                output.accept(VEGETABLE_SOUP.get());
                                                output.accept(BORSCHT.get());
                                                output.accept(STEW.get());
                                                output.accept(MUSHROOM_STEW_UPGRADED.get());
                                                output.accept(CORN_SOUP.get());
                                                output.accept(SALAD.get());
                                                output.accept(GINGER_TEA.get());
                                        }).build());

        //
        public ElysiumHarvest(IEventBus modEventBus, ModContainer modContainer) {
                modEventBus.addListener(this::commonSetup);
                BLOCKS.register(modEventBus);
                ITEMS.register(modEventBus);
                CREATIVE_MODE_TABS.register(modEventBus);
                MOB_EFFECTS.register(modEventBus);
                BLOCK_ENTITIES.register(modEventBus);
                MENUS.register(modEventBus);
                RECIPE_TYPES.register(modEventBus);
                RECIPE_SERIALIZERS.register(modEventBus);
                NeoForge.EVENT_BUS.register(this);
                modEventBus.addListener(this::addCreative);
                modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        }

        private void commonSetup(FMLCommonSetupEvent event) {
                LOGGER.info("HELLO FROM COMMON SETUP");
                if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
                        LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
                }

                LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

                Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
        }

        private void addCreative(BuildCreativeModeTabContentsEvent event) {
                if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
                        event.accept(RAW_FLORITE_BLOCK_ITEM);
                        event.accept(FLORITE_BLOCK_ITEM);
                        event.accept(QAZAN_ITEM);
                }

                if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
                        event.accept(DEEPSLATE_FLORITE_ORE_ITEM);
                        event.accept(RAW_FLORITE_BLOCK_ITEM);
                        event.accept(QAZAN_ITEM);
                }

                if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
                        event.accept(CHERRY);
                        // Новые овощи
                        event.accept(TOMATO);
                        event.accept(ONION);
                        event.accept(CUCUMBER);
                        event.accept(CABBAGE);
                        event.accept(GARLIC);
                        event.accept(BELL_PEPPER);
                        event.accept(EGGPLANT);
                        event.accept(CORN);
                        event.accept(BROCCOLI);
                        event.accept(LETTUCE);
                        event.accept(GINGER);
                        // Готовые блюда
                        event.accept(HARVEST_STEW);
                        event.accept(VEGETABLE_SOUP);
                        event.accept(BORSCHT);
                        event.accept(STEW);
                        event.accept(MUSHROOM_STEW_UPGRADED);
                        event.accept(CORN_SOUP);
                        event.accept(SALAD);
                        event.accept(GINGER_TEA);
                }

                if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                        event.accept(FLORITE);
                        event.accept(FLORITE_INGOT);
                        // Семена
                        event.accept(TOMATO_SEEDS);
                        event.accept(CUCUMBER_SEEDS);
                        event.accept(CABBAGE_SEEDS);
                        event.accept(BELL_PEPPER_SEEDS);
                        event.accept(EGGPLANT_SEEDS);
                        event.accept(CORN_SEEDS);
                        event.accept(BROCCOLI_SEEDS);
                        event.accept(LETTUCE_SEEDS);
                }
        }

        @SubscribeEvent
        public void onServerStarting(ServerStartingEvent event) {
                // Do something when the server starts
                LOGGER.info("HELLO from server starting");
        }
}
