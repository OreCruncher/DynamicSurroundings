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
package org.blockartistry.DynSurround.client.footsteps.implem;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An acoustic profile is associated with a given IBlockState.
 */
@SideOnly(Side.CLIENT)
public class AcousticProfile {

	public static final AcousticProfile NO_PROFILE = new AcousticProfile();
	public static final IAcoustic[] NO_ACOUSTICS = {};

	protected AcousticProfile() {

	}

	@Nonnull
	public IAcoustic[] get() {
		return NO_ACOUSTICS;
	}

	/**
	 * A Static acoustic profile is computed and stored for reuse. The profile does
	 * not change over time.
	 */
	public static class Static extends AcousticProfile {

		protected final IAcoustic[] acoustics;

		public Static(@Nonnull final IAcoustic[] acoustics) {
			this.acoustics = acoustics;
		}

		@Override
		@Nonnull
		public IAcoustic[] get() {
			return this.acoustics;
		}
	}

	/**
	 * A Dynamic acoustic profile can change over time based on the world
	 * environment at the time of query.
	 */
	public static class Dynamic extends AcousticProfile {

		protected final IBlockState state;

		public Dynamic(@Nonnull final IBlockState state) {
			this.state = state;
		}

		@Override
		@Nonnull
		public IAcoustic[] get() {
			return ClientRegistry.FOOTSTEPS.resolvePrimitive(this.state);
		}
	}
}
