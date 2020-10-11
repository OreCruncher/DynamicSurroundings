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
package org.orecruncher.dsurround.client.handlers.effects;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.effects.EntityEffect;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactory;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactoryFilter;
import org.orecruncher.dsurround.client.effects.IEntityEffectHandlerState;
import org.orecruncher.dsurround.client.sound.ISoundInstance;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.effect.EntityEffectInfo;
import org.orecruncher.dsurround.registry.item.IItemData;
import org.orecruncher.dsurround.registry.item.ItemUtils;

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
		protected Item lastHeld;

		public HandTracker(@Nonnull final EntityPlayer player) {
			this(player, EnumHand.OFF_HAND);
		}

		protected HandTracker(@Nonnull final EntityPlayer player, @Nonnull final EnumHand hand) {
			this.hand = hand;
			this.lastHeld = getItemForHand(player, hand);
		}

		protected Item getItemForHand(final EntityPlayer player, final EnumHand hand) {
			final ItemStack stack = player.getHeldItem(hand);
			return stack.getItem();
		}

		protected boolean triggerNewEquipSound(@Nonnull final EntityPlayer player) {
			final Item heldItem = getItemForHand(player, this.hand);
			return heldItem != this.lastHeld;
		}

		protected void clearState() {
			this.lastHeld = null;
		}

		public void update(@Nonnull final IEntityEffectHandlerState state) {
			Optional<Entity> e = state.subject();
			if (e.isPresent()) {
				final EntityPlayer player = (EntityPlayer) e.get();
				if (triggerNewEquipSound(player)) {
					clearState();
					final ItemStack currentStack = player.getHeldItem(this.hand);
					final IItemData data = ItemUtils.getItemData(currentStack.getItem());
					final SoundEffect soundEffect = data.getEquipSound(currentStack);
					if (soundEffect != null) {
						final ISoundInstance snd = state.createSound(soundEffect, player);
						state.playSound(snd);
						this.lastHeld = currentStack.getItem();
					}
				}
			}
		}
	}

	protected static class MainHandTracker extends HandTracker {

		protected int lastSlot;

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
			state.subject().ifPresent(e -> this.lastSlot = ((EntityPlayer) e).inventory.currentItem);
		}
	}

	protected final MainHandTracker mainHand;
	protected final HandTracker offHand;

	public PlayerToolBarSoundEffect(@Nonnull final EntityPlayer player) {
		this.mainHand = new MainHandTracker(player);
		this.offHand = new HandTracker(player);
	}

	@Nonnull
	@Override
	public String name() {
		return "Toolbar";
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		if (ModOptions.sound.enableEquipSound) {
			this.mainHand.update(getState());
			this.offHand.update(getState());
		}
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> eei.effects.contains("toolbar");

	public static class Factory implements IEntityEffectFactory {
		@Nonnull
		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity) {
			return ImmutableList.of(new PlayerToolBarSoundEffect((EntityPlayer) entity));
		}
	}

}
