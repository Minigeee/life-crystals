package minigee.life_crystals;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import minigee.life_crystals.items.LifeCrystal;

public class LifeCrystals implements ModInitializer {
	/** Mod id */
	public static final String MOD_ID = "life_crystals";
	/** Health modifier name */
	public static final String HEALTH_MODIFIER_NAME = MOD_ID + ":HealthModifier";

	/** Life crystal shard */
	public static final Item LIFE_CRYSTAL_SHARD = new Item(new FabricItemSettings().rarity(Rarity.UNCOMMON));
	/** Life crystal item */
	public static final LifeCrystal LIFE_CRYSTAL = new LifeCrystal(new FabricItemSettings().rarity(Rarity.RARE));

	/** Ore block */
	public static final Block LIFE_CRYSTAL_ORE = new Block(FabricBlockSettings.create().strength(3.0f, 4.0f));
	/** Deepslate ore block */
	public static final Block DEEPSLATE_LIFE_CRYSTAL_ORE = new Block(FabricBlockSettings.create().strength(4.5f, 4.0f));

	// Placed feature registry keys
	public static final RegistryKey<PlacedFeature> LIFE_CRYSTAL_ORE_PLACED_KEY = RegistryKey
			.of(RegistryKeys.PLACED_FEATURE, new Identifier(MOD_ID, "ore_life_crystal"));
	public static final RegistryKey<PlacedFeature> LIFE_CRYSTAL_ORE_BURIED_PLACED_KEY = RegistryKey
			.of(RegistryKeys.PLACED_FEATURE, new Identifier(MOD_ID, "ore_life_crystal_buried"));
	public static final RegistryKey<PlacedFeature> LIFE_CRYSTAL_ORE_LARGE_PLACED_KEY = RegistryKey
			.of(RegistryKeys.PLACED_FEATURE, new Identifier(MOD_ID, "ore_life_crystal_large"));

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

	@Override
	public void onInitialize() {
		// Set up config
		Config.setup();

		// Register items
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "life_crystal"), LIFE_CRYSTAL);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "life_crystal_shard"), LIFE_CRYSTAL_SHARD);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "life_crystal_ore"),
				new BlockItem(LIFE_CRYSTAL_ORE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "deepslate_life_crystal_ore"),
				new BlockItem(DEEPSLATE_LIFE_CRYSTAL_ORE, new FabricItemSettings()));

		// Register blocks
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "life_crystal_ore"), LIFE_CRYSTAL_ORE);
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "deepslate_life_crystal_ore"),
				DEEPSLATE_LIFE_CRYSTAL_ORE);

		// Register item group
		Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "main_group"), MAIN_GROUP);

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

			// Get state
			final HealthState state = HealthState.getServerState(world.getServer());

			// Set player to base health if new player
			if (!state.modifierIds.containsKey(player.getUuid())) {
				final var health = Config.DATA.baseHealth();

				EntityAttributeModifier modifier = new EntityAttributeModifier(HEALTH_MODIFIER_NAME, health - 20,
						Operation.ADDITION);
				player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(modifier);
				player.setHealth(health);

				// Save state
				state.modifierIds.put(player.getUuid(), modifier.getId());
				state.markDirty();
			}
		});

		// Called when player respawns
		ServerPlayerEvents.COPY_FROM.register(((oldPlayer, newPlayer, arg2) -> {
			// Get old modifier
			final HealthState state = HealthState.getServerState(oldPlayer.getServer());
			final var oldModifierId = state.modifierIds.get(oldPlayer.getUuid());

			// Set new player modifier
			if (oldModifierId != null) {
				final var oldModifier = oldPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
						.getModifier(oldModifierId);
				int maxHealth = (int) oldModifier.getValue() + 20;

				newPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(oldModifier);
				newPlayer.setHealth(maxHealth);
			}
		}));
	}
}