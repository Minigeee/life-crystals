package minigee.life_crystals.items;

import minigee.life_crystals.Config;
import minigee.life_crystals.HealthState;
import minigee.life_crystals.LifeCrystals;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class LifeCrystal extends Item {
	public LifeCrystal(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		final var stack = user.getStackInHand(hand);
		if (world.isClient)
			return TypedActionResult.fail(stack);

		// Get health state
		final HealthState state = HealthState.getServerState(world.getServer());
		int maxHealth = state.getMaxHealth(user);

		// Check if at max allowable health
		if (maxHealth >= Config.DATA.maxHealth) {
			user.sendMessage(Text.translatable("life_crystals.limit_reached").formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}

		// Play sound
		user.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);

		// Decrement stack
		stack.decrement(1);

		// Increase hearts by adding an attribute modifier (so it doesn't interfere with
		// other max health modifiers hopefully)
		final var attr = user.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

		// Remove all modifiers from this mod
		final var modifiers = attr.getModifiers();
		modifiers.forEach((modifier) -> {
			if (modifier.getName().equals(LifeCrystals.HEALTH_MODIFIER_NAME))
				attr.removeModifier(modifier.getId());
		});

		// Apply modifier as the difference between the target max health and the
		// default max health
		maxHealth += Config.DATA.healthIncrement;
		attr.addPersistentModifier(
				new EntityAttributeModifier(LifeCrystals.HEALTH_MODIFIER_NAME, maxHealth - 20, Operation.ADDITION));

		// Save state
		state.maxHealth.put(user.getUuid(), maxHealth);
		state.markDirty();

		return TypedActionResult.success(stack);
	}
}
