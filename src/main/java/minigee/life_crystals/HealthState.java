package minigee.life_crystals;

import java.util.HashMap;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

/** Tracks the amount of health accumulated */
public class HealthState extends PersistentState {
	/** Persistent state id */
	public static final String ID = LifeCrystals.MOD_ID + "_health_state";

	/** Tracks modifier ids */
	public HashMap<UUID, UUID> modifierIds = new HashMap<>();

	private static Type<HealthState> STATE_MGR_TYPE = new Type<>(
			HealthState::new,
			HealthState::createFromNbt,
			null);

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		// Create map of added health
		NbtCompound healthNbt = new NbtCompound();
		modifierIds.forEach((id, modifierId) -> {
			healthNbt.putUuid(id.toString(), modifierId);
		});

		// Add map
		nbt.put("modifier_ids", healthNbt);

		return nbt;
	}

	/**
	 * Create persistent health state from nbt
	 * 
	 * @param nbt The nbt compound the state should be loaded from
	 * @return New persistent state object
	 */
	public static HealthState createFromNbt(NbtCompound nbt) {
		// Create new state
		HealthState state = new HealthState();

		// Read modifier ids
		NbtCompound healthNbt = nbt.getCompound("modifier_ids");
		healthNbt.getKeys().forEach((playerId) -> {
			state.modifierIds.put(UUID.fromString(playerId), healthNbt.getUuid(playerId));
		});

		return state;
	}

	/**
	 * Get health state from server
	 * 
	 * @param server The server to get state from
	 * @return Health state
	 */
	public static HealthState getServerState(MinecraftServer server) {
		PersistentStateManager stateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
		return stateManager.getOrCreate(STATE_MGR_TYPE, ID);
	}

	/**
	 * Get health modifier for the given player
	 * 
	 * @param player The player to retrieve added health for
	 * @return The added health of the given player, or null if the
	 *         player does not have any data
	 */
	@Nullable
	public UUID getModifier(LivingEntity player) {
		// Either get the player by the uuid, or we don't have data for them yet so
		// return default value
		return modifierIds.get(player.getUuid());
	}
}
