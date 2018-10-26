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
package org.blockartistry.DynSurround.client.effects;

import javax.annotation.Nonnull;

import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An EventEffect is a special effect that can take place in response to a Forge
 * event. For example, the JUMP sound would play in response to a
 * LivingJumpEvent.
 */
@SideOnly(Side.CLIENT)
public abstract class EventEffect {

	private IEventEffectLibraryState library;

	public EventEffect() {
	}

	/**
	 * Used by the framework to configure the EffectLibrary to which it is
	 * associated. It is set during registration.
	 *
	 * @param state
	 *            The library set to configure for the EventEffect
	 */
	void setState(@Nonnull final IEventEffectLibraryState state) {
		this.library = state;
	}

	/**
	 * Accessor to obtain the IEventEffectLibraryState associated with this
	 * EventEffect instance.
	 *
	 * @return Associated IEventEffectLibraryState instance
	 */
	protected IEventEffectLibraryState getState() {
		return this.library;
	}

	/**
	 * Determines if the EntityEvent is valid in terms of a proper Entity being
	 * configured and that the event is fired on Side.CLIENT.
	 *
	 * @param event
	 *            The event to evaluate
	 * @return true if valid, false otherwise
	 */
	protected boolean isClientValid(@Nonnull final EntityEvent event) {
		if (event != null) {
			if (event.getEntity() != null) {
				if (event.getEntity().getEntityWorld() != null) {
					return event.getEntity().getEntityWorld().isRemote;
				}
			}
		}

		return false;
	}

	/**
	 * Determines if the PlayerEvent is valid in terms of a proper Entity being
	 * configured and that the event is fired on Side.CLIENT.
	 *
	 * @param event
	 *            The event to evaluate
	 * @return true if valid, false otherwise
	 */
	protected boolean isClientValid(@Nonnull final PlayerEvent event) {
		if (event != null) {
			if (event.player != null) {
				if (event.player.getEntityWorld() != null) {
					return event.player.getEntityWorld().isRemote;
				}
			}
		}

		return false;
	}

}
