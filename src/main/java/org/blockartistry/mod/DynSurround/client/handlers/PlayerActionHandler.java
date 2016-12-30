package org.blockartistry.mod.DynSurround.client.handlers;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundManager;
import org.blockartistry.mod.DynSurround.registry.ItemRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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

	private static final SoundEffect JUMP = new SoundEffect(new ResourceLocation(DSurround.RESOURCE_ID, "jump"), 0.4F,
			1.0F, true);;
	private static final SoundEffect CRAFTING = new SoundEffect(
			new ResourceLocation(DSurround.RESOURCE_ID, "crafting"));

	private static final SoundEffect SWORD = new SoundEffect(new ResourceLocation(DSurround.RESOURCE_ID, "swoosh"),
			1.0F, 1.0F);
	private static final SoundEffect AXE = new SoundEffect(new ResourceLocation(DSurround.RESOURCE_ID, "swoosh"), 1.0F,
			0.5F);
	private static final SoundEffect BOW_PULL = new SoundEffect(new ResourceLocation(DSurround.RESOURCE_ID, "bowpull"));

	private ItemRegistry itemRegistry;

	@Override
	public String getHandlerName() {
		return "PlayerActionHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

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
			SoundManager.playSoundAtPlayer(EnvironState.getPlayer(), JUMP, SoundCategory.PLAYERS);
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
					sound = SWORD;
				else if (this.itemRegistry.doAxeSound(currentItem))
					sound = AXE;

				if (sound != null)
					SoundManager.playSoundAtPlayer(EnvironState.getPlayer(), sound, SoundCategory.PLAYERS);
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
			SoundManager.playSoundAtPlayer(EnvironState.getPlayer(), CRAFTING, SoundCategory.PLAYERS);
		}

	}

	@SubscribeEvent
	public void onItemUse(@Nonnull final PlayerInteractEvent.RightClickItem event) {
		if (!ModOptions.enableBowPullSound)
			return;

		if (event.getEntityPlayer() == null || event.getEntityPlayer().worldObj == null || event.getItemStack() == null)
			return;

		if (event.getEntityPlayer().worldObj.isRemote && this.itemRegistry.doBowSound(event.getItemStack())) {
			SoundManager.playSoundAtPlayer(EnvironState.getPlayer(), BOW_PULL, SoundCategory.PLAYERS);
		}
	}

}
