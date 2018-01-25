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

import javax.annotation.Nonnull;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Interface for an effect.
 */
@SideOnly(Side.CLIENT)
public abstract class EntityEffect {

	private IEntityEffectHandlerState state;

	/**
	 * Do not perform any heavy initialization in the CTOR! Do it in the
	 * initialize() method!
	 */
	public EntityEffect() {

	}

	/**
	 * Called by the EntityEffectLibrary during the initialization of an
	 * EntityEffectHandler. Override this method to perform any initialization
	 * specific to the EntityEffect. Remember to call the super class!
	 * 
	 * @param state
	 *            The state provided by the EntityEffectLibrary
	 */
	public void intitialize(@Nonnull final IEntityEffectHandlerState state) {
		this.state = state;
	}

	/**
	 * Accessor to obtain the IEntityEffectHandlerState associated with this
	 * EntityEffect instance.
	 * 
	 * @return Associated IEntityEffectHandlerState instance
	 */
	protected IEntityEffectHandlerState getState() {
		return this.state;
	}

	/**
	 * Called when an EntityEffect should update it's state and take action based on
	 * results. Called once per tick.
	 */
	public abstract void update();

	/**
	 * Called when the EntityEffectHandler decides that the EntityEffect should die.
	 * Normally this method would not be hooked. Should only be hooked if there is
	 * additional state in other lists and places that need to be cleaned up.
	 */
	public void die() {
	}

}
