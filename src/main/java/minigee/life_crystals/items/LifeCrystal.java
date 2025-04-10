package minigee.life_crystals.items;

import minigee.life_crystals.Config;
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
			
		// Increase hearts by adding an attribute modifier (so it doesn't interfere with
		// other max health modifiers hopefully)
		final var attr = user.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

		// Get max health (use default base health if no modifier exists)
		boolean hasModifier = attr.hasModifier(LifeCrystals.HEALTH_MODIFIER_ID);
		int maxHealth = hasModifier ? (int) attr.getModifier(LifeCrystals.HEALTH_MODIFIER_ID).value() + 20 : Config.DATA.baseHealth;

		// Check if at max allowable health
		if (maxHealth >= Config.DATA.maxHealth) {
			user.sendMessage(Text.translatable("life_crystals.limit_reached").formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}

		// Play sound
		user.playSoundToPlayer(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);

		// Decrement stack
		stack.decrement(1);
		
		// Increment max health
		int increment = Math.min(Config.DATA.healthIncrement, Config.DATA.maxHealth - maxHealth);
		maxHealth += increment;
		// Calculate modifier value
		int modifierValue = maxHealth - 20;

		// Apply new modifier value
		EntityAttributeModifier modifier = new EntityAttributeModifier(LifeCrystals.HEALTH_MODIFIER_ID, modifierValue, Operation.ADD_VALUE);
		attr.overwritePersistentModifier(modifier);

		return TypedActionResult.success(stack);
	}
}
