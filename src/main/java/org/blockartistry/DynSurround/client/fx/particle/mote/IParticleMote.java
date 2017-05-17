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

package org.blockartistry.DynSurround.client.fx.particle.mote;

import javax.annotation.Nonnull;

import com.google.common.base.Predicate;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IParticleMote {

	/**
	 * Predicate used to detect dead motes.
	 */
	public static final Predicate<IParticleMote> IS_DEAD = new Predicate<IParticleMote>() {
		@Override
		public boolean apply(@Nonnull final IParticleMote mote) {
			return !mote.isAlive();
		}
	};

	/**
	 * Predicate used to update a mote and return whether it is
	 * dead or not.
	 */
	public static final Predicate<IParticleMote> UPDATE_REMOVE = new Predicate<IParticleMote>() {
		@Override
		public boolean apply(@Nonnull final IParticleMote mote) {
			mote.onUpdate();
			return !mote.isAlive();
		}
	};

	boolean isAlive();

	void onUpdate();

	void renderParticle(final VertexBuffer buffer, final Entity entityIn, final float partialTicks, final float rotX,
			final float rotZ, final float rotYZ, final float rotXY, final float rotXZ);

}
