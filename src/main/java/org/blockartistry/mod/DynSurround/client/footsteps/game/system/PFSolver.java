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

package org.blockartistry.mod.DynSurround.client.footsteps.game.system;

import java.util.Locale;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.implem.ConfigOptions;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.EventType;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.IOptions.Option;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IIsolator;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.ISolver;
import org.blockartistry.mod.DynSurround.compat.MCHelper;
import org.blockartistry.mod.DynSurround.util.MathStuff;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
public class PFSolver implements ISolver {
	private final IIsolator isolator;

	public PFSolver(final IIsolator isolator) {
		this.isolator = isolator;
	}

	@Override
	public void playAssociation(final EntityPlayer ply, final Association assos, final EventType eventType) {
		if (assos != null && !assos.isNotEmitter()) {
			if (assos.getNoAssociation()) {
				this.isolator.getDefaultStepPlayer().playStep(ply, assos);
			} else {
				this.isolator.getAcoustics().playAcoustic(ply, assos, eventType);
			}
		}
	}

	@Override
	public Association findAssociationForPlayer(final EntityPlayer ply, final double verticalOffsetAsMinus,
			final boolean isRightFoot) {
		final int yy = MathHelper.floor_double(ply.getEntityBoundingBox().minY - 0.1d - verticalOffsetAsMinus);
		final double rot = MathStuff.toRadians(MathHelper.wrapDegrees(ply.rotationYaw));
		final double xn = MathStuff.cos(rot);
		final double zn = MathStuff.sin(rot);
		final float feetDistanceToCenter = 0.2f * (isRightFoot ? -1 : 1);
		final int xx = MathHelper.floor_double(ply.posX + xn * feetDistanceToCenter);
		final int zz = MathHelper.floor_double(ply.posZ + zn * feetDistanceToCenter);

		return findAssociationForLocation(ply, new BlockPos(xx, yy, zz));
	}

	@Override
	public Association findAssociationForPlayer(final EntityPlayer ply, final double verticalOffsetAsMinus) {
		final int yy = MathHelper.floor_double(ply.posY - 0.1d - ply.getYOffset() - verticalOffsetAsMinus);
		final int xx = MathHelper.floor_double(ply.posX);
		final int zz = MathHelper.floor_double(ply.posZ);
		return findAssociationForLocation(ply, new BlockPos(xx, yy, zz));
	}

