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

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.fx.ParticleCollections;
import org.blockartistry.DynSurround.client.fx.particle.mote.IParticleMote;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.entity.CapabilityEmojiData;
import org.blockartistry.DynSurround.entity.EmojiType;
import org.blockartistry.DynSurround.entity.IEmojiDataSettable;
import org.blockartistry.DynSurround.event.EntityEmojiEvent;
import org.blockartistry.lib.WorldUtils;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityEmojiHandler extends EffectHandlerBase {

	private final TIntObjectHashMap<IParticleMote> emojiParticles = new TIntObjectHashMap<>();

	public EntityEmojiHandler() {
		super("Entity Emojis");
	}

	@Override
	public boolean doTick(final int tick) {
		return this.emojiParticles.size() > 0;
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		this.emojiParticles.retainEntries((idx, emoji) -> {
			return emoji.isAlive();
		});
	}

	@SubscribeEvent
	public void onEntityEmojiEvent(@Nonnull final EntityEmojiEvent event) {
		final Entity entity = WorldUtils.locateEntity(EnvironState.getWorld(), event.entityId);
		if (entity != null) {
			final IEmojiDataSettable data = (IEmojiDataSettable) entity.getCapability(CapabilityEmojiData.EMOJI, null);
			data.setActionState(event.actionState);
			data.setEmotionalState(event.emotionalState);
			data.setEmojiType(event.emojiType);

			if (ModOptions.speechbubbles.enableEntityEmojis && entity.isEntityAlive()
					&& data.getEmojiType() != EmojiType.NONE && !this.emojiParticles.contains(event.entityId)) {
				final IParticleMote mote = ParticleCollections.addEmoji(entity);
				if (mote != null)
					this.emojiParticles.put(event.entityId, mote);
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
