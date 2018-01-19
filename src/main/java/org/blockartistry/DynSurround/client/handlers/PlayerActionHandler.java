/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.DynSurround.client.handlers;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.fx.particle.ParticleBreath;
import org.blockartistry.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.BasicSound;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.client.sound.SoundEngine;
import org.blockartistry.DynSurround.client.sound.Sounds;
import org.blockartistry.DynSurround.registry.ItemRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayerActionHandler extends EffectHandlerBase {

	private ItemRegistry itemRegistry;

	private class HandTracker implements ITickable {

		protected final EnumHand hand;

		protected Item lastHeld = null;
		protected String soundId = null;

		public HandTracker() {
			this(EnumHand.OFF_HAND);
		}

		protected HandTracker(@Nonnull final EnumHand hand) {
			this.hand = hand;
		}

		protected boolean triggerNewEquipSound(@Nonnull final EntityPlayer player) {
			final ItemStack stack = player.getHeldItem(this.hand);
			if (this.lastHeld == null && stack == null)
				return false;

			return stack == null && this.lastHeld != null || stack != null && this.lastHeld == null
					|| this.lastHeld != stack.getItem();
		}

		@Override
		public void update() {
			final EntityPlayer player = EnvironState.getPlayer();
			if (triggerNewEquipSound(player)) {

				SoundEngine.instance().stopSound(this.soundId, SoundCategory.PLAYERS);

				final ItemStack currentStack = player.getHeldItem(this.hand);
				final SoundEffect soundEffect = PlayerActionHandler.this.itemRegistry.getEquipSound(currentStack);
				if (soundEffect != null) {
					final BasicSound<?> sound = makeSound(soundEffect);
					this.soundId = SoundEffectHandler.INSTANCE.playSound(sound);
					this.lastHeld = currentStack.getItem();
				} else {
					this.soundId = null;
					this.lastHeld = null;
				}
			}
		}
	}

	private class MainHandTracker extends HandTracker {

		protected int lastSlot = -1;

		public MainHandTracker() {
			super(EnumHand.MAIN_HAND);
		}

		@Override
		protected boolean triggerNewEquipSound(@Nonnull final EntityPlayer player) {
			return this.lastSlot != player.inventory.currentItem || super.triggerNewEquipSound(player);
		}

		@Override
		public void update() {
			super.update();
			this.lastSlot = EnvironState.getPlayer().inventory.currentItem;
		}
	}

	protected final MainHandTracker mainHand = new MainHandTracker();
	protected final HandTracker offHand = new HandTracker();

	public PlayerActionHandler() {
		super("PlayerActionHandler");

		this.itemRegistry = RegistryManager.get(RegistryType.ITEMS);
	}

	private static BasicSound<?> makeSound(@Nonnull final SoundEffect se) {
		return se.createSound(EnvironState.getPlayer(), false).setAttenuationType(BasicSound.noAttenuation());
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		if (ModOptions.suppressPotionParticles)
			player.getDataManager().set(EntityLivingBase.HIDE_PARTICLES, true);

		// Handle item equip sounds
		if (ModOptions.enableEquipSound) {
			this.mainHand.update();
			this.offHand.update();
		}

		// Frost breath if enabled
		if (ModOptions.showBreath)
			this.doFrostBreath();
	}

	@SubscribeEvent
	public void onJump(@Nonnull final LivingJumpEvent event) {
		if (!ModOptions.enableJumpSound)
			return;

		if (event.getEntity() == null || event.getEntity().worldObj == null)
			return;

		if (event.getEntity().worldObj.isRemote && EnvironState.isPlayer(event.getEntity())) {
			final BasicSound<?> sound = makeSound(Sounds.JUMP);
			SoundEffectHandler.INSTANCE.playSound(sound);
		}
	}

	@SubscribeEvent
	public void onItemSwing(@Nonnull final PlayerInteractEvent.LeftClickEmpty event) {
		if (event.getEntityPlayer() == null || event.getEntityPlayer().worldObj == null)
			return;

		if (event.getEntityPlayer().worldObj.isRemote && EnvironState.isPlayer(event.getEntityPlayer())) {
			final ItemStack currentItem = event.getEntityPlayer().getHeldItem(event.getHand());
			final SoundEffect soundEffect = this.itemRegistry.getSwingSound(currentItem);
			if (soundEffect != null) {
				final BasicSound<?> sound = makeSound(soundEffect);
				sound.setRoutable(DSurround.isInstalledOnServer());
				SoundEffectHandler.INSTANCE.playSound(sound);
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
			this.craftSoundThrottle = EnvironState.getTickCounter();
			final BasicSound<?> sound = makeSound(Sounds.CRAFTING);
			SoundEffectHandler.INSTANCE.playSound(sound);
		}

	}

	@SubscribeEvent
	public void onItemUse(@Nonnull final PlayerInteractEvent.RightClickItem event) {
		if (event.getEntityPlayer() == null || event.getEntityPlayer().worldObj == null || event.getItemStack() == null)
			return;

		if (event.getEntityPlayer().worldObj.isRemote && this.itemRegistry.doBowSound(event.getItemStack())) {
			final ItemStack currentItem = event.getEntityPlayer().getHeldItem(event.getHand());
			final SoundEffect soundEffect = this.itemRegistry.getUseSound(currentItem);
			if (soundEffect != null) {
				final BasicSound<?> sound = makeSound(soundEffect);
				sound.setRoutable(DSurround.isInstalledOnServer());
				SoundEffectHandler.INSTANCE.playSound(sound);
			}
		}
	}

	protected void doFrostBreath() {
		final double distSq = ModOptions.specialEffectRange * ModOptions.specialEffectRange;
		for (final Entity e : EnvironState.getWorld().loadedEntityList) {
			if (isRightMobType(e)) {
				final int v = getTickInterval(e);
				if (v < 3 && e.getDistanceSqToEntity(EnvironState.getPlayer()) <= distSq
						&& !e.isInvisibleToPlayer(EnvironState.getPlayer()) && e.isInsideOfMaterial(Material.AIR)
						&& EnvironState.getPlayer().canEntityBeSeen(e)) {
					handleEntityBreath(e);
				}
			}
		}
	}
	
	protected int getTickInterval(final Entity e) {
		return ((EnvironState.getTickCounter() + MathStuff.abs(e.getPersistentID().hashCode())) / 10) % 8;
	}

	protected boolean isRightMobType(final Entity e) {
		return e instanceof EntityPlayer || e instanceof EntityVillager;
	}
	
	protected void handleEntityBreath(final Entity entity) {
		// < 0.2D is considered COLD
		final BlockPos entityPos = entity.getPosition();
		final float temp = entity.getEntityWorld().getBiome(entityPos).getFloatTemperature(entityPos);
		if (temp < 0.2F) {
			ParticleHelper.addParticle(new ParticleBreath(entity));
		}
	}

}
