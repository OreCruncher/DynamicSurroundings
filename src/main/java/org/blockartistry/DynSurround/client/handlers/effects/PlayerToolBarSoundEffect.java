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

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.sound.BasicSound;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.lib.ItemStackUtil;
import org.blockartistry.lib.effects.EntityEffect;
import org.blockartistry.lib.effects.EntityEffectInfo;
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
public class PlayerToolBarSoundEffect extends EntityEffect {

	protected static class HandTracker {

		protected final EnumHand hand;

		protected Item lastHeld = null;
		protected String soundId = null;

		public HandTracker(@Nonnull final EntityPlayer player) {
			this(player, EnumHand.OFF_HAND);
		}

		protected HandTracker(@Nonnull final EntityPlayer player, @Nonnull final EnumHand hand) {
			this.hand = hand;
			this.lastHeld = this.getItemForHand(player, hand);
		}

		protected Item getItemForHand(final EntityPlayer player, final EnumHand hand) {
			final ItemStack stack = player.getHeldItem(hand);
			return ItemStackUtil.isValidItemStack(stack) ? stack.getItem() : null;
		}

		protected boolean triggerNewEquipSound(@Nonnull final EntityPlayer player) {
			final Item heldItem = getItemForHand(player, this.hand);
			return heldItem != this.lastHeld;
		}

		protected void clearState(@Nonnull final IEntityEffectHandlerState state) {
			if (!StringUtils.isEmpty(this.soundId))
				state.stopSound(this.soundId);
			this.soundId = null;
			this.lastHeld = null;
		}

		public void update(@Nonnull final IEntityEffectHandlerState state) {
			final EntityPlayer player = (EntityPlayer) state.subject().get();
			if (triggerNewEquipSound(player)) {
				this.clearState(state);
				final ItemStack currentStack = player.getHeldItem(this.hand);
				final SoundEffect soundEffect = ClientRegistry.ITEMS.getEquipSound(currentStack);
				if (soundEffect != null) {
					final BasicSound<?> sound = state.createSound(soundEffect, player);
					this.soundId = state.playSound(sound);
					this.lastHeld = currentStack.getItem();
				}
			}
		}
	}

	protected static class MainHandTracker extends HandTracker {

		protected int lastSlot = -1;

		public MainHandTracker(@Nonnull final EntityPlayer player) {
			super(player, EnumHand.MAIN_HAND);
			this.lastSlot = player.inventory.currentItem;
		}

		@Override
		protected boolean triggerNewEquipSound(@Nonnull final EntityPlayer player) {
			return this.lastSlot != player.inventory.currentItem || super.triggerNewEquipSound(player);
		}

		@Override
		public void update(@Nonnull final IEntityEffectHandlerState state) {
			super.update(state);
			this.lastSlot = ((EntityPlayer) (state.subject().get())).inventory.currentItem;
		}
	}

	protected final MainHandTracker mainHand;
	protected final HandTracker offHand;

	public PlayerToolBarSoundEffect(@Nonnull final EntityPlayer player) {
		this.mainHand = new MainHandTracker(player);
		this.offHand = new HandTracker(player);
	}

	@Override
	public String name() {
		return "PlayerToolBarSoundEffect";
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		if (ModOptions.sound.enableEquipSound) {
			this.mainHand.update(this.getState());
			this.offHand.update(this.getState());
		}
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = new IEntityEffectFactoryFilter() {
		@Override
		public boolean applies(@Nonnull final Entity e, @Nonnull final EntityEffectInfo eei) {
			return eei.effects.contains("toolbar");
		}
	};

	public static class Factory implements IEntityEffectFactory {
		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new PlayerToolBarSoundEffect((EntityPlayer) entity));
		}
	}

}
