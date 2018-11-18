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
package org.orecruncher.dsurround.client.footsteps.system;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.footsteps.implem.AcousticsManager;
import org.orecruncher.dsurround.client.footsteps.implem.BlockMap;
import org.orecruncher.dsurround.client.footsteps.implem.Substrate;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;
import org.orecruncher.dsurround.facade.FacadeHelper;
import org.orecruncher.lib.MyUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class AcousticResolver {

	protected final IBlockState airState = Blocks.AIR.getDefaultState();
	protected final IBlockAccess world;
	protected final BlockMap blockMap;
	protected final FootStrikeLocation loc;

	public AcousticResolver(@Nonnull final IBlockAccess world, @Nonnull final BlockMap map,
			@Nonnull final FootStrikeLocation loc) {
		this.world = world;
		this.blockMap = map;
		this.loc = loc;
	}

	protected IBlockState getBlockStateFacade(@Nonnull final BlockPos pos) {
		return FacadeHelper.resolveState(loc.getEntity(), getBlockState(pos), this.world, pos, EnumFacing.UP);
	}

	protected IBlockState getBlockState(@Nonnull final BlockPos pos) {
		return this.world.getBlockState(pos);
	}

	public Association resolve(@Nonnull BlockPos pos) {
		IBlockState in = null;
		IAcoustic[] acoustics = null;

		BlockPos tPos = pos.up();
		final IBlockState above = getBlockState(tPos);

		if (above != this.airState)
			acoustics = this.blockMap.getBlockAcoustics(above, Substrate.CARPET);

		if (acoustics == null || acoustics == AcousticsManager.NOT_EMITTER) {
			// This condition implies that if the carpet is NOT_EMITTER, solving
			// will CONTINUE with the actual block surface the player is walking
			// on NOT_EMITTER carpets will not cause solving to skip

			in = getBlockStateFacade(pos);
			if (in == this.airState) {
				tPos = pos.down();
				final IBlockState below = getBlockState(tPos);
				acoustics = this.blockMap.getBlockAcoustics(below, Substrate.FENCE);
				if (acoustics != null) {
					pos = tPos;
					in = below;
				}
			}

			if (acoustics == null) {
				acoustics = this.blockMap.getBlockAcoustics(in);
			}

			if (acoustics != null && acoustics != AcousticsManager.NOT_EMITTER) {
				// This condition implies that foliage over a NOT_EMITTER block
				// CANNOT PLAY This block most not be executed if the association
				// is a carpet => this block of code is here, not outside this
				// if else group.

				if (above != this.airState) {
					final IAcoustic[] foliage = this.blockMap.getBlockAcoustics(above, Substrate.FOLIAGE);
					if (foliage != null && foliage != AcousticsManager.NOT_EMITTER) {
						acoustics = MyUtils.concatenate(acoustics, foliage);
					}
				}
			}
		} else {
			pos = tPos;
			in = above;
		}

		if (acoustics != null) {
			if (acoustics == AcousticsManager.NOT_EMITTER) {
				// Player has stepped on a non-emitter block as defined in the blockmap
				return null;
			} else {
				// Let's play the fancy acoustics we have defined for the block
				return new Association(in, this.loc.rebase(pos), acoustics);
			}
		} else {
			// No acoustics. Calling logic will default to playing the normal block
			// step sound if available.
			return new Association(in, this.loc.rebase(pos));
		}

	}

}
