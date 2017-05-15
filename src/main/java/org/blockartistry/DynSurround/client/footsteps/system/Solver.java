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

package org.blockartistry.DynSurround.client.footsteps.system;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.DynSurround.client.footsteps.implem.ConfigOptions;
import org.blockartistry.DynSurround.client.footsteps.interfaces.EventType;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IOptions.Option;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.MCHelper;
import org.blockartistry.lib.MathStuff;
import org.blockartistry.lib.MyUtils;
import org.blockartistry.lib.WorldUtils;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Solves in-world locations and players into associations. Associations are an
 * extension of IAcoustic names, with some special codes. They are derived from
 * the blockmap and defined with these values:<br>
 * <br>
 * The association null is derived from blockmap "NOT_EMITTER" and means the
 * block is NOT MEANT to emit sounds (not equal to "no sound").<br>
 * The association "_NO_ASSOCIATION:xx:yy:zz" is derived from AN ABSENCE of an
 * entry in the blockmap (after solving missing metadata and carpets). xx,yy,zz
 * is the location of the incriminated block.<br>
 * Any other association string returned by the findAssociation* methods
 * correspond to an IAcoustic name.
 * 
 * @author Hurry
 */
@SideOnly(Side.CLIENT)
public class Solver {

	private final Isolator isolator;

	public Solver(@Nonnull final Isolator isolator) {
		this.isolator = isolator;
	}

	/**
	 * Play an association.
	 */
	public void playAssociation(@Nonnull final EntityPlayer ply, @Nullable final Association assos,
			@Nonnull final EventType eventType) {
		if (assos != null && !assos.isNotEmitter()) {
			if (assos.getNoAssociation()) {
				this.isolator.getDefaultStepPlayer().playStep(ply, assos);
			} else {
				this.isolator.getAcoustics().playAcoustic(ply, assos, eventType);
			}
		}
	}

	/**
	 * Find an association for a player particular foot. This will fetch the
	 * player angle and use it as a basis to find out what block is below their
	 * feet (or which block is likely to be below their feet if the player is
	 * walking on the edge of a block when walking over non-emitting blocks like
	 * air or water).<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block
	 * was found, but has no association in the blockmap.
	 */
	@Nonnull
	public Association findAssociationForPlayer(@Nonnull final EntityPlayer player, final double verticalOffsetAsMinus,
			final boolean isRightFoot) {
		// Moved from routine below - why do all this calculation just to toss
		// it away
		if (MathStuff.abs(player.motionY) < 0.02)
			return null; // Don't play sounds on every tiny bounce

		final float rotDegrees = MathStuff.wrapDegrees(player.rotationYaw);
		final double rot = MathStuff.toRadians(rotDegrees);
		final double xn = MathStuff.cos(rot);
		final double zn = MathStuff.sin(rot);
		final float feetDistanceToCenter = 0.2f * (isRightFoot ? -1 : 1);

		final double xx = player.posX + xn * feetDistanceToCenter;
		final double minY = player.getEntityBoundingBox().minY;
		final double zz = player.posZ + zn * feetDistanceToCenter;
		final BlockPos pos = new BlockPos(xx, minY - 0.1D - verticalOffsetAsMinus, zz);

		final Association result = addSoundOverlay(findAssociationForLocation(player, pos));
		if (ModOptions.enableFootprints && result != null && !player.isJumping)
			result.setStepLocation(new Vec3d(xx, minY, zz), rotDegrees, isRightFoot);

		return result;
	}

