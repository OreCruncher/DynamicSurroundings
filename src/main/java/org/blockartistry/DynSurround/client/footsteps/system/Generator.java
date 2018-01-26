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
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.DynSurround.client.footsteps.implem.ConfigOptions;
import org.blockartistry.DynSurround.client.footsteps.implem.Variator;
import org.blockartistry.DynSurround.client.footsteps.implem.Substrate;
import org.blockartistry.DynSurround.client.footsteps.interfaces.EventType;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IOptions.Option;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.facade.FacadeHelper;
import org.blockartistry.DynSurround.registry.ArmorClass;
import org.blockartistry.lib.MCHelper;
import org.blockartistry.lib.MyUtils;
import org.blockartistry.lib.TimeUtils;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.math.MathStuff;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Generator {

	protected static final Random RANDOM = XorShiftRandom.current();
	protected static final IBlockState AIR_STATE = Blocks.AIR.getDefaultState();

	protected final Isolator isolator;
	protected final Variator VAR;

	protected float dmwBase;
	protected float dwmYChange;
	protected double yPosition;

	protected boolean isFlying;
	protected float fallDistance;

	protected float lastReference;
	protected boolean isImmobile;
	protected long timeImmobile;

	protected boolean isRightFoot;

	protected double xMovec;
	protected double zMovec;
	protected boolean scalStat;
	protected boolean stepThisFrame;

	protected boolean isMessyFoliage;
	protected long brushesTime;

	// We calc our own because of inconsistencies with Minecraft
	protected double distanceWalkedOnStepModified;

	public Generator(@Nonnull final Isolator isolator, @Nonnull final Variator var) {
		this.isolator = isolator;
		this.VAR = var;
	}

	public void generateFootsteps(@Nonnull final EntityLivingBase entity) {
		simulateFootsteps(entity);
		simulateAirborne(entity);
		simulateBrushes(entity);

		if (ModOptions.footstepsSoundFactor > 0)
			entity.nextStepDistance = Integer.MAX_VALUE;
		else if (entity.nextStepDistance == Integer.MAX_VALUE)
			entity.nextStepDistance = 0;
	}

	protected boolean stoppedImmobile(float reference) {
		final long current = TimeUtils.currentTimeMillis();
		final float diff = this.lastReference - reference;
		this.lastReference = reference;
		if (!this.isImmobile && diff == 0f) {
			this.timeImmobile = current;
			this.isImmobile = true;
		} else if (this.isImmobile && diff != 0f) {
			this.isImmobile = false;
			return current - timeImmobile > VAR.IMMOBILE_DURATION;
		}

		return false;
	}

	protected void updateWalkedOnStep(@Nonnull final EntityLivingBase entity) {
		final double dX = entity.posX - entity.prevPosX;
		final double dY = entity.posY - entity.prevPosY;
		final double dZ = entity.posZ - entity.prevPosZ;
		this.distanceWalkedOnStepModified += Math.sqrt(dX * dX + dY * dY + dZ * dZ);
	}

	protected void simulateFootsteps(@Nonnull final EntityLivingBase ply) {

		updateWalkedOnStep(ply);

		final float distanceReference = (float) this.distanceWalkedOnStepModified;
		// final float distanceReference = ply.distanceWalkedOnStepModified;

		this.stepThisFrame = false;

		if (this.dmwBase > distanceReference) {
			this.dmwBase = 0;
			this.dwmYChange = 0;
		}

		final double movX = ply.motionX;
		final double movZ = ply.motionZ;

		double scal = movX * this.xMovec + movZ * this.zMovec;
		if (this.scalStat != scal < 0.001f) {
			this.scalStat = !this.scalStat;

			if (this.scalStat && VAR.PLAY_WANDER && !this.hasSpecialStoppingConditions(ply)) {
				playSinglefoot(ply, 0d, EventType.WANDER, this.isRightFoot);
			}
		}

		this.xMovec = movX;
		this.zMovec = movZ;

		if (ply.onGround || ply.isInWater() || ply.isOnLadder()) {
			EventType event = null;

			float dwm = distanceReference - this.dmwBase;
			final boolean immobile = stoppedImmobile(distanceReference);
			if (immobile && !ply.isOnLadder()) {
				dwm = 0;
				this.dmwBase = distanceReference;
			}

			float distance = 0f;

			if (ply.isOnLadder() && !ply.onGround) {
				distance = VAR.STRIDE_LADDER;
			} else if (!ply.isInWater() && MathStuff.abs(this.yPosition - ply.posY) > 0.4d) {
				// This ensures this does not get recorded as landing, but as a
				// step
				if (this.yPosition < ply.posY) { // Going upstairs
					distance = VAR.STRIDE_STAIR;
					event = speedDisambiguator(ply, EventType.UP, EventType.UP_RUN);
				} else if (!ply.isSneaking()) { // Going downstairs
					distance = -1f;
					event = speedDisambiguator(ply, EventType.DOWN, EventType.DOWN_RUN);
				}

				this.dwmYChange = distanceReference;

			} else {
				distance = VAR.STRIDE;
			}

			if (event == null) {
				event = speedDisambiguator(ply, EventType.WALK, EventType.RUN);
			}

			distance = reevaluateDistance(event, distance);

			if (dwm > distance) {
				produceStep(ply, event, 0F);
				stepped(ply, event);
				this.dmwBase = distanceReference;
			}
		}

		// This fixes an issue where the value is evaluated
		// while the player is between two steps in the air
		// while descending stairs
		if (ply.onGround) {
			this.yPosition = ply.posY;
		}
	}

	protected void stepped(@Nonnull final EntityLivingBase ply, @Nonnull final EventType event) {
	}

	protected float reevaluateDistance(@Nonnull final EventType event, final float distance) {
		return distance;
	}

	protected void produceStep(@Nonnull final EntityLivingBase ply, @Nonnull final EventType event) {
		produceStep(ply, event, 0d);
	}

	protected void produceStep(@Nonnull final EntityLivingBase ply, @Nullable EventType event,
			final double verticalOffsetAsMinus) {
		if (!this.playSpecialStoppingConditions(ply)) {
			if (event == null)
				event = speedDisambiguator(ply, EventType.WALK, EventType.RUN);
			playSinglefoot(ply, verticalOffsetAsMinus, event, this.isRightFoot);
			this.isRightFoot = !this.isRightFoot;
		}

		this.stepThisFrame = true;
	}

	protected void simulateAirborne(@Nonnull final EntityLivingBase ply) {
		if ((ply.onGround || ply.isOnLadder()) == this.isFlying) {
			this.isFlying = !this.isFlying;
			simulateJumpingLanding(ply);
		}

		if (this.isFlying)
			this.fallDistance = ply.fallDistance;
	}

	protected void simulateJumpingLanding(@Nonnull final EntityLivingBase ply) {
		if (this.hasSpecialStoppingConditions(ply))
			return;

		final boolean isJumping = ply.isJumping;

		if (this.isFlying && isJumping) { // ply.isJumping)
			if (VAR.EVENT_ON_JUMP) {
				double speed = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;

				if (speed < VAR.SPEED_TO_JUMP_AS_MULTIFOOT) { // STILL JUMP
					playMultifoot(ply, 0.4d, EventType.JUMP); // 2 -
																// 0.7531999805212d
																// (magic number
																// for vertical
																// offset?)
				} else {
					playSinglefoot(ply, 0.4d, EventType.JUMP, this.isRightFoot); // RUNNING
					// JUMP
					// Do not toggle foot: After landing sounds, the first foot
					// will be same as the one used to jump.
				}
			}
		} else if (!this.isFlying) {
			if (this.fallDistance > VAR.LAND_HARD_DISTANCE_MIN) {
				playMultifoot(ply, 0d, EventType.LAND); // Always assume the
														// player lands on their
														// two feet
				// Do not toggle foot: After landing sounds, the first foot will
				// be same as the one used to jump.
			} else if (!this.stepThisFrame && !ply.isSneaking()) {
				playSinglefoot(ply, 0d, speedDisambiguator(ply, EventType.CLIMB, EventType.CLIMB_RUN),
						this.isRightFoot);
				this.isRightFoot = !this.isRightFoot;
			}

		}
	}

	protected EventType speedDisambiguator(@Nonnull final EntityLivingBase ply, @Nonnull final EventType walk,
			@Nonnull final EventType run) {
		final double velocity = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
		return velocity > VAR.SPEED_TO_RUN ? run : walk;
	}

	protected void simulateBrushes(@Nonnull final EntityLivingBase ply) {
		final long current = TimeUtils.currentTimeMillis();
		if (this.brushesTime > current)
			return;

		this.brushesTime = current + 100;

		if ((ply.motionX == 0d && ply.motionZ == 0d) || ply.isSneaking())
			return;

		final int yy = MathStuff.floor(ply.posY - 0.1d - ply.getYOffset() - (ply.onGround ? 0d : 0.25d));
		final Association assos = this.findAssociationMessyFoliage(new BlockPos(ply.posX, yy, ply.posZ));
		if (assos != null) {
			if (!this.isMessyFoliage) {
				this.isMessyFoliage = true;
				this.playAssociation(ply, assos, EventType.WALK);
			}
		} else {
			this.isMessyFoliage = false;
		}
	}

	protected void playSinglefoot(@Nonnull final EntityLivingBase ply, final double verticalOffsetAsMinus,
			@Nonnull final EventType eventType, final boolean foot) {
		final Association assos = this.findAssociationForPlayer(ply, verticalOffsetAsMinus, foot);
		this.playAssociation(ply, assos, eventType);
	}

	protected void playMultifoot(@Nonnull final EntityLivingBase ply, final double verticalOffsetAsMinus,
			final EventType eventType) {
		// STILL JUMP
		final Association leftFoot = this.findAssociationForPlayer(ply, verticalOffsetAsMinus, false);
		final Association rightFoot = this.findAssociationForPlayer(ply, verticalOffsetAsMinus, true);
		this.playAssociation(ply, leftFoot, eventType);
		this.playAssociation(ply, rightFoot, eventType);
	}

	protected float scalex(final float number, final float min, final float max) {
		return MathStuff.clamp((number - min) / (max - min), 0.0F, 1.0F);
	}

	/*
	 * Former Solver code moved into Generator.
	 */

	/**
	 * Play an association.
	 */
	protected void playAssociation(@Nonnull final EntityLivingBase ply, @Nullable final Association assos,
			@Nonnull final EventType eventType) {
		if (assos != null && !assos.isNotEmitter()) {
			if (assos.getNoAssociation()) {
				this.isolator.getDefaultStepPlayer().playStep(ply, assos);
			} else {
				this.isolator.getAcoustics().playAcoustic(ply, assos, eventType);
			}
		}
	}

	protected boolean hasFootstepImprint(@Nullable final IBlockState state, @Nonnull final BlockPos pos) {
		if (state != null) {
			final IBlockState footstepState = FacadeHelper.resolveState(state, EnvironState.getWorld(), pos,
					EnumFacing.UP);
			return ClientRegistry.FOOTSTEPS.hasFootprint(footstepState);
		}

		return false;
	}

	protected boolean hasFootstepImprint(@Nonnull final Vec3d pos) {
		// Check the block above to see if it has a footprint. Intended to handle things
		// like snow on stone.
		BlockPos blockPos = new BlockPos(pos).up();
		IBlockState state = WorldUtils.getBlockState(EnvironState.getWorld(), blockPos);
		if (state != null && hasFootstepImprint(state, blockPos))
			return true;

		// If the block above blocks movement then it's not possible to lay
		// down a footprint.
		if (state.getMaterial().blocksMovement())
			return false;

		// Check the requested block
		blockPos = new BlockPos(pos);
		state = WorldUtils.getBlockState(EnvironState.getWorld(), blockPos);
		if (state != null) {
			return hasFootstepImprint(state, blockPos);
		}

		return false;
	}

	/**
	 * Find an association for a player particular foot. This will fetch the player
	 * angle and use it as a basis to find out what block is below their feet (or
	 * which block is likely to be below their feet if the player is walking on the
	 * edge of a block when walking over non-emitting blocks like air or water).<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block was
	 * found, but has no association in the blockmap.
	 */
	@Nonnull
	protected Association findAssociationForPlayer(@Nonnull final EntityLivingBase player,
			final double verticalOffsetAsMinus, final boolean isRightFoot) {
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

		final Association result = addSoundOverlay(player, findAssociationForLocation(player, pos));
		if (result != null && !player.isJumping) {
			final Vec3d printLocation = new Vec3d(xx, minY, zz);
			if (hasFootstepImprint(printLocation.addVector(0D, -0.5D, 0D)))
				result.generatePrint(player, printLocation, rotDegrees, isRightFoot);
		}
		return result;
	}

	/**
	 * Find an association for a player, and a location. This will try to find the
	 * best matching block on that location, or near that location, for instance if
	 * the player is walking on the edge of a block when walking over non-emitting
	 * blocks like air or water)<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block was
	 * found, but has no association in the blockmap.
	 */
	@Nonnull
	protected Association findAssociationForLocation(@Nonnull final EntityLivingBase player,
			@Nonnull final BlockPos pos) {

		final World world = player.getEntityWorld();

		if (player.isInWater())
			DSurround.log().debug(
					"WARNING!!! Playing a sound while in the water! This is supposed to be halted by the stopping conditions!!");

		Association worked = findAssociationForBlock(world, pos);

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
					worked = findAssociationForBlock(world, xdang > 0 ? pos.east() : pos.west());
				} else {
					worked = findAssociationForBlock(world, zdang > 0 ? pos.south() : pos.north());
				}

				// If that didn't work, then maybe the footstep hit in the
				// direction of walking
				// Try with the other closest block
				if (worked == null) { // Take the maximum direction and try with
										// the orthogonal direction of it
					if (isXdangMax) {
						worked = findAssociationForBlock(world, zdang > 0 ? pos.south() : pos.north());
					} else {
						worked = findAssociationForBlock(world, xdang > 0 ? pos.east() : pos.west());
					}
				}
			}
		}
		return worked;
	}

	/**
	 * Find an association for a certain block assuming the player is standing on
	 * it. This will sometimes select the block above because some block act like
	 * carpets. This also applies when the block targeted by the location is
	 * actually not emitting, such as lilypads on water.<br>
	 * <br>
	 * Returns null if the block is not a valid emitting block (this causes the
	 * engine to continue looking for valid blocks). This also happens if the carpet
	 * is non-emitting.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if the block is valid,
	 * but has no association in the blockmap. If the carpet was selected, this
	 * solves to the carpet.
	 */
	@Nonnull
	protected Association findAssociationForBlock(@Nonnull final World world, @Nonnull BlockPos pos) {
		IBlockState in = WorldUtils.getBlockState(world, pos);
		BlockPos tPos = pos.up();
		final IBlockState above = WorldUtils.getBlockState(world, tPos);

		IAcoustic[] association = null;

		if (above != AIR_STATE)
			association = ClientRegistry.FOOTSTEPS.getBlockMap().getBlockSubstrateAcoustics(world, above, tPos, Substrate.CARPET);

		if (association == null || association == AcousticsManager.NOT_EMITTER) {
			// This condition implies that if the carpet is NOT_EMITTER, solving
			// will CONTINUE with the actual block surface the player is walking
			// on NOT_EMITTER carpets will not cause solving to skip

			if (in == AIR_STATE) {
				tPos = pos.down();
				final IBlockState below = WorldUtils.getBlockState(world, tPos);
				association = ClientRegistry.FOOTSTEPS.getBlockMap().getBlockSubstrateAcoustics(world, below, tPos,
						Substrate.FENCE);
				if (association != null) {
					pos = tPos;
					in = below;
					DSurround.log().debug("Fence detected");
				}
			}

			if (association == null) {
				association = ClientRegistry.FOOTSTEPS.getBlockMap().getBlockAcoustics(world, in, pos);
			}

			if (association != null && association != AcousticsManager.NOT_EMITTER) {
				// This condition implies that foliage over a NOT_EMITTER block
				// CANNOT PLAY This block most not be executed if the
				// association
				// is a carpet => this block of code is here, not outside this
				// if else group.

				if (above != AIR_STATE) {
					IAcoustic[] foliage = ClientRegistry.FOOTSTEPS.getBlockMap().getBlockSubstrateAcoustics(world, above, pos.up(),
							Substrate.FOLIAGE);
					if (foliage != null && foliage != AcousticsManager.NOT_EMITTER) {
						association = MyUtils.concatenate(association, foliage);
						DSurround.log().debug("Foliage detected");
					}
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
	protected IAcoustic[] resolvePrimitive(@Nonnull final IBlockState state) {

		if (state == AIR_STATE)
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
	protected boolean playSpecialStoppingConditions(@Nonnull final EntityLivingBase ply) {
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
	protected boolean hasSpecialStoppingConditions(@Nonnull final EntityLivingBase ply) {
		return ply.isInWater();
	}

	/**
	 * Find an association for a certain block assuming the player is standing on
	 * it, using a custom strategy which strategies are defined by the solver.
	 */
	@Nonnull
	protected Association findAssociationMessyFoliage(@Nonnull final BlockPos pos) {

		final World world = EnvironState.getWorld();
		final BlockPos up = pos.up();
		final IBlockState above = WorldUtils.getBlockState(world, up);

		if (above == AIR_STATE)
			return null;

		IAcoustic[] association = null;
		boolean found = false;
		// Try to see if the block above is a carpet...
		/*
		 * String association = this.isolator.getBlockMap().getBlockMapSubstrate(
		 * PF172Helper.nameOf(xblock), xmetadata, "carpet");
		 * 
		 * if (association == null || association.equals("NOT_EMITTER")) { // This
		 * condition implies that // if the carpet is NOT_EMITTER, solving will CONTINUE
		 * with the actual // block surface the player is walking on // > NOT_EMITTER
		 * carpets will not cause solving to skip
		 * 
		 * // Not a carpet association =
		 * this.isolator.getBlockMap().getBlockMap(PF172Helper.nameOf(block), metadata);
		 * 
		 * if (association != null && !association.equals("NOT_EMITTER")) { // This
		 * condition implies that // foliage over a NOT_EMITTER block CANNOT PLAY
		 * 
		 * // This block most not be executed if the association is a carpet // => this
		 * block of code is here, not outside this if else group.
		 */

		IAcoustic[] foliage = ClientRegistry.FOOTSTEPS.getBlockMap().getBlockSubstrateAcoustics(world, above, up,
				Substrate.FOLIAGE);
		if (foliage != null && foliage != AcousticsManager.NOT_EMITTER) {
			// we discard the normal block association, and mark the foliage as
			// detected
			// association = association + "," + foliage;
			association = foliage;
			IAcoustic[] isMessy = ClientRegistry.FOOTSTEPS.getBlockMap().getBlockSubstrateAcoustics(world, above, up,
					Substrate.MESSY);

			if (isMessy != null && isMessy == AcousticsManager.MESSY_GROUND)
				found = true;
		}
		/*
		 * } // else { the information is discarded anyways, the method returns null or
		 * no association } } else // Is a carpet return null;
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
	protected Association addSoundOverlay(@Nonnull final EntityLivingBase entity, @Nullable Association assoc) {

		final ArmorClass armor;
		final ArmorClass foot;

		if (EnvironState.isPlayer(entity)) {
			armor = EnvironState.getPlayerArmorClass();
			foot = EnvironState.getPlayerFootArmorClass();
		} else {
			armor = ArmorClass.effectiveArmorClass(entity);
			foot = ArmorClass.footArmorClass(entity);
		}

		final IAcoustic armorAddon = this.isolator.getAcoustics().getAcoustic(armor.getAcoustic());
		IAcoustic footAddon = this.isolator.getAcoustics().getAcoustic(foot.getFootAcoustic());

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