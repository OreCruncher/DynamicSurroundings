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
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.footsteps.implem.AcousticsManager;
import org.orecruncher.dsurround.client.footsteps.implem.BlockMap;
import org.orecruncher.dsurround.client.footsteps.implem.Substrate;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;
import org.orecruncher.dsurround.client.footsteps.system.facade.FacadeHelper;
import org.orecruncher.lib.MyUtils;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AcousticResolver {

	protected final IBlockState airState = Blocks.AIR.getDefaultState();
	protected final IBlockAccess world;
	protected final BlockMap blockMap;
	protected final FootStrikeLocation loc;
	protected final double distanceToCenter;

	protected final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	public AcousticResolver(@Nonnull final IBlockAccess world, @Nonnull final BlockMap map,
			@Nonnull final FootStrikeLocation loc, final double distanceToCenter) {
		this.world = world;
		this.blockMap = map;
		this.loc = loc;
		this.distanceToCenter = distanceToCenter;
	}

	protected IBlockState getBlockStateFacade(@Nonnull final Vec3d pos) {
		return FacadeHelper.resolveState(this.loc.getEntity(), getBlockState(pos), this.world, pos, EnumFacing.UP);
	}

	protected IBlockState getBlockState(@Nonnull final Vec3d pos) {
		return this.world.getBlockState(new BlockPos(pos));
	}

	/**
	 * Find an association for an entity, and a location. This will try to find the
	 * best matching block on that location, or near that location, for instance if
	 * the player is walking on the edge of a block when walking over non-emitting
	 * blocks like air or water)
	 *
	 * Returns null if no blocks are valid emitting blocks. Returns a string that
	 * begins with "_NO_ASSOCIATION" if a matching block was found, but has no
	 * association in the blockmap.
	 */
	@Nullable
	public Association findAssociationForEvent() {

		final Vec3d pos = this.loc.getStrikePosition();

		Association worked = resolve(pos);

		// If it didn't work, the player has walked over the air on the border
		// of a block.
		// ------ ------ --> z
		// | o | < player is here
		// wool | air |
		// ------ ------
		// |
		// V z
		if (worked == null) {
			// Create a trigo. mark contained inside the block the player is
			// over
			final EntityLivingBase entity = this.loc.getEntity();
			final BlockPos adj = new BlockPos(pos);
			final double xdang = (entity.posX - adj.getX()) * 2 - 1;
			final double zdang = (entity.posZ - adj.getZ()) * 2 - 1;
			// -1 0 1
			// ------- -1
			// | o |
			// | + | 0 --> x
			// | |
			// ------- 1
			// |
			// V z

			// If the player is at the edge of that
			if (Math.max(MathStuff.abs(xdang), MathStuff.abs(zdang)) > this.distanceToCenter) {
				// Find the maximum absolute value of X or Z
				final boolean isXdangMax = MathStuff.abs(xdang) > MathStuff.abs(zdang);
				// --------------------- ^ maxofZ-
				// | . . |
				// | . . |
				// | o . . |
				// | . . |
				// | . |
				// < maxofX- maxofX+ >
				// Take the maximum border to produce the sound
				if (isXdangMax) {
					// If we are in the positive border, add 1,
					// else subtract 1
					worked = resolve(xdang > 0 ? this.loc.east() : this.loc.west());
				} else {
					worked = resolve(zdang > 0 ? this.loc.south() : this.loc.north());
				}

				// If that didn't work, then maybe the footstep hit in the
				// direction of walking. Try with the other closest block
				if (worked == null) {
					// Take the maximum direction and try with
					// the orthogonal direction of it
					if (isXdangMax) {
						worked = resolve(zdang > 0 ? this.loc.south() : this.loc.north());
					} else {
						worked = resolve(xdang > 0 ? this.loc.east() : this.loc.west());
					}
				}
			}
		}
		return worked;
	}

	@Nullable
	protected Association resolve(@Nonnull Vec3d vec) {
		IBlockState in = null;
		IAcoustic[] acoustics = null;

		Vec3d tPos = vec.add(0, 1, 0);
		final IBlockState above = getBlockState(tPos);

		if (above != this.airState)
			acoustics = this.blockMap.getBlockAcoustics(above, Substrate.CARPET);

		if (acoustics == null || acoustics == AcousticsManager.NOT_EMITTER) {
			// This condition implies that if the carpet is NOT_EMITTER, solving
			// will CONTINUE with the actual block surface the player is walking
			// on NOT_EMITTER carpets will not cause solving to skip

			in = getBlockStateFacade(vec);
			if (in == this.airState) {
				tPos = vec.add(0, -1, 0);
				final IBlockState below = getBlockState(tPos);
				acoustics = this.blockMap.getBlockAcoustics(below, Substrate.FENCE);
				if (acoustics != null) {
					vec = tPos;
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
			vec = tPos;
			in = above;
		}

		if (acoustics != null) {
			if (acoustics == AcousticsManager.NOT_EMITTER) {
				// Player has stepped on a non-emitter block as defined in the blockmap
				return null;
			} else {
				// Let's play the fancy acoustics we have defined for the block
				return new Association(in, this.loc.rebase(new BlockPos(vec)), acoustics);
			}
		} else {
			// No acoustics. Calling logic will default to playing the normal block
			// step sound if available.
			return new Association(in, this.loc.rebase(new BlockPos(vec)));
		}

	}

}
