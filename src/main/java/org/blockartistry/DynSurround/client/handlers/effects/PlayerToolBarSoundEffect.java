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
package org.blockartistry.DynSurround.client.handlers.effects;

import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.BasicSound;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.registry.ItemRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.effects.IEntityEffect;
import org.blockartistry.lib.effects.IEntityEffectFactory;
import org.blockartistry.lib.effects.IEntityEffectFactoryFilter;
import org.blockartistry.lib.effects.IEntityEffectHandlerState;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayerToolBarSoundEffect implements IEntityEffect {

	protected final ItemRegistry itemRegistry = RegistryManager.get(RegistryType.ITEMS);

	protected class HandTracker {

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

		public void update(@Nonnull final IEntityEffectHandlerState state) {
			final EntityPlayer player = (EntityPlayer) state.subject().get();
			if (triggerNewEquipSound(player)) {

				state.stopSound(this.soundId);
				final ItemStack currentStack = player.getHeldItem(this.hand);
				final SoundEffect soundEffect = PlayerToolBarSoundEffect.this.itemRegistry.getEquipSound(currentStack);
				if (soundEffect != null) {
					final BasicSound<?> sound = state.createSound(soundEffect, player);
					this.soundId = state.playSound(sound);
					this.lastHeld = currentStack.getItem();
				} else {
					this.soundId = null;
					this.lastHeld = null;
				}
			}
		}
	}

	protected class MainHandTracker extends HandTracker {

		protected int lastSlot = -1;

		public MainHandTracker() {
			super(EnumHand.MAIN_HAND);
		}

		@Override
		protected boolean triggerNewEquipSound(@Nonnull final EntityPlayer player) {
			return this.lastSlot != player.inventory.currentItem || super.triggerNewEquipSound(player);
		}

		@Override
		public void update(@Nonnull final IEntityEffectHandlerState state) {
			super.update(state);
			this.lastSlot = EnvironState.getPlayer().inventory.currentItem;
		}
	}

	protected final MainHandTracker mainHand = new MainHandTracker();
	protected final HandTracker offHand = new HandTracker();

	public PlayerToolBarSoundEffect() {

	}

	@Override
	public void update(@Nonnull final IEntityEffectHandlerState state) {
		if (ModOptions.enableEquipSound) {
			this.mainHand.update(state);
			this.offHand.update(state);
		}
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = new IEntityEffectFactoryFilter() {
		@Override
		public boolean applies(@Nonnull final Entity e) {
			return e instanceof EntityPlayer;
		}
	};

	public static class Factory implements IEntityEffectFactory {
		@Override
		public List<IEntityEffect> create(@Nonnull final Entity entity) {
			return ImmutableList.of(new PlayerToolBarSoundEffect());
		}
	}

}
