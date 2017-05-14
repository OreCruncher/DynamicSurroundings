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
package org.blockartistry.DynSurround.api.events;

import javax.annotation.Nonnull;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Fires when a footstep event occurs. If received on client it means a footstep
 * is going to be displayed; if on server it means a client is requesting it to
 * be distributed to other attached clients.
 */
public class FootstepEvent extends Event {

	public final int dimensionId;
	public final Side side;
	public final Vec3d location;
	public final float rotation;
	public final boolean isRightFoot;

	protected FootstepEvent(final int dimensionId, @Nonnull final Side side, @Nonnull final Vec3d loc, final float rotation, final boolean rightFoot) {
		this.dimensionId = dimensionId;
		this.side = side;
		this.location = loc;
		this.rotation = rotation;
		this.isRightFoot = rightFoot;
	}

	/**
	 * Event generated server side when a client requests that footprints be
	 * distributed to connected clients.
	 */
	public static class Send extends FootstepEvent {
		public Send(final int dimensionId, @Nonnull final Vec3d pos, final float rotation, final boolean rightFoot) {
			super(dimensionId, Side.SERVER, pos, rotation, rightFoot);
		}
	}

	/**
	 * Event received client side when the server requests that footprints be
	 * displayed.
	 */
	public static class Display extends FootstepEvent {
		public Display(final int dimensionId, @Nonnull final Vec3d pos, final float rotation, final boolean rightFoot) {
			super(dimensionId, Side.CLIENT, pos, rotation, rightFoot);
		}
	}

}
