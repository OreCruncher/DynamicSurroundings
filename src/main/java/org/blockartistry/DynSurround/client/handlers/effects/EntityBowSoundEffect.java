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

import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.lib.ItemStackUtil;
import org.blockartistry.lib.effects.EntityEffect;
import org.blockartistry.lib.effects.EntityEffectInfo;
import org.blockartistry.lib.effects.IEntityEffectFactory;
import org.blockartistry.lib.effects.IEntityEffectFactoryFilter;
import org.blockartistry.lib.sound.BasicSound;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityBowSoundEffect extends EntityEffect {

	protected ItemStack lastActiveStack;
	
	@Override
	public String name() {
		return "Bow Sound";
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		final EntityLivingBase entity = (EntityLivingBase) subject;
		final ItemStack currentStack = entity.getActiveItemStack();
		if (ItemStackUtil.isValidItemStack(currentStack)) {

			if (this.lastActiveStack == null || !ItemStack.areItemStacksEqual(currentStack, this.lastActiveStack)) {
				if (ClientRegistry.ITEMS.isBow(currentStack) || ClientRegistry.ITEMS.isShield(currentStack)) {
					final SoundEffect soundEffect = ClientRegistry.ITEMS.getUseSound(currentStack);
					if (soundEffect != null) {
						final BasicSound<?> fx = this.getState().createSound(soundEffect, entity);
						this.getState().playSound(fx);
					}
				}

				this.lastActiveStack = currentStack;
			}

		} else {
			this.lastActiveStack = null;
		}
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = new IEntityEffectFactoryFilter() {
		@Override
		public boolean applies(@Nonnull final Entity e, @Nonnull final EntityEffectInfo eei) {
			return eei.effects.contains("bow");
		}
	};

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new EntityBowSoundEffect());
		}
	}

}
