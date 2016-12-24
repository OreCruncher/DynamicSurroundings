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
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.client.event.EntityEmojiEvent;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleEmoji;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.entity.ActionState;
import org.blockartistry.mod.DynSurround.entity.EmojiType;
import org.blockartistry.mod.DynSurround.entity.EmotionalState;
import org.blockartistry.mod.DynSurround.util.WorldUtils;

import com.google.common.base.Function;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityEmojiHandler extends EffectHandlerBase {
	
	private static class EntityEmojiData {
		
		@SuppressWarnings("unused")
		protected ActionState actionState;
		@SuppressWarnings("unused")
		protected EmotionalState emotionalState;
		protected EmojiType emojiType;
		protected WeakReference<ParticleEmoji> particle;
		
		public EntityEmojiData(@Nonnull final ActionState action, @Nonnull final EmotionalState emotion, @Nonnull EmojiType type) {
			this.actionState = action;
			this.emotionalState = emotion;
			this.emojiType = type;
		}
	}

	private final TIntObjectHashMap<EntityEmojiData> emojiData = new TIntObjectHashMap<EntityEmojiData>();

	public EntityEmojiHandler() {
	}
	
	@Override
	public String getHandlerName() {
		return "EntityEmojiHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		// Clear out the old emoji data for entities that no
		// longer exist.
		final TIntObjectIterator<EntityEmojiData> data = this.emojiData.iterator();
		while (data.hasNext()) {
			data.advance();
			if(WorldUtils.locateEntity(player.getEntityWorld(), data.key()) == null) {
				data.remove();
			}
		}

	}

	@SubscribeEvent
	public void onEntityEmojiEvent(@Nonnull final EntityEmojiEvent event) {
		
		final Entity entity = WorldUtils.locateEntity(EnvironState.getWorld(), event.entityId);
		if(entity != null) {

			EntityEmojiData data = this.emojiData.get(entity.getEntityId());
			if(data == null) {
				this.emojiData.put(entity.getEntityId(), data = new EntityEmojiData(event.actionState, event.emotionalState, event.emojiType));
			} else {
				data.actionState = event.actionState;
				data.emotionalState = event.emotionalState;
				data.emojiType = event.emojiType;
			}
			
			if((data.particle == null || data.particle.get() == null || !data.particle.get().isAlive()) && data.emojiType != EmojiType.NONE) {
				
				final Function<Integer, EmojiType> ACCESSOR = new Function<Integer, EmojiType>() {

					@Override
					@Nullable
					public EmojiType apply(@Nonnull final Integer input) {
						final EntityEmojiData data = EntityEmojiHandler.this.emojiData.get(input.intValue());
						return data == null ? null : data.emojiType;
					}

				};
				
				final ParticleEmoji particle = new ParticleEmoji(entity, ACCESSOR);
				data.particle = new WeakReference<ParticleEmoji>(particle);
				ParticleHelper.addParticle(particle);
			}
		}
		
	}
	
	@Override
	public void onConnect() {
		this.emojiData.clear();
	}
	
	@Override
	public void onDisconnect() {
		this.emojiData.clear();
	}
}
