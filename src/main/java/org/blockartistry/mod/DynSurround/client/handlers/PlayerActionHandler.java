package org.blockartistry.mod.DynSurround.client.handlers;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundEngine;
import org.blockartistry.mod.DynSurround.client.sound.Sounds;
import org.blockartistry.mod.DynSurround.registry.ArmorClass;
import org.blockartistry.mod.DynSurround.registry.ItemRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayerActionHandler extends EffectHandlerBase {

	private ItemRegistry itemRegistry;

	@Override
	public String getHandlerName() {
		return "PlayerActionHandler";
	}

	private boolean lastHeld = false;
	private int lastSlot = -1;
	private String soundId = null;

	protected boolean triggerNewEquipSound(@Nonnull final EntityPlayer player) {
		if (this.lastSlot != player.inventory.currentItem)
			return true;

		return this.lastHeld != (player.getHeldItemMainhand() != null);
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		// Handle item equip sounds
		if (!ModOptions.enableEquipSound || !triggerNewEquipSound(player))
			return;

		final ItemStack currentStack = player.getHeldItemMainhand();

		SoundEngine.instance().stopSound(this.soundId, SoundCategory.PLAYERS);

		if (currentStack != null) {
			final SoundEffect sound;
			if (this.itemRegistry.doSwordSound(currentStack))
				sound = Sounds.SWORD_EQUIP;
			else if (this.itemRegistry.doAxeSound(currentStack))
				sound = Sounds.AXE_EQUIP;
			else if (this.itemRegistry.doToolSound(currentStack))
				sound = Sounds.TOOL_EQUIP;
			else if (this.itemRegistry.doBowSound(currentStack))
				sound = Sounds.BOW_EQUIP;
			else {
				final ArmorClass armor = this.itemRegistry.getArmorClass(currentStack);
				switch (armor) {
				case LIGHT:
					sound = Sounds.LIGHT_ARMOR_EQUIP;
					break;
				case MEDIUM:
					sound = Sounds.MEDIUM_ARMOR_EQUIP;
					break;
				case HEAVY:
					sound = Sounds.HEAVY_ARMOR_EQUIP;
					break;
				case CRYSTAL:
					sound = Sounds.CRYSTAL_ARMOR_EQUIP;
					break;
				default:
					sound = Sounds.UTILITY_EQUIP;
				}
			}

			this.soundId = SoundEffectHandler.INSTANCE.playSoundAtPlayer(player, sound);
		} else {
			this.soundId = null;
		}
		
		this.lastHeld = player.getHeldItemMainhand() != null;
		this.lastSlot = player.inventory.currentItem;
	}

	@Override
	public void onConnect() {
		this.itemRegistry = RegistryManager.get(RegistryType.ITEMS);
	}

	@Override
	public void onDisconnect() {
		this.itemRegistry = null;
	}

	@SubscribeEvent
	public void onJump(@Nonnull final LivingJumpEvent event) {
		if (!ModOptions.enableJumpSound)
			return;

		if (event.getEntity() == null || event.getEntity().worldObj == null)
			return;

		if (event.getEntity().worldObj.isRemote && EnvironState.isPlayer(event.getEntity()))
			SoundEffectHandler.INSTANCE.playSoundAtPlayer(EnvironState.getPlayer(), Sounds.JUMP);
	}

	@SubscribeEvent
	public void onItemSwing(@Nonnull final PlayerInteractEvent.LeftClickEmpty event) {
		if (!ModOptions.enableSwingSound)
			return;

		if (event.getEntityPlayer() == null || event.getEntityPlayer().worldObj == null)
			return;

		if (event.getEntityPlayer().worldObj.isRemote && EnvironState.isPlayer(event.getEntityPlayer())) {
			final ItemStack currentItem = event.getEntityPlayer().getHeldItemMainhand();
			if (currentItem != null) {
				SoundEffect sound = null;
				if (this.itemRegistry.doSwordSound(currentItem))
					sound = Sounds.SWORD_SWING;
				else if (this.itemRegistry.doAxeSound(currentItem))
					sound = Sounds.AXE_SWING;
				else if (this.itemRegistry.doToolSound(currentItem))
					sound = Sounds.TOOL_SWING;

				if (sound != null)
					SoundEffectHandler.INSTANCE.playSoundAtPlayer(EnvironState.getPlayer(), sound);
			}
		}
	}

	private int craftSoundThrottle = 0;

	@SubscribeEvent
	public void onCrafting(@Nonnull final ItemCraftedEvent event) {
		if (!ModOptions.enableCraftingSound)
			return;

		if (this.craftSoundThrottle >= (EnvironState.getTickCounter() - 30))
			return;

		if (event.player == null || event.player.worldObj == null)
			return;

		if (event.player.worldObj.isRemote && EnvironState.isPlayer(event.player)) {
			craftSoundThrottle = EnvironState.getTickCounter();
			SoundEffectHandler.INSTANCE.playSoundAtPlayer(EnvironState.getPlayer(), Sounds.CRAFTING);
		}

	}

	@SubscribeEvent
	public void onItemUse(@Nonnull final PlayerInteractEvent.RightClickItem event) {
		if (!ModOptions.enableBowPullSound)
			return;

		if (event.getEntityPlayer() == null || event.getEntityPlayer().worldObj == null || event.getItemStack() == null)
			return;

		if (event.getEntityPlayer().worldObj.isRemote && this.itemRegistry.doBowSound(event.getItemStack())) {
			SoundEffectHandler.INSTANCE.playSoundAtPlayer(EnvironState.getPlayer(), Sounds.BOW_PULL);
		}
	}

}
