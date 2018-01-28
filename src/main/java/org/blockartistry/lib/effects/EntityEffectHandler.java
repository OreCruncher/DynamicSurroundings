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
package org.blockartistry.lib.effects;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An EntityEffectHandler is responsible for managing the effects that are
 * attached to an entity.
 */
@SideOnly(Side.CLIENT)
public class EntityEffectHandler extends EntityEffectStateBase implements IEntityEffectHandlerState {

	protected final List<EntityEffect> activeEffects;
	protected boolean isAlive = true;

	public EntityEffectHandler(@Nonnull final Entity entity) {
		super(entity);
		this.activeEffects = null;
	}

	public EntityEffectHandler(@Nonnull final Entity entity, @Nonnull final List<EntityEffect> effects) {
		super(entity);
		this.activeEffects = effects;
		for (final EntityEffect ee : this.activeEffects)
			ee.intitialize(this);
	}

	/**
	 * Updates the state of the EntityEffectHandler as well as the state of the
	 * EntityEffects that are attached.
	 */
	public void update() {
		if (!this.isAlive())
			return;

		final Entity entity = this.subject.get();
		this.isAlive = entity != null && entity.isEntityAlive();

		if (entity != null)
			for (final EntityEffect e : activeEffects)
				e.update(entity);
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
	public boolean isActive() {
		return true;
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

}
