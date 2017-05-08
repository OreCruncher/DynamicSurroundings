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

import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.entity.EmojiType;
import org.blockartistry.DynSurround.api.entity.EntityCapability;
import org.blockartistry.DynSurround.api.entity.IEntityEmoji;
import org.blockartistry.DynSurround.api.events.EntityEmojiEvent;
import org.blockartistry.DynSurround.client.fx.particle.ParticleEmoji;
import org.blockartistry.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.entity.IEntityEmojiSettable;
import org.blockartistry.lib.WorldUtils;

import com.google.common.base.Predicate;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityEmojiHandler extends EffectHandlerBase {

	private final TIntObjectHashMap<ParticleEmoji> emojiParticles = new TIntObjectHashMap<ParticleEmoji>();

	public EntityEmojiHandler() {
	}

	@Override
	public String getHandlerName() {
		return "EntityEmojiHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		if (this.emojiParticles.size() > 0) {
			// Get rid of dead particles
			final TIntObjectIterator<ParticleEmoji> data = this.emojiParticles.iterator();
			while (data.hasNext()) {
				data.advance();
				if (data.value().shouldExpire())
					data.remove();
			}
		}
		
		if(!ModOptions.enableEntityEmojis)
			return;
		
		// Spawn any new ones
		final BlockPos pos = EnvironState.getPlayerPosition();
		final double rangeSq = ModOptions.speechBubbleRange * ModOptions.speechBubbleRange;
		final Predicate<Entity> needFilter = new Predicate<Entity>() {
			@Override
			public boolean apply(@Nonnull final Entity input) {
				return input.isEntityAlive() && input.getDistanceSq(pos) <= rangeSq && !EntityEmojiHandler.this.emojiParticles.contains(input.getEntityId());
			}
		};
		
		final List<Entity> newOnes = world.getEntities(Entity.class, needFilter);
		for(final Entity e: newOnes) {
			final IEntityEmoji emoji = e.getCapability(EntityCapability.EMOJI, null);
			if(emoji != null && emoji.getEmojiType() != EmojiType.NONE) {
				final ParticleEmoji p = new ParticleEmoji(e);
				this.emojiParticles.put(e.getEntityId(), p);
				ParticleHelper.addParticle(p);
			}
		}

	}

	@SubscribeEvent
	public void onEntityEmojiEvent(@Nonnull final EntityEmojiEvent event) {
		final Entity entity = WorldUtils.locateEntity(EnvironState.getWorld(), event.entityId);
		if (entity != null) {
			final IEntityEmojiSettable data = (IEntityEmojiSettable) entity.getCapability(EntityCapability.EMOJI, null);
			data.setActionState(event.actionState);
			data.setEmotionalState(event.emotionalState);
			data.setEmojiType(event.emojiType);
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
