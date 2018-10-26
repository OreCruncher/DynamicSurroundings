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
import org.blockartistry.DynSurround.client.effects.EntityEffect;
import org.blockartistry.DynSurround.client.effects.IEntityEffectFactory;
import org.blockartistry.DynSurround.client.effects.IEntityEffectFactoryFilter;
import org.blockartistry.DynSurround.client.fx.ParticleCollections;
import org.blockartistry.DynSurround.client.fx.particle.mote.IParticleMote;
import org.blockartistry.DynSurround.entity.CapabilityEmojiData;
import org.blockartistry.DynSurround.entity.EmojiType;
import org.blockartistry.DynSurround.entity.IEmojiData;
import org.blockartistry.DynSurround.registry.EntityEffectInfo;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityEmojiEffect extends EntityEffect {

	// Number of ticks to keep the icon around until
	// the particle is dismissed.
	private static final int HOLD_TICK_COUNT = 40;

	protected IParticleMote mote = null;
	protected int holdTicks;

	public EntityEmojiEffect(@Nonnull final Entity entity) {

	}

	@Override
	public String name() {
		return "Entity Emoji";
	}

	@Override
	public void update(@Nonnull final Entity entity) {
		if (!ModOptions.speechbubbles.enableEntityEmojis) {
			killMote();
		} else if (entity.isEntityAlive()) {
			final IEmojiData data = entity.getCapability(CapabilityEmojiData.EMOJI, null);
			if (data != null) {
				if (data.getEmojiType() != EmojiType.NONE && this.mote == null) {
					this.mote = ParticleCollections.addEmoji(entity);
				} else if (data.getEmojiType() == EmojiType.NONE && this.mote != null) {
					this.holdTicks++;
					if (this.holdTicks >= HOLD_TICK_COUNT) {
						killMote();
					}
				}
			} else {
				killMote();
			}
		} else {
			killMote();
		}
	}

	protected void killMote() {
		if (this.mote != null) {
			this.holdTicks = 0;
			this.mote.kill();
			this.mote = null;
		}
	}

	@Override
	public void die() {
		killMote();
	}

	public static final IEntityEffectFactoryFilter DEFAULT_FILTER = (@Nonnull final Entity e,
			@Nonnull final EntityEffectInfo eei) -> e instanceof EntityLivingBase;

	public static class Factory implements IEntityEffectFactory {

		@Override
		public List<EntityEffect> create(@Nonnull final Entity entity, @Nonnull final EntityEffectInfo eei) {
			return ImmutableList.of(new EntityEmojiEffect(entity));
		}
	}

}
