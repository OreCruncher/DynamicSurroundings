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

import org.orecruncher.dsurround.client.effects.EntityEffect;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactory;
import org.orecruncher.dsurround.client.effects.IEntityEffectFactoryFilter;
import org.orecruncher.dsurround.client.sound.ISoundInstance;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.effect.EntityEffectInfo;
import org.orecruncher.dsurround.registry.item.IItemData;
import org.orecruncher.dsurround.registry.item.ItemClass;
import org.orecruncher.dsurround.registry.item.ItemUtils;
import org.orecruncher.lib.ItemStackUtil;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityBowSoundEffect extends EntityEffect {

	private ItemStack lastActiveStack = ItemStack.EMPTY;

	@Nonnull
	@Override
	public String name() {
		return "Bow Sound";
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		final EntityLivingBase entity = (EntityLivingBase) subject;
		final ItemStack currentStack = entity.getActiveItemStack();
		if (ItemStackUtil.isValidItemStack(currentStack)) {
			if (!ItemStack.areItemStacksEqual(currentStack, this.lastActiveStack)) {
				final IItemData data = ItemUtils.getItemData(currentStack.getItem());
				final ItemClass itemClass = data.getItemClass();
				if (itemClass == ItemClass.BOW || itemClass == ItemClass.SHIELD) {
					final SoundEffect soundEffect = data.getUseSound(currentStack);
					if (soundEffect != null) {
						final ISoundInstance fx = getState().createSound(soundEffect, entity);
						getState().playSound(fx);
					}
				}

				this.lastActiveStack = currentStack;
			}

		} else {
			this.lastActiveStack = ItemStack.EMPTY;
		}
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> eei.effects.contains("bow");

	public static class Factory implements IEntityEffectFactory {

		@Nonnull
		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity) {
			return ImmutableList.of(new EntityBowSoundEffect());
		}
	}

}
