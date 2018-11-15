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
package org.orecruncher.dsurround.client.effects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.lib.collections.ObjectArray;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An EntityEffectHandler is responsible for managing the effects that are
 * attached to an entity.
 */
@SideOnly(Side.CLIENT)
public class EntityEffectHandler extends EntityEffectStateBase implements IEntityEffectHandlerState {

	/**
	 * Dummy do nothing handler.
	 */
	public static class Dummy extends EntityEffectHandler {
		public Dummy(@Nonnull final Entity entity) {
			super(entity, null, null);
		}

		@Override
		public void update() {
		}

		@Override
		public void die() {
			this.isAlive = false;
		}

		@Override
		public boolean isDummy() {
			return true;
		}

		@Override
		public List<String> getAttachedEffects() {
			return ImmutableList.of("Dummy EffectHandler");
		}
	};

	protected final ObjectArray<EntityEffect> activeEffects;
	protected boolean isAlive = true;
	protected double rangeToPlayer;

	public EntityEffectHandler(@Nonnull final Entity entity, @Nonnull final IParticleHelper ph,
			@Nonnull final ISoundHelper sh) {
		super(entity, ph, sh);
		this.activeEffects = null;
	}

	public EntityEffectHandler(@Nonnull final Entity entity, @Nonnull final ObjectArray<EntityEffect> effects,
			@Nonnull final IParticleHelper ph, @Nonnull final ISoundHelper sh) {
		super(entity, ph, sh);
		this.activeEffects = effects;
		for (final EntityEffect ee : this.activeEffects)
			ee.intitialize(this);
	}

	/**
	 * Updates the state of the EntityEffectHandler as well as the state of the
	 * EntityEffects that are attached.
	 */
	public void update() {
		if (!isAlive())
			return;

		this.isAlive = isSubjectAlive();
		final Entity entity = this.subject.get();
		if (entity != null) {
			final EntityPlayer player = Minecraft.getMinecraft().player;
			this.rangeToPlayer = entity.getDistanceSq(player);

			for (int i = 0; i < this.activeEffects.size(); i++) {
				final EntityEffect e = this.activeEffects.get(i);
				if (this.isAlive || e.receiveLastCall())
					e.update(entity);
			}
		}
	}

	/**
	 * Instructs the EntityEffectHandler that it should cleanup state because it is
	 * about to die.
	 */
	public void die() {
		this.isAlive = false;
		for (final EntityEffect e : this.activeEffects)
			e.die();
	}

	/**
	 * Used for metric collection to distinguish between active handlers and
	 * dummies.
	 *
	 * @return true if it is an active handler, false for a dummy
	 */
	public boolean isDummy() {
		return false;
	}

	/**
	 * Used for collecting diagnostic information.
	 *
	 * @return List of attached handlers
	 */
	@Nonnull
	public List<String> getAttachedEffects() {
		final List<String> result = new ArrayList<>();
		if (this.activeEffects.size() == 0) {
			result.add("No effects");
		} else {
			for (final EntityEffect e : this.activeEffects)
				result.add(e.toString());
		}
		return result;
	}

	// ================================================
	// IEntityEffectHandlerState interface
	// ================================================

	/**
	 * Whether the EntityEffectHandler is alive or dead.
	 *
	 * @return true if the EntityEffectHandler is active, false otherwise.
	 */
	@Override
	public boolean isAlive() {
		return this.isAlive;
	}

	/**
	 * Provides the distance, squared, to the player entity behind the keyboard.
	 *
	 * @return Range to client player, squared.
	 */
	@Override
	public double rangeToPlayerSq() {
		return this.rangeToPlayer;
	}

}
