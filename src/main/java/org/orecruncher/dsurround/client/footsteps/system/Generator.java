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

import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.footsteps.implem.AcousticsManager;
import org.orecruncher.dsurround.client.footsteps.implem.BlockMap;
import org.orecruncher.dsurround.client.footsteps.implem.ConfigOptions;
import org.orecruncher.dsurround.client.footsteps.implem.Substrate;
import org.orecruncher.dsurround.client.footsteps.interfaces.EventType;
import org.orecruncher.dsurround.client.footsteps.interfaces.FootprintStyle;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;
import org.orecruncher.dsurround.client.footsteps.system.accents.FootstepAccents;
import org.orecruncher.dsurround.client.fx.ParticleCollections;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.facade.FacadeHelper;
import org.orecruncher.dsurround.registry.footstep.Variator;
import org.orecruncher.lib.MyUtils;
import org.orecruncher.lib.TimeUtils;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.compat.EntityLivingBaseUtil;
import org.orecruncher.lib.compat.EntityUtil;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
	protected static final int BRUSH_INTERVAL = 100;

	protected static final Consumer<Footprint> GENERATE_PRINT = print -> {
		final Vec3d loc = print.getStepLocation();
		final World world = print.getEntity().getEntityWorld();
		ParticleCollections.addFootprint(print.getStyle(), world, loc, print.getRotation(), print.getScale(),
				print.isRightFoot());
	};

	protected final Variator VAR;
	protected final BlockMap blockMap;

	protected float dmwBase;
	protected float dwmYChange;
	protected double yPosition;

	protected boolean didJump;
	protected boolean isFlying;
	protected float fallDistance;

	protected float lastReference;
	protected boolean isImmobile;
	protected long timeImmobile;

	protected boolean isRightFoot;
	protected boolean isOnLadder;

	protected double xMovec;
	protected double zMovec;
	protected boolean scalStat;
	protected boolean stepThisFrame;

	protected BlockPos messyPos = BlockPos.ORIGIN;
	protected long brushesTime;

	// We calc our own because of inconsistencies with Minecraft
	protected double distanceWalkedOnStepModified;
	protected int pedometer;

	protected static final ObjectArray<IAcoustic> accents = new ObjectArray<>(4);
	protected final ObjectArray<Footprint> footprints = new ObjectArray<>(4);
	protected final SoundPlayer soundPlayer;

	public Generator(@Nonnull final Variator var) {
		this.VAR = var;
		this.blockMap = ClientRegistry.FOOTSTEPS.getBlockMap();
		this.soundPlayer = new SoundPlayer(this.VAR);
	}

	public int getPedometer() {
		return this.pedometer;
	}

	public void generateFootsteps(@Nonnull final EntityLivingBase entity) {

		// If an entity is a passenger or is sleeping then no footsteps to process
		if (entity.isRiding() || entity.isPlayerSleeping())
			return;

		// Clear starting state
		this.didJump = false;
		this.stepThisFrame = false;

		this.isOnLadder = entity.isOnLadder();

		simulateFootsteps(entity);
		simulateAirborne(entity);
		simulateBrushes(entity);

		// Flush!
		this.soundPlayer.think();

		if (this.footprints.size() > 0) {
			this.footprints.forEach(GENERATE_PRINT);
			this.footprints.clear();
		}

		if (this.stepThisFrame)
			this.pedometer++;

		// Player jump breath
		if (this.didJump && ModOptions.sound.enableJumpSound && this.VAR.PLAY_JUMP && !entity.isSneaking()) {
			this.soundPlayer.playAcoustic(entity, AcousticsManager.JUMP, EventType.JUMP, null);
		}

		if (ModOptions.sound.footstepsSoundFactor > 0) {
			EntityUtil.setNextStepDistance(entity, Integer.MAX_VALUE);
		} else {
			final int dist = EntityUtil.getNextStepDistance(entity);
			if (dist == Integer.MAX_VALUE)
				EntityUtil.setNextStepDistance(entity, 0);
		}
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
			return current - this.timeImmobile > this.VAR.IMMOBILE_DURATION;
		}

		return false;
	}

	protected void updateWalkedOnStep(@Nonnull final EntityLivingBase entity) {
		final double dX = entity.posX - entity.prevPosX;
		final double dY = entity.posY - entity.prevPosY;
		final double dZ = entity.posZ - entity.prevPosZ;
		this.distanceWalkedOnStepModified += Math.sqrt(dX * dX + dY * dY + dZ * dZ) * 0.6F;
	}

	protected void simulateFootsteps(@Nonnull final EntityLivingBase entity) {

		updateWalkedOnStep(entity);

		final float distanceReference = (float) this.distanceWalkedOnStepModified;

		if (this.dmwBase > distanceReference) {
			this.dmwBase = 0;
			this.dwmYChange = 0;
		}

		final double movX = entity.motionX;
		final double movZ = entity.motionZ;

		final double scal = movX * this.xMovec + movZ * this.zMovec;
		if (this.scalStat != scal < 0.001f) {
			this.scalStat = !this.scalStat;

			if (this.scalStat && this.VAR.PLAY_WANDER && !hasSpecialStoppingConditions(entity)) {
				playSinglefoot(entity, 0d, EventType.WANDER, this.isRightFoot);
			}
		}

		this.xMovec = movX;
		this.zMovec = movZ;

		if (entity.onGround || entity.isInWater() || entity.isOnLadder()) {
			EventType event = null;

			float dwm = distanceReference - this.dmwBase;
			final boolean immobile = stoppedImmobile(distanceReference);
			if (immobile && !entity.isOnLadder()) {
				dwm = 0;
				this.dmwBase = distanceReference;
			}

			float distance = 0f;

			if (entity.isOnLadder() && !entity.onGround) {
				distance = this.VAR.STRIDE_LADDER;
			} else if (!entity.isInWater() && MathStuff.abs(this.yPosition - entity.posY) > 0.4d) {
				// This ensures this does not get recorded as landing, but as a
				// step
				if (this.yPosition < entity.posY) { // Going upstairs
					distance = this.VAR.STRIDE_STAIR;
					event = speedDisambiguator(entity, EventType.UP, EventType.UP_RUN);
				} else if (!entity.isSneaking()) { // Going downstairs
					distance = -1f;
					event = speedDisambiguator(entity, EventType.DOWN, EventType.DOWN_RUN);
				}

				this.dwmYChange = distanceReference;

			} else {
				distance = this.VAR.STRIDE;
			}

			if (event == null) {
				event = speedDisambiguator(entity, EventType.WALK, EventType.RUN);
			}

			distance = reevaluateDistance(event, distance);

			if (dwm > distance) {
				produceStep(entity, event, 0F);
				stepped(entity, event);
				this.dmwBase = distanceReference;
			}
		}

		// This fixes an issue where the value is evaluated
		// while the player is between two steps in the air
		// while descending stairs
		if (entity.onGround) {
			this.yPosition = entity.posY;
		}
	}

	protected void stepped(@Nonnull final EntityLivingBase entity, @Nonnull final EventType event) {
	}

	protected float reevaluateDistance(@Nonnull final EventType event, final float distance) {
		return distance;
	}

	protected void produceStep(@Nonnull final EntityLivingBase entity, @Nonnull final EventType event) {
		produceStep(entity, event, 0d);
	}

	protected void produceStep(@Nonnull final EntityLivingBase entity, @Nullable EventType event,
			final double verticalOffsetAsMinus) {
		if (!playSpecialStoppingConditions(entity)) {
			if (event == null)
				event = speedDisambiguator(entity, EventType.WALK, EventType.RUN);
			playSinglefoot(entity, verticalOffsetAsMinus, event, this.isRightFoot);
			this.isRightFoot = !this.isRightFoot;
		}

		this.stepThisFrame = true;
	}

	protected void simulateAirborne(@Nonnull final EntityLivingBase entity) {
		if ((entity.onGround || entity.isOnLadder()) == this.isFlying) {
			this.isFlying = !this.isFlying;
			simulateJumpingLanding(entity);
		}

		if (this.isFlying)
			this.fallDistance = entity.fallDistance;
	}

	protected void simulateJumpingLanding(@Nonnull final EntityLivingBase entity) {
		if (hasSpecialStoppingConditions(entity))
			return;

		if (this.isFlying && EntityLivingBaseUtil.isJumping(entity)) {
			if (this.VAR.EVENT_ON_JUMP) {
				// If climbing stairs motion will be negative
				if (entity.motionY > 0) {
					this.didJump = true;
					final double speed = entity.motionX * entity.motionX + entity.motionZ * entity.motionZ;

					if (speed < this.VAR.SPEED_TO_JUMP_AS_MULTIFOOT) {
						// STILL JUMP
						playMultifoot(entity, 0.4d, EventType.JUMP);
					} else {
						// RUNNING JUMP
						playSinglefoot(entity, 0.4d, EventType.JUMP, this.isRightFoot);
					}
				}
			}
		} else if (!this.isFlying && this.fallDistance > 0) {
			if (this.fallDistance > this.VAR.LAND_HARD_DISTANCE_MIN) {
				playMultifoot(entity, 0d, EventType.LAND);
			} else if (!this.stepThisFrame && !entity.isSneaking()) {
				playSinglefoot(entity, 0d, speedDisambiguator(entity, EventType.CLIMB, EventType.CLIMB_RUN),
						this.isRightFoot);
				this.isRightFoot = !this.isRightFoot;
			}
		}
	}

	protected EventType speedDisambiguator(@Nonnull final EntityLivingBase entity, @Nonnull final EventType walk,
			@Nonnull final EventType run) {
		final double velocity = entity.motionX * entity.motionX + entity.motionZ * entity.motionZ;
		return velocity > this.VAR.SPEED_TO_RUN ? run : walk;
	}

	protected void simulateBrushes(@Nonnull final EntityLivingBase entity) {
		final long current = TimeUtils.currentTimeMillis();
		if (current >= this.brushesTime) {
			this.brushesTime = current + BRUSH_INTERVAL;
			if (proceedWithStep(entity) && (entity.motionX != 0d || entity.motionZ != 0d)) {
				final int yy = MathStuff
						.floor(entity.posY - 0.1d - entity.getYOffset() - (entity.onGround ? 0d : 0.25d));
				final BlockPos pos = new BlockPos(entity.posX, yy, entity.posZ);
				if (!this.messyPos.equals(pos)) {
					this.messyPos = pos;
					final Association assos = findAssociationMessyFoliage(entity.getEntityWorld(), pos);
					if (assos != null)
						playAssociation(entity, assos, EventType.WALK);
				}
			}
		}
	}

	protected boolean proceedWithStep(@Nonnull final EntityLivingBase entity) {
		return !entity.isSneaking();
	}

	protected void playSinglefoot(@Nonnull final EntityLivingBase entity, final double verticalOffsetAsMinus,
			@Nonnull final EventType eventType, final boolean foot) {
		if (proceedWithStep(entity)) {
			final Association assos = findAssociationForPlayer(entity, verticalOffsetAsMinus, foot);
			playAssociation(entity, assos, eventType);
		}
	}

	protected void playMultifoot(@Nonnull final EntityLivingBase entity, final double verticalOffsetAsMinus,
			final EventType eventType) {

		if (proceedWithStep(entity)) {
			// STILL JUMP
			final Association leftFoot = findAssociationForPlayer(entity, verticalOffsetAsMinus, false);
			final Association rightFoot = findAssociationForPlayer(entity, verticalOffsetAsMinus, true);
			playAssociation(entity, leftFoot, eventType);
			playAssociation(entity, rightFoot, eventType);
		}
	}

	/**
	 * Play an association.
	 */
	protected void playAssociation(@Nonnull final EntityLivingBase entity, @Nullable final Association assos,
			@Nonnull final EventType eventType) {
		if (assos != null && !assos.isNotEmitter()) {
			this.soundPlayer.playAcoustic(entity, assos, eventType);
		}
	}

	protected boolean hasFootstepImprint(@Nonnull final World world, @Nullable final IBlockState state,
			@Nonnull final BlockPos pos) {
		if (state != null) {
			final IBlockState footstepState = FacadeHelper.resolveState(state, world, pos, EnumFacing.UP);
			return ClientRegistry.FOOTSTEPS.hasFootprint(footstepState);
		}

		return false;
	}

	/**
	 * Determines the actual footprint location based on the BlockPos provided. The
	 * print is to ride on top of the bounding box. If the block does not have a
	 * print a null is returned.
	 *
	 * @param entity
	 *            The Entity generating the print
	 * @param pos
	 *            The block position where the footprint is to be placed on top
	 * @param xx
	 *            Calculated foot position for X
	 * @param zz
	 *            Calculated foot position for Z
	 * @return Vector containing footprint coordinates or null if no footprint is to
	 *         be generated
	 */
	@Nullable
	protected Vec3d footstepPosition(@Nonnull final EntityLivingBase entity, @Nonnull final BlockPos pos,
			final double xx, final double zz) {
		final World world = entity.getEntityWorld();
		final IBlockState state = WorldUtils.getBlockState(world, pos);
		if (hasFootstepImprint(world, state, pos)) {
			final double entityY = entity.getEntityBoundingBox().minY;
			final double blockY = pos.getY() + state.getBoundingBox(world, pos).maxY;
			return new Vec3d(xx, Math.max(entityY, blockY), zz);

		}
		return null;
	}

	protected boolean shouldProducePrint(@Nonnull final EntityLivingBase entity) {
		return ModOptions.player.enableFootprints && this.VAR.HAS_FOOTPRINT
				&& (entity.onGround || !(EntityLivingBaseUtil.isJumping(entity) || entity.isAirBorne))
				&& !entity.isInvisibleToPlayer(EnvironState.getPlayer());
	}

	/**
	 * Find an association for an entities particular foot. This will fetch the
	 * player angle and use it as a basis to find out what block is below their feet
	 * (or which block is likely to be below their feet if the player is walking on
	 * the edge of a block when walking over non-emitting blocks like air or water).
	 */
	@Nullable
	protected Association findAssociationForPlayer(@Nonnull final EntityLivingBase entity,
			final double verticalOffsetAsMinus, final boolean isRightFoot) {

		final float rotDegrees = MathStuff.wrapDegrees(entity.rotationYaw);
		final double rot = MathStuff.toRadians(rotDegrees);
		final float feetDistanceToCenter = isRightFoot ? -this.VAR.DISTANCE_TO_CENTER : this.VAR.DISTANCE_TO_CENTER;

		final double xx = entity.posX + MathStuff.cos(rot) * feetDistanceToCenter;
		final double zz = entity.posZ + MathStuff.sin(rot) * feetDistanceToCenter;
		final double minY = entity.getEntityBoundingBox().minY;
		final BlockPos pos = new BlockPos(xx, minY - 0.1D - verticalOffsetAsMinus, zz);

		final Association result = addSoundOverlay(entity, findAssociationForLocation(entity, pos));

		// It is possible that the association has no position, so it
		// needs to be checked.
		if (result != null && result.getPos() != null && shouldProducePrint(entity)) {
			final Vec3d printPos = footstepPosition(entity, result.getPos(), xx, zz);
			if (printPos != null) {
				FootprintStyle style = this.VAR.FOOTPRINT_STYLE;
				if (entity instanceof EntityPlayer) {
					style = FootprintStyle.getStyle(ModOptions.player.footprintStyle);
				}
				final Footprint print = Footprint.produce(style, entity, printPos, rotDegrees, this.VAR.FOOTPRINT_SCALE,
						isRightFoot);
				this.footprints.add(print);
			}
		}
		return result;
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
	protected Association findAssociationForLocation(@Nonnull final EntityLivingBase entity,
			@Nonnull final BlockPos pos) {

		final World world = entity.getEntityWorld();

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
			final double xdang = (entity.posX - pos.getX()) * 2 - 1;
			final double zdang = (entity.posZ - pos.getZ()) * 2 - 1;
			// -1 0 1
			// ------- -1
			// | o |
			// | + | 0 --> x
			// | |
			// ------- 1
			// |
			// V z

			// If the player is at the edge of that
			if (Math.max(MathStuff.abs(xdang), MathStuff.abs(zdang)) > this.VAR.DISTANCE_TO_CENTER) {
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
					worked = findAssociationForBlock(world, xdang > 0 ? pos.east() : pos.west());
				} else {
					worked = findAssociationForBlock(world, zdang > 0 ? pos.south() : pos.north());
				}

				// If that didn't work, then maybe the footstep hit in the
				// direction of walking. Try with the other closest block
				if (worked == null) {
					// Take the maximum direction and try with
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
	 * actually not emitting, such as lilypads on water.
	 *
	 * Returns null if the block is not a valid emitting block (this causes the
	 * engine to continue looking for valid blocks). This also happens if the carpet
	 * is non-emitting.
	 *
	 * Returns a string that begins with "_NO_ASSOCIATION" if the block is valid,
	 * but has no association in the blockmap. If the carpet was selected, this
	 * solves to the carpet.
	 */
	@Nullable
	protected Association findAssociationForBlock(@Nonnull final World world, @Nonnull BlockPos pos) {
		final IBlockState airState = Blocks.AIR.getDefaultState();
		IBlockState in = WorldUtils.getBlockState(world, pos);
		BlockPos tPos = pos.up();
		final IBlockState above = WorldUtils.getBlockState(world, tPos);

		IAcoustic[] association = null;

		if (above != airState)
			association = this.blockMap.getBlockAcoustics(world, above, tPos, Substrate.CARPET);

		if (association == null || association == AcousticsManager.NOT_EMITTER) {
			// This condition implies that if the carpet is NOT_EMITTER, solving
			// will CONTINUE with the actual block surface the player is walking
			// on NOT_EMITTER carpets will not cause solving to skip

			if (in == airState) {
				tPos = pos.down();
				final IBlockState below = WorldUtils.getBlockState(world, tPos);
				association = this.blockMap.getBlockAcoustics(world, below, tPos, Substrate.FENCE);
				if (association != null) {
					pos = tPos;
					in = below;
				}
			}

			if (association == null) {
				association = this.blockMap.getBlockAcoustics(world, in, pos);
			}

			if (association != null && association != AcousticsManager.NOT_EMITTER) {
				// This condition implies that foliage over a NOT_EMITTER block
				// CANNOT PLAY This block most not be executed if the association
				// is a carpet => this block of code is here, not outside this
				// if else group.

				if (above != airState) {
					final IAcoustic[] foliage = this.blockMap.getBlockAcoustics(world, above, pos.up(),
							Substrate.FOLIAGE);
					if (foliage != null && foliage != AcousticsManager.NOT_EMITTER) {
						association = MyUtils.concatenate(association, foliage);
					}
				}
			}
		} else {
			pos = tPos;
			in = above;
		}

		if (association != null) {
			if (association == AcousticsManager.NOT_EMITTER) {
				// Player has stepped on a non-emitter block as defined in the blockmap
				return null;
			} else {
				// Let's play the fancy acoustics we have defined for the block
				return new Association(in, pos, association);
			}
		} else {
			// No acoustics. Calling logic will default to playing the normal block
			// step sound if available.
			return new Association(in, pos);
		}
	}

	/**
	 * Play special sounds that must stop the usual footstep figuring things out
	 * process.
	 */
	protected boolean playSpecialStoppingConditions(@Nonnull final EntityLivingBase entity) {
		if (entity.isInWater()) {
			if (proceedWithStep(entity)) {
				final float volume = (float) MathStuff.sqrt(entity.motionX * entity.motionX
						+ entity.motionY * entity.motionY + entity.motionZ * entity.motionZ) * 1.25F;
				final ConfigOptions options = new ConfigOptions();
				options.setGlidingVolume(volume > 1 ? 1 : volume);
				// material water, see EntityLivingBase line 286
				this.soundPlayer.playAcoustic(entity, AcousticsManager.SWIM,
						entity.isInsideOfMaterial(Material.WATER) ? EventType.SWIM : EventType.WALK, options);
			}
			return true;
		}

		return false;
	}

	/**
	 * Tells if footsteps can be played.
	 */
	protected boolean hasSpecialStoppingConditions(@Nonnull final EntityLivingBase entity) {
		return entity.isInWater();
	}

	@Nullable
	protected Association findAssociationMessyFoliage(@Nonnull final World world, @Nonnull final BlockPos pos) {
		Association result = null;
		final BlockPos up = pos.up();
		final IBlockState above = WorldUtils.getBlockState(world, up);

		if (above != Blocks.AIR.getDefaultState()) {
			IAcoustic[] acoustics = this.blockMap.getBlockAcoustics(world, above, up, Substrate.MESSY);
			if (acoustics == AcousticsManager.MESSY_GROUND) {
				acoustics = this.blockMap.getBlockAcoustics(world, above, up, Substrate.FOLIAGE);
				if (acoustics != null && acoustics != AcousticsManager.NOT_EMITTER)
					result = new Association(acoustics);

			}
		}
		return result;
	}

	/**
	 * Adds additional sound overlays to the acoustic based on other environment
	 * aspects, such as armor being worn.
	 */
	@Nullable
	protected Association addSoundOverlay(@Nonnull final EntityLivingBase entity, @Nullable Association assoc) {
		// Don't apply overlays if the entity is not on the ground
		if (entity.onGround) {
			accents.clear();
			final BlockPos pos = assoc != null ? assoc.getPos() : null;
			FootstepAccents.provide(entity, pos, accents);
			if (accents.size() > 0) {
				if (assoc == null)
					assoc = new Association();
				assoc.add(accents);
			}
		}

		return assoc;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("didJump: ").append(Boolean.toString(this.didJump)).append(' ');
		builder.append("onLadder: ").append(Boolean.toString(this.isOnLadder)).append(' ');
		builder.append("flying: ").append(Boolean.toString(this.isFlying)).append(' ');
		builder.append("immobile: ").append(Boolean.toString(this.isImmobile)).append(' ');
		builder.append("steps: ").append(this.pedometer);
		return builder.toString();
	}

}