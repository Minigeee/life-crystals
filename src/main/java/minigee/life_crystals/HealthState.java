package minigee.life_crystals;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class HealthState extends PersistentState {
	/** Persistent state id */
	public static final String ID = LifeCrystals.MOD_ID + ":health_state";

	/** Map of max health */
	public HashMap<UUID, Integer> maxHealth = new HashMap<>();

	private static Type<HealthState> STATE_MGR_TYPE = new Type<>(
			HealthState::new,
			HealthState::createFromNbt,
			null);

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		// Create map of max healths
		NbtCompound healthNbt = new NbtCompound();
		maxHealth.forEach((id, maxHealth) -> {
			healthNbt.putInt(id.toString(), maxHealth);
		});

		// Add map
		nbt.put("max_health", healthNbt);

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

		// Read max healths
		NbtCompound healthNbt = nbt.getCompound("max_health");
		healthNbt.getKeys().forEach((playerId) -> {
			state.maxHealth.put(UUID.fromString(playerId), healthNbt.getInt(playerId));
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
	 * Get max health of the given player
	 * 
	 * @param player The player to retrieve max health for
	 * @return The max health of the given player, or the default value if the
	 *         player does not have any data
	 */
	public Integer getMaxHealth(LivingEntity player) {
        // Either get the player by the uuid, or we don't have data for them yet so return default value
        return maxHealth.computeIfAbsent(player.getUuid(), uuid -> Config.DATA.baseHealth);
    }
}
