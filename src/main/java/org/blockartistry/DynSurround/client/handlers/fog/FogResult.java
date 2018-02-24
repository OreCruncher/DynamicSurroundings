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
package org.blockartistry.DynSurround.client.handlers.fog;

import javax.annotation.Nonnull;

import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class FogResult {

	public static final float DEFAULT_PLANE_SCALE = 0.75F;

	private int fogMode;
	private float start;
	private float end;

	public FogResult() {
		this.fogMode = 0;
		this.end = this.start = 0F;
	}

	public FogResult(final int fogMode, final float distance, final float scale) {
		this.set(fogMode, distance, scale);
	}

	public FogResult(final float start, final float end) {
		this.set(start, end);
	}

	public FogResult(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
		this.set(event);
	}

	public void set(final int fogMode, final float distance, final float scale) {
		this.fogMode = fogMode;
		this.start = fogMode < 0 ? 0F : distance * scale;
		this.end = distance;
	}

	public void set(final float start, final float end) {
		this.fogMode = 0;
		this.start = start;
		this.end = end;
	}

	public void set(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
		this.set(event.getFogMode(), event.getFarPlaneDistance(), DEFAULT_PLANE_SCALE);
	}

	public int getFogMode() {
		return this.fogMode;
	}

	public float getStart() {
		return this.start;
	}

	public float getEnd() {
		return this.end;
	}

	public boolean isValid(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
		return this.end > this.start && event.getFogMode() == this.fogMode;
	}

	@Override
	public String toString() {
		return String.format("[mode: %d, start: %f, end: %f]", this.fogMode, this.start, this.end);
	}

}