	/**
	 * Find an association for a player, and a location. This will try to find
	 * the best matching block on that location, or near that location, for
	 * instance if the player is walking on the edge of a block when walking
	 * over non-emitting blocks like air or water)<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block
	 * was found, but has no association in the blockmap.
	 */
	@Nonnull
	protected Association findAssociationForLocation(@Nonnull final EntityPlayer player, @Nonnull final BlockPos pos) {
		// if (MathStuff.abs(player.motionY) < 0.02)
		// return null; // Don't play sounds on every tiny bounce

		if (player.isInWater())
			DSurround.log().debug(
					"WARNING!!! Playing a sound while in the water! This is supposed to be halted by the stopping conditions!!");

		Association worked = findAssociationForBlock(pos);

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
			double xdang = (player.posX - pos.getX()) * 2 - 1;
			double zdang = (player.posZ - pos.getZ()) * 2 - 1;
			// -1 0 1
			// ------- -1
			// | o |
			// | + | 0 --> x
			// | |
			// ------- 1
			// |
			// V z

			// If the player is at the edge of that
			if (Math.max(MathStuff.abs(xdang), MathStuff.abs(zdang)) > 0.2f) {
				// Find the maximum absolute value of X or Z
				boolean isXdangMax = MathStuff.abs(xdang) > MathStuff.abs(zdang);
				// --------------------- ^ maxofZ-
				// | . . |
				// | . . |
				// | o . . |
				// | . . |
				// | . |
				// < maxofX- maxofX+ >
				// Take the maximum border to produce the sound
				if (isXdangMax) { // If we are in the positive border, add 1,
									// else subtract 1
					worked = findAssociationForBlock(xdang > 0 ? pos.east() : pos.west());
				} else {
					worked = findAssociationForBlock(zdang > 0 ? pos.south() : pos.north());
				}

				// If that didn't work, then maybe the footstep hit in the
				// direction of walking
				// Try with the other closest block
				if (worked == null) { // Take the maximum direction and try with
										// the orthogonal direction of it
					if (isXdangMax) {
						worked = findAssociationForBlock(zdang > 0 ? pos.south() : pos.north());
					} else {
						worked = findAssociationForBlock(xdang > 0 ? pos.east() : pos.west());
					}
				}
			}
		}
		return worked;
	}

	/**
	 * Find an association for a certain block assuming the player is standing
	 * on it. This will sometimes select the block above because some block act
	 * like carpets. This also applies when the block targeted by the location
	 * is actually not emitting, such as lilypads on water.<br>
	 * <br>
	 * Returns null if the block is not a valid emitting block (this causes the
	 * engine to continue looking for valid blocks). This also happens if the
	 * carpet is non-emitting.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if the block is
	 * valid, but has no association in the blockmap. If the carpet was
	 * selected, this solves to the carpet.
	 */
	@Nonnull
	public Association findAssociationForBlock(@Nonnull BlockPos pos) {
		final World world = EnvironState.getWorld();
		IBlockState in = WorldUtils.getBlockState(world, pos);
		BlockPos tPos = pos.up();
		final IBlockState above = WorldUtils.getBlockState(world, tPos);

		IAcoustic[] association = isolator.getBlockMap().getBlockSubstrateAcoustics(above, tPos, "carpet");

		// PFLog.debugf("Walking on block: %0 -- Being in block: %1", in,
		// above);

		if (association == null || association == AcousticsManager.NOT_EMITTER) {
			// This condition implies that if the carpet is NOT_EMITTER, solving
			// will CONTINUE with the actual block surface the player is walking
			// on
			// > NOT_EMITTER carpets will not cause solving to skip

			if (WorldUtils.isAirBlock(in)) {
				tPos = pos.down();
				final IBlockState below = WorldUtils.getBlockState(world, tPos);
				association = this.isolator.getBlockMap().getBlockSubstrateAcoustics(below, tPos, "bigger");
				if (association != null) {
					pos = tPos;
					in = below;
					DSurround.log().debug("Fence detected");
				}
			}

			if (association == null) {
				association = isolator.getBlockMap().getBlockAcoustics(in, pos);
			}

			if (association != null && association != AcousticsManager.NOT_EMITTER) {
				// This condition implies that foliage over a NOT_EMITTER block
				// CANNOT PLAY
				// This block most not be executed if the association is a
				// carpet
				// => this block of code is here, not outside this if else
				// group.

				IAcoustic[] foliage = this.isolator.getBlockMap().getBlockSubstrateAcoustics(above, pos.up(),
						"foliage");
				if (foliage != null && foliage != AcousticsManager.NOT_EMITTER) {
					association = MyUtils.concatenate(association, foliage);
					DSurround.log().debug("Foliage detected");
				}
			}
		} else {
			pos = tPos;
			in = above;
			DSurround.log().debug("Carpet detected");
		}

		if (association != null) {
			if (association == AcousticsManager.NOT_EMITTER) {
				// if (in.getBlock() != Blocks.air) { // air block
				// PFLog.debugf("Not emitter for %0 : %1", in);
				// }
				return null; // Player has stepped on a non-emitter block as
								// defined in the blockmap
			} else {
				// PFLog.debugf("Found association for %0 : %1 : %2", in,
				// association);
				return new Association(in, pos, association);
			}
		} else {
			IAcoustic[] primitive = resolvePrimitive(in);
			if (primitive != null) {
				if (primitive == AcousticsManager.NOT_EMITTER) {
					// PFLog.debugf("Primitive for %0 : %1 : %2 is NOT_EMITTER!
					// Following behavior is uncertain.", in, primitive);
					return null;
				}

				// PFLog.debugf("Found primitive for %0 : %1 : %2", in,
				// primitive);
				return new Association(in, pos, primitive);
			} else {
				// PFLog.debugf("No association for %0 : %1", in);
				return new Association(in, pos);
			}
		}
	}

	@Nonnull
	private IAcoustic[] resolvePrimitive(@Nonnull final IBlockState state) {

		if (state.getBlock() == Blocks.AIR)
			return AcousticsManager.NOT_EMITTER;

		final SoundType type = MCHelper.getSoundType(state);

		if (type == null)
			return AcousticsManager.NOT_EMITTER;

		final String soundName;
		boolean flag = false;

		if (type.getStepSound() == null || type.getStepSound().getSoundName().getResourcePath().isEmpty()) {
			soundName = "UNDEFINED";
			flag = true;
		} else
			soundName = type.getStepSound().getSoundName().toString();

		final String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", type.getVolume(), type.getPitch());

		// Check for primitive in register
		IAcoustic[] primitive = this.isolator.getPrimitiveMap().getPrimitiveMapSubstrate(soundName, substrate);
		if (primitive == null) {
			if (flag) {
				primitive = this.isolator.getPrimitiveMap().getPrimitiveMapSubstrate(soundName, "break_" + soundName); // Check
																														// sound
			}
			if (primitive == null) {
				primitive = this.isolator.getPrimitiveMap().getPrimitiveMap(soundName);
			}
		}

		if (primitive != null) {
			DSurround.log().debug("Primitive found for %s: %s", soundName, substrate);
			return primitive;
		}

		DSurround.log().debug("No primitive for %s: %s", soundName, substrate);
		return null;
	}

	/**
	 * Play special sounds that must stop the usual footstep figuring things out
	 * process.
	 */
	public boolean playSpecialStoppingConditions(@Nonnull final EntityPlayer ply) {
		if (ply.isInWater()) {
			final float volume = (float) MathStuff
					.sqrt(ply.motionX * ply.motionX + ply.motionY * ply.motionY + ply.motionZ * ply.motionZ) * 1.25F;
			final ConfigOptions options = new ConfigOptions();
			options.getMap().put(Option.GLIDING_VOLUME, volume > 1 ? 1 : volume);
			// material water, see EntityLivingBase line 286
			this.isolator.getAcoustics().playAcoustic(ply, AcousticsManager.SWIM,
					ply.isInsideOfMaterial(Material.WATER) ? EventType.SWIM : EventType.WALK, options);
			return true;
		}

		return false;
	}

	/**
	 * Tells if footsteps can be played.
	 */
	public boolean hasSpecialStoppingConditions(@Nonnull final EntityPlayer ply) {
		return ply.isInWater();
	}

	/**
	 * Find an association for a certain block assuming the player is standing
	 * on it, using a custom strategy which strategies are defined by the
	 * solver.
	 */
	@Nonnull
	public Association findAssociationMessyFoliage(@Nonnull final BlockPos pos) {

		final World world = EnvironState.getWorld();
		final BlockPos up = pos.up();
		final IBlockState above = WorldUtils.getBlockState(world, up);

		IAcoustic[] association = null;
		boolean found = false;
		// Try to see if the block above is a carpet...
		/*
		 * String association =
		 * this.isolator.getBlockMap().getBlockMapSubstrate(
		 * PF172Helper.nameOf(xblock), xmetadata, "carpet");
		 * 
		 * if (association == null || association.equals("NOT_EMITTER")) { //
		 * This condition implies that // if the carpet is NOT_EMITTER, solving
		 * will CONTINUE with the actual // block surface the player is walking
		 * on // > NOT_EMITTER carpets will not cause solving to skip
		 * 
		 * // Not a carpet association =
		 * this.isolator.getBlockMap().getBlockMap(PF172Helper.nameOf(block),
		 * metadata);
		 * 
		 * if (association != null && !association.equals("NOT_EMITTER")) { //
		 * This condition implies that // foliage over a NOT_EMITTER block
		 * CANNOT PLAY
		 * 
		 * // This block most not be executed if the association is a carpet //
		 * => this block of code is here, not outside this if else group.
		 */

		IAcoustic[] foliage = this.isolator.getBlockMap().getBlockSubstrateAcoustics(above, up, "foliage");
		if (foliage != null && foliage != AcousticsManager.NOT_EMITTER) {
			// we discard the normal block association, and mark the foliage as
			// detected
			// association = association + "," + foliage;
			association = foliage;
			IAcoustic[] isMessy = this.isolator.getBlockMap().getBlockSubstrateAcoustics(above, up, "messy");

			if (isMessy != null && isMessy == AcousticsManager.MESSY_GROUND)
				found = true;
		}
		/*
		 * } // else { the information is discarded anyways, the method returns
		 * null or no association } } else // Is a carpet return null;
		 */

		if (found && association != null) {
			return association == AcousticsManager.NOT_EMITTER ? null : new Association(association);
		}
		return null;
	}

	/**
	 * Adds additional sound overlays to the acoustic based on other environment
	 * aspects, such as armor being worn.
	 */
	@Nonnull
	public Association addSoundOverlay(@Nullable Association assoc) {

		final IAcoustic armorAddon = this.isolator.getAcoustics()
				.getAcoustic(EnvironState.getPlayerArmorClass().getAcoustic());
		IAcoustic footAddon = this.isolator.getAcoustics()
				.getAcoustic(EnvironState.getPlayerFootArmorClass().getFootAcoustic());

		if (armorAddon == null && footAddon == null)
			return assoc;

		// Eliminate duplicates
		if (armorAddon == footAddon)
			footAddon = null;

		if (assoc == null)
			assoc = new Association();
		if (armorAddon != null)
			assoc.add(armorAddon);
		if (footAddon != null)
			assoc.add(footAddon);

		return assoc;
	}

}