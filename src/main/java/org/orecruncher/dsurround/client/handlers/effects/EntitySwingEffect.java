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

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.effects.EntityEffect;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactory;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactoryFilter;
import org.orecruncher.dsurround.client.sound.ISoundInstance;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.effect.EntityEffectInfo;
import org.orecruncher.dsurround.registry.item.IItemData;
import org.orecruncher.dsurround.registry.item.ItemUtils;
import org.orecruncher.lib.math.RayTrace;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntitySwingEffect extends EntityEffect {

	// A click on a block by the player (opening a door, starting to break one) within this many ticks: the swing it
	// starts isn't a swing at the air, though the trace a tick later can miss the door swung open or the block gone
	private static final int BLOCK_CLICK_TICKS = 2;
	private static int blockClickedAt = Integer.MIN_VALUE / 2;

	protected int swingProgress = 0;
	protected boolean isSwinging = false;

	@SubscribeEvent
	public static void onRightClickBlock(@Nonnull final PlayerInteractEvent.RightClickBlock event) {
		blockClicked(event);
	}

	@SubscribeEvent
	public static void onLeftClickBlock(@Nonnull final PlayerInteractEvent.LeftClickBlock event) {
		blockClicked(event);
	}

	private static void blockClicked(@Nonnull final PlayerInteractEvent event) {
		if (event.getWorld().isRemote && event.getEntityPlayer() == Minecraft.getMinecraft().player)
			blockClickedAt = event.getEntityPlayer().ticksExisted;
	}

	private static boolean clickedABlock(@Nonnull final EntityLivingBase entity) {
		return entity == Minecraft.getMinecraft().player
				&& Math.abs(entity.ticksExisted - blockClickedAt) <= BLOCK_CLICK_TICKS;
	}

	/**
	 * Whether a swing starts: it is in progress and wasn't, or it went back to the start. Not each time it moves on
	 * after a tick it didn't: slow weapons (the Betweenlands' greatsword and battleaxes) stall a tick at a time, and
	 * the sound played again after each (#763).
	 */
	static boolean swingStarts(final boolean inProgress, final int progress, final boolean wasSwinging,
			final int lastProgress) {
		return inProgress && (!wasSwinging || progress < lastProgress);
	}

	@Nonnull
	@Override
	public String name() {
		return "Item Swing";
	}

	@Override
	public void update(@Nonnull final Entity subject) {

		if (!ModOptions.sound.enableSwingSounds)
			return;

		final EntityLivingBase entity = (EntityLivingBase) subject;

		// Boats are strange - ignore them for now
		if (entity.getRidingEntity() instanceof EntityBoat)
			return;

		final boolean inProgress = entity.isSwingInProgress && entity.swingingHand != null;
		if (swingStarts(inProgress, entity.swingProgressInt, this.isSwinging, this.swingProgress)
				&& !clickedABlock(entity)) {
			final ItemStack currentItem = entity.getHeldItem(entity.swingingHand);
			final IItemData data = ItemUtils.getItemData(currentItem.getItem());
			final SoundEffect soundEffect = data.getSwingSound(currentItem);
			if (soundEffect != null) {
				final RayTraceResult whatImHitting = RayTrace.trace(entity);
				if (whatImHitting == null || whatImHitting.typeOfHit != Type.BLOCK) {
					final ISoundInstance snd = getState().createSound(soundEffect, entity);
					getState().playSound(snd);
				}
			}
		}

		this.isSwinging = inProgress;
		this.swingProgress = entity.swingProgressInt;
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> eei.effects.contains("swing");

	public static class Factory implements IEntityEffectFactory {

		@Nonnull
		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity) {
			return ImmutableList.of(new EntitySwingEffect());
		}
	}

}
