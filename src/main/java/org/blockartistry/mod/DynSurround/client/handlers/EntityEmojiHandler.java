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

package org.blockartistry.mod.DynSurround.client.handlers;

import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import org.blockartistry.mod.DynSurround.client.event.EntityEmojiEvent;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleEmoji;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.entity.EmojiType;
import org.blockartistry.mod.DynSurround.entity.EntityEmojiCapability;
import org.blockartistry.mod.DynSurround.entity.IEntityEmojiSettable;
import org.blockartistry.mod.DynSurround.util.WorldUtils;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityEmojiHandler extends EffectHandlerBase {

	private final TIntObjectHashMap<WeakReference<ParticleEmoji>> emojiParticles = new TIntObjectHashMap<WeakReference<ParticleEmoji>>();

	public EntityEmojiHandler() {
	}

	@Override
	public String getHandlerName() {
		return "EntityEmojiHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		// Get rid of dead particles
		final TIntObjectIterator<WeakReference<ParticleEmoji>> data = this.emojiParticles.iterator();
		while (data.hasNext()) {
			data.advance();
			final ParticleEmoji particle = data.value().get();
			if (particle == null || !particle.isAlive()) {
				data.remove();
			}
		}

	}

	@SubscribeEvent
	public void onEntityEmojiEvent(@Nonnull final EntityEmojiEvent event) {

		final Entity entity = WorldUtils.locateEntity(EnvironState.getWorld(), event.entityId);
		if (entity != null) {
			final IEntityEmojiSettable data = (IEntityEmojiSettable) entity
					.getCapability(EntityEmojiCapability.CAPABILIITY, null);
			data.setActionState(event.actionState);
			data.setEmotionalState(event.emotionalState);
			data.setEmojiType(event.emojiType);
			
			final WeakReference<ParticleEmoji> particle = this.emojiParticles.get(entity.getEntityId());

			if ((particle == null || particle.get() == null || !particle.get().isAlive())
					&& data.getEmojiType() != EmojiType.NONE) {

				final ParticleEmoji p = new ParticleEmoji(entity);
				this.emojiParticles.put(entity.getEntityId(), new WeakReference<ParticleEmoji>(p));
				ParticleHelper.addParticle(p);
			}
		}

	}

	@Override
	public void onConnect() {
		this.emojiParticles.clear();
	}

	@Override
	public void onDisconnect() {
		this.emojiParticles.clear();
	}
}