	@Override
	public Association findAssociationForLocation(final EntityPlayer player, final BlockPos pos) {
		if (Math.abs(player.motionY) < 0.02)
			return null; // Don't play sounds on every tiny bounce
		if (player.isInWater())
			ModLog.debug(
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
			if (Math.max(Math.abs(xdang), Math.abs(zdang)) > 0.2f) {
				// Find the maximum absolute value of X or Z
				boolean isXdangMax = Math.abs(xdang) > Math.abs(zdang);
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
					worked = findAssociationForBlock(zdang > 0 ? pos.up() : pos.down());
				}

				// If that didn't work, then maybe the footstep hit in the
				// direction of walking
				// Try with the other closest block
				if (worked == null) { // Take the maximum direction and try with
										// the orthogonal direction of it
					if (isXdangMax) {
						worked = findAssociationForBlock(zdang > 0 ? pos.up() : pos.down());
					} else {
						worked = findAssociationForBlock(xdang > 0 ? pos.east() : pos.west());
					}
				}
			}
		}
		return worked;
	}

	@Override
	public Association findAssociationForBlock(final BlockPos immutablePos) {
		final World world = EnvironState.getWorld();
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(immutablePos);
		IBlockState in = world.getBlockState(pos);
		final IBlockState above = world.getBlockState(pos.up());

		String association = isolator.getBlockMap().getBlockMapSubstrate(above, "carpet");

		// PFLog.debugf("Walking on block: %0 -- Being in block: %1", in,
		// above);

		if (association == null || association.equals("NOT_EMITTER")) {
			// This condition implies that if the carpet is NOT_EMITTER, solving
			// will CONTINUE with the actual block surface the player is walking
			// on
			// > NOT_EMITTER carpets will not cause solving to skip

			if (world.isAirBlock(pos)) {
				final IBlockState below = world.getBlockState(pos.down());
				association = this.isolator.getBlockMap().getBlockMapSubstrate(below, "bigger");
				if (association != null) {
					pos.move(EnumFacing.DOWN);
					in = below;
					ModLog.debug("Fence detected: " + association);
				}
			}

			if (association == null) {
				association = isolator.getBlockMap().getBlockMap(in);
			}

			if (association != null && !association.equals("NOT_EMITTER")) {
				// This condition implies that foliage over a NOT_EMITTER block
				// CANNOT PLAY
				// This block most not be executed if the association is a
				// carpet
				// => this block of code is here, not outside this if else
				// group.

				String foliage = this.isolator.getBlockMap().getBlockMapSubstrate(above, "foliage");
				if (foliage != null && !foliage.equals("NOT_EMITTER")) {
					association = association + "," + foliage;
					ModLog.debug("Foliage detected: " + foliage);
				}
			}
		} else {
			pos.move(EnumFacing.UP);
			in = above;
			ModLog.debug("Carpet detected: " + association);
		}

		if (association != null) {
			if (association.equals("NOT_EMITTER")) {
				// if (in.getBlock() != Blocks.air) { // air block
				// PFLog.debugf("Not emitter for %0 : %1", in);
				// }
				return null; // Player has stepped on a non-emitter block as
								// defined in the blockmap
			} else {
				// PFLog.debugf("Found association for %0 : %1 : %2", in,
				// association);
				return (new Association(in, pos)).setAssociation(association);
			}
		} else {
			String primitive = resolvePrimitive(in);
			if (primitive != null) {
				if (primitive.equals("NOT_EMITTER")) {
					// PFLog.debugf("Primitive for %0 : %1 : %2 is NOT_EMITTER!
					// Following behavior is uncertain.", in, primitive);
					return null;
				}

				// PFLog.debugf("Found primitive for %0 : %1 : %2", in,
				// primitive);
				return (new Association(in, pos)).setPrimitive(primitive);
			} else {
				// PFLog.debugf("No association for %0 : %1", in);
				return (new Association(in, pos)).setNoAssociation();
			}
		}
	}

	private String resolvePrimitive(final IBlockState state) {

		if (state.getMaterial() == Material.AIR)
			return "NOT_EMITTER";

		final SoundType type = MCHelper.getSoundType(state);

		if (type == null)
			return "NOT_EMITTER";

		final String soundName;
		boolean flag = false;

		if (type.getStepSound() == null || type.getStepSound().getSoundName().getResourcePath().isEmpty()) {
			soundName = "UNDEFINED";
			flag = true;
		} else
			soundName = type.getStepSound().getSoundName().toString();

		final String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", type.getVolume(), type.getPitch());

		// Check for primitive in register
		String primitive = this.isolator.getPrimitiveMap().getPrimitiveMapSubstrate(soundName, substrate);
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
			ModLog.debug("Primitive found for " + soundName + ":" + substrate);
			return primitive;
		}

		ModLog.debug("No primitive for " + soundName + ":" + substrate);
		return null;
	}

	@Override
	public boolean playSpecialStoppingConditions(final EntityPlayer ply) {
		if (ply.isInWater()) {
			final float volume = MathHelper.sqrt_double(
					ply.motionX * ply.motionX * 0.2d + ply.motionY * ply.motionY + ply.motionZ * ply.motionZ * 0.2d)
					* 0.35f;
			final ConfigOptions options = new ConfigOptions();
			options.getMap().put(Option.GLIDING_VOLUME, volume > 1 ? 1 : volume);
			// material water, see EntityLivingBase line 286
			this.isolator.getAcoustics().playAcoustic(ply, "_SWIM",
					ply.isInsideOfMaterial(Material.WATER) ? EventType.SWIM : EventType.WALK, options);
			return true;
		}

		return false;
	}

	@Override
	public boolean hasSpecialStoppingConditions(final EntityPlayer ply) {
		return ply.isInWater();
	}

	@Override
	public Association findAssociationForBlock(final BlockPos pos, String strategy) {
		if (!strategy.equals("find_messy_foliage"))
			return null;

		final World world = EnvironState.getWorld();
		final IBlockState above = world.getBlockState(pos.up());

		String association = null;
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

		String foliage = this.isolator.getBlockMap().getBlockMapSubstrate(above, "foliage");
		if (foliage != null && !foliage.equals("NOT_EMITTER")) {
			// we discard the normal block association, and mark the foliage as
			// detected
			// association = association + "," + foliage;
			association = foliage;
			String isMessy = this.isolator.getBlockMap().getBlockMapSubstrate(above, "messy");

			if (isMessy != null && isMessy.equals("MESSY_GROUND"))
				found = true;
		}
		/*
		 * } // else { the information is discarded anyways, the method returns
		 * null or no association } } else // Is a carpet return null;
		 */

		if (found && association != null) {
			return association.equals("NOT_EMITTER") ? null : (new Association()).setAssociation(association);
		}
		return null;
	}
}