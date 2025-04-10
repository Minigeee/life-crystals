package minigee.life_crystals;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.List;

import minigee.life_crystals.items.LifeCrystal;

public class LifeCrystals implements ModInitializer {
	/** Mod id */
	public static final String MOD_ID = "life_crystals";
	/** Health modifier id */
	public static final Identifier HEALTH_MODIFIER_ID = Identifier.of(MOD_ID, "health");

	/** Life crystal shard */
	public static final Item LIFE_CRYSTAL_SHARD = new Item(new Item.Settings().rarity(Rarity.UNCOMMON));
	/** Life crystal item */
	public static final LifeCrystal LIFE_CRYSTAL = new LifeCrystal(new Item.Settings().rarity(Rarity.RARE));

	/** Ore block */
	public static final Block LIFE_CRYSTAL_ORE = new Block(AbstractBlock.Settings.create().strength(3.0f, 4.0f));
	/** Deepslate ore block */
	public static final Block DEEPSLATE_LIFE_CRYSTAL_ORE = new Block(AbstractBlock.Settings.create().strength(4.5f, 4.0f));

	// Placed feature registry keys
	public static final RegistryKey<PlacedFeature> LIFE_CRYSTAL_ORE_PLACED_KEY = RegistryKey
			.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "ore_life_crystal"));
	public static final RegistryKey<PlacedFeature> LIFE_CRYSTAL_ORE_BURIED_PLACED_KEY = RegistryKey
			.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "ore_life_crystal_buried"));
	public static final RegistryKey<PlacedFeature> LIFE_CRYSTAL_ORE_LARGE_PLACED_KEY = RegistryKey
			.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "ore_life_crystal_large"));

	/** Item group */
	public static final ItemGroup MAIN_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(LIFE_CRYSTAL))
			.displayName(Text.translatable("item_group.life_crystals.main_group"))
			.entries((context, entries) -> {
				entries.add(LIFE_CRYSTAL_SHARD);
				entries.add(LIFE_CRYSTAL);
				entries.add(LIFE_CRYSTAL_ORE);
				entries.add(DEEPSLATE_LIFE_CRYSTAL_ORE);
			})
			.build();

	// Loot tables
	final List<RegistryKey<LootTable>> LOOT_TABLE_IDS = List.of(
			LootTables.ABANDONED_MINESHAFT_CHEST,
			LootTables.ANCIENT_CITY_CHEST,
			LootTables.BURIED_TREASURE_CHEST,
			LootTables.DESERT_PYRAMID_CHEST,
			LootTables.END_CITY_TREASURE_CHEST,
			LootTables.JUNGLE_TEMPLE_CHEST,
			LootTables.SIMPLE_DUNGEON_CHEST,
			LootTables.WOODLAND_MANSION_CHEST,
			LootTables.TRIAL_CHAMBERS_REWARD_RARE_CHEST,
			LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE_CHEST);

	@Override
	public void onInitialize() {
		// Set up config
		Config.setup();

		// Register items
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "life_crystal"), LIFE_CRYSTAL);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "life_crystal_shard"), LIFE_CRYSTAL_SHARD);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "life_crystal_ore"),
				new BlockItem(LIFE_CRYSTAL_ORE, new Item.Settings()));
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "deepslate_life_crystal_ore"),
				new BlockItem(DEEPSLATE_LIFE_CRYSTAL_ORE, new Item.Settings()));

		// Register blocks
		Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "life_crystal_ore"), LIFE_CRYSTAL_ORE);
		Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "deepslate_life_crystal_ore"),
				DEEPSLATE_LIFE_CRYSTAL_ORE);

		// Register item group
		Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "main_group"), MAIN_GROUP);

		// Register placed keys
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
				LIFE_CRYSTAL_ORE_PLACED_KEY);
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
				LIFE_CRYSTAL_ORE_BURIED_PLACED_KEY);
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
				LIFE_CRYSTAL_ORE_LARGE_PLACED_KEY);

		// Called when new entity is loaded
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			// Only handle player entities
			if (!(entity instanceof PlayerEntity player))
				return;

			var attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

			// Set player to base health if new player (has no modifier)
			if (!attr.hasModifier(HEALTH_MODIFIER_ID)) {
				final var health = Config.DATA.baseHealth;

				EntityAttributeModifier modifier = new EntityAttributeModifier(HEALTH_MODIFIER_ID, health - 20,
						Operation.ADD_VALUE);
				attr.addPersistentModifier(modifier);
				player.setHealth(health);
			}
		});

		// Called when player respawns
		ServerPlayerEvents.COPY_FROM.register(((oldPlayer, newPlayer, arg2) -> {
			var oldAttr = oldPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

			// Set new player modifier
			if (oldAttr.hasModifier(HEALTH_MODIFIER_ID)) {
				final var oldModifier = oldAttr.getModifier(HEALTH_MODIFIER_ID);
				int maxHealth = (int) oldModifier.value() + 20;

				// Use "add" because the new player will not have any attributes
				newPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(oldModifier);
				newPlayer.setHealth(maxHealth);
			}
		}));

		// Add to loot tables
		LootTableEvents.MODIFY.register((key, tableBuilder, source) -> {
			float lootChance = Config.DATA.lootChance;

			if (source.isBuiltin() && Config.DATA.addChestLoot && LOOT_TABLE_IDS.contains(key)) {
				LootPool.Builder poolBuilder = LootPool.builder()
						.rolls(BinomialLootNumberProvider.create(1, lootChance))
						.with(ItemEntry.builder(LIFE_CRYSTAL_SHARD).weight(4)
								.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))).build())
						.with(ItemEntry.builder(LIFE_CRYSTAL).weight(1).build());

				tableBuilder.pool(poolBuilder);
			}
		});
	}
}