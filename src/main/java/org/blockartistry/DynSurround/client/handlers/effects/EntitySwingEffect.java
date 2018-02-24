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
import org.blockartistry.DynSurround.registry.EntityEffectInfo;
import org.blockartistry.lib.effects.EntityEffect;
import org.blockartistry.lib.effects.IEntityEffectFactory;
import org.blockartistry.lib.effects.IEntityEffectFactoryFilter;
import org.blockartistry.lib.math.RayTrace;
import org.blockartistry.lib.sound.BasicSound;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntitySwingEffect extends EntityEffect {

	protected int swingProgress = 0;
	protected boolean isSwinging = false;

	@Override
	public String name() {
		return "Item Swing";
	}

	@Override
	public void update(@Nonnull final Entity subject) {
		final EntityLivingBase entity = (EntityLivingBase) subject;

		// Boats are strange - ignore them for now
		if (entity.getRidingEntity() instanceof EntityBoat)
			return;

		// Is the swing in motion
		if (entity.swingingHand != null && entity.swingProgressInt > this.swingProgress) {
			if (!this.isSwinging) {
				final ItemStack currentItem = entity.getHeldItem(entity.swingingHand);
				final SoundEffect soundEffect = ClientRegistry.ITEMS.getSwingSound(currentItem);
				if (soundEffect != null) {
					final RayTraceResult whatImHitting = RayTrace.trace(entity);
					if (whatImHitting == null || whatImHitting.typeOfHit != Type.BLOCK) {
						final BasicSound<?> snd = getState().createSound(soundEffect, entity);
						getState().playSound(snd);
					}
				}
			}

			this.isSwinging = true;
			this.swingProgress = entity.swingProgressInt;

		} else {
			this.isSwinging = false;
			this.swingProgress = entity.swingProgressInt;
		}
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> eei.effects.contains("swing");

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new EntitySwingEffect());
		}
	}

}
