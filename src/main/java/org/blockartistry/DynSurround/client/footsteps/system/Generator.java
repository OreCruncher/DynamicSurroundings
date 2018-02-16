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
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.DynSurround.client.footsteps.implem.BlockMap;
import org.blockartistry.DynSurround.client.footsteps.implem.ConfigOptions;
import org.blockartistry.DynSurround.client.footsteps.implem.Substrate;
import org.blockartistry.DynSurround.client.footsteps.interfaces.EventType;
import org.blockartistry.DynSurround.client.footsteps.interfaces.FootprintStyle;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.client.footsteps.system.accents.FootstepAccents;
import org.blockartistry.DynSurround.client.fx.ParticleCollections;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.facade.FacadeHelper;
import org.blockartistry.DynSurround.registry.Variator;
import org.blockartistry.lib.MCHelper;
import org.blockartistry.lib.MyUtils;
import org.blockartistry.lib.TimeUtils;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.collections.ObjectArray;
import org.blockartistry.lib.compat.EntityLivingBaseUtil;
import org.blockartistry.lib.compat.EntityUtil;
import org.blockartistry.lib.math.MathStuff;
import org.blockartistry.lib.random.XorShiftRandom;
import org.blockartistry.lib.sound.SoundUtils;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
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

	protected static final Consumer<Footprint> GENERATE_PRINT = print -> {
		final Vec3d loc = print.getStepLocation();
		final World world = print.getEntity().getEntityWorld();
		ParticleCollections.addFootprint(print.getStyle(), world, loc.xCoord, loc.yCoord, loc.zCoord,
				print.getRotation(), print.getScale(), print.isRightFoot());
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

	protected boolean isMessyFoliage;
	protected long brushesTime;

	// We calc our own because of inconsistencies with Minecraft
	protected double distanceWalkedOnStepModified;

	protected int pedometer;

	protected final ObjectArray<Footprint> footprints = new ObjectArray<Footprint>();
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

		// If an entity is a passenger then no footsteps to process
		if (entity.isRiding())
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

	protected void simulateFootsteps(@Nonnull final EntityLivingBase entity) {

		updateWalkedOnStep(entity);

		final float distanceReference = (float) this.distanceWalkedOnStepModified;

		if (this.dmwBase > distanceReference) {
			this.dmwBase = 0;
			this.dwmYChange = 0;
		}

		final double movX = entity.motionX;
		final double movZ = entity.motionZ;

		double scal = movX * this.xMovec + movZ * this.zMovec;
		if (this.scalStat != scal < 0.001f) {
			this.scalStat = !this.scalStat;

			if (this.scalStat && VAR.PLAY_WANDER && !this.hasSpecialStoppingConditions(entity)) {
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
				distance = VAR.STRIDE_LADDER;
			} else if (!entity.isInWater() && MathStuff.abs(this.yPosition - entity.posY) > 0.4d) {
				// This ensures this does not get recorded as landing, but as a
				// step
				if (this.yPosition < entity.posY) { // Going upstairs
					distance = VAR.STRIDE_STAIR;
					event = speedDisambiguator(entity, EventType.UP, EventType.UP_RUN);
				} else if (!entity.isSneaking()) { // Going downstairs
					distance = -1f;
					event = speedDisambiguator(entity, EventType.DOWN, EventType.DOWN_RUN);
				}

				this.dwmYChange = distanceReference;

			} else {
				distance = VAR.STRIDE;
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
		if (!this.playSpecialStoppingConditions(entity)) {
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
		if (this.hasSpecialStoppingConditions(entity))
			return;

		if (this.isFlying && EntityLivingBaseUtil.isJumping(entity)) {
			if (VAR.EVENT_ON_JUMP) {
				// If climbing stairs motion will be negative
				if (entity.motionY > 0) {
					this.didJump = true;
					double speed = entity.motionX * entity.motionX + entity.motionZ * entity.motionZ;

					if (speed < VAR.SPEED_TO_JUMP_AS_MULTIFOOT) {
						// STILL JUMP
						playMultifoot(entity, 0.4d, EventType.JUMP);
					} else {
						// RUNNING JUMP
						playSinglefoot(entity, 0.4d, EventType.JUMP, this.isRightFoot);
					}
				}
			}
		} else if (!this.isFlying) {
			if (this.fallDistance > VAR.LAND_HARD_DISTANCE_MIN) {
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
		return velocity > VAR.SPEED_TO_RUN ? run : walk;
	}

	protected void simulateBrushes(@Nonnull final EntityLivingBase entity) {
		final long current = TimeUtils.currentTimeMillis();
		if (this.brushesTime > current)
			return;

		this.brushesTime = current + 100;

		if (proceedWithStep(entity)) {
			if (entity.motionX == 0d && entity.motionZ == 0d)
				return;

			final int yy = MathStuff.floor(entity.posY - 0.1d - entity.getYOffset() - (entity.onGround ? 0d : 0.25d));
			final Association assos = this.findAssociationMessyFoliage(entity.getEntityWorld(),
					new BlockPos(entity.posX, yy, entity.posZ));
			if (assos != null) {
				if (!this.isMessyFoliage) {
					this.isMessyFoliage = true;
					this.playAssociation(entity, assos, EventType.WALK);
				}
			} else {
				this.isMessyFoliage = false;
			}
		}
	}

	protected boolean proceedWithStep(@Nonnull final EntityLivingBase entity) {
		return !entity.isSneaking();
	}

	protected void playSinglefoot(@Nonnull final EntityLivingBase entity, final double verticalOffsetAsMinus,
			@Nonnull final EventType eventType, final boolean foot) {
		if (proceedWithStep(entity)) {
			final Association assos = this.findAssociationForPlayer(entity, verticalOffsetAsMinus, foot);
			this.playAssociation(entity, assos, eventType);
		}
	}

	protected void playMultifoot(@Nonnull final EntityLivingBase entity, final double verticalOffsetAsMinus,
			final EventType eventType) {

		if (proceedWithStep(entity)) {
			// STILL JUMP
			final Association leftFoot = this.findAssociationForPlayer(entity, verticalOffsetAsMinus, false);
			final Association rightFoot = this.findAssociationForPlayer(entity, verticalOffsetAsMinus, true);
			this.playAssociation(entity, leftFoot, eventType);
			this.playAssociation(entity, rightFoot, eventType);
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
	 * @param world
	 *            The Entity world
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
	protected Vec3d footstepPosition(@Nonnull final World world, @Nonnull final BlockPos pos, final double xx,
			final double zz) {
		final IBlockState state = WorldUtils.getBlockState(world, pos);
		if (hasFootstepImprint(world, state, pos)) {
			final double posY = pos.getY() + state.getBoundingBox(world, pos).maxY;
			return new Vec3d(xx, posY, zz);

		}
		return null;
	}

	protected boolean shouldProducePrint(@Nonnull final Entity entity) {
		return ModOptions.player.enableFootprints && this.VAR.HAS_FOOTPRINT
				&& !entity.isInvisibleToPlayer(EnvironState.getPlayer());
	}

	/**
	 * Find an association for an entities particular foot. This will fetch the
	 * player angle and use it as a basis to find out what block is below their feet
	 * (or which block is likely to be below their feet if the player is walking on
	 * the edge of a block when walking over non-emitting blocks like air or
	 * water).<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block was
	 * found, but has no association in the blockmap.
	 */
	@Nonnull
	protected Association findAssociationForPlayer(@Nonnull final EntityLivingBase entity,
			final double verticalOffsetAsMinus, final boolean isRightFoot) {

		final float rotDegrees = MathStuff.wrapDegrees(entity.rotationYaw);
		final double rot = MathStuff.toRadians(rotDegrees);
		final float feetDistanceToCenter = isRightFoot ? -this.VAR.DISTANCE_TO_CENTER : this.VAR.DISTANCE_TO_CENTER;

		final double xx = entity.posX + MathStuff.cos(rot) * feetDistanceToCenter;
		final double zz = entity.posZ + MathStuff.sin(rot) * feetDistanceToCenter;
		final double minY = entity.getEntityBoundingBox().minY;
		final BlockPos pos = new BlockPos(xx, minY - 0.1D - verticalOffsetAsMinus, zz);

		Association result = findAssociationForLocation(entity, pos);
		if (SoundUtils.canBeHeard(entity, EnvironState.getPlayerPosition()))
			result = addSoundOverlay(entity, result);

		// It is possible that the association has no position, so it
		// needs to be checked.
		if (result != null && result.getPos() != null && shouldProducePrint(entity)) {
			final Vec3d printPos = footstepPosition(entity.getEntityWorld(), result.getPos(), xx, zz);
			if (printPos != null) {
				FootprintStyle style = this.VAR.FOOTPRINT_STYLE;
				if (style == null) {
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
	 * blocks like air or water)<br>
	 * <br>
	 * Returns null if no blocks are valid emitting blocks.<br>
	 * Returns a string that begins with "_NO_ASSOCIATION" if a matching block was
	 * found, but has no association in the blockmap.
	 */
	@Nonnull
	protected Association findAssociationForLocation(@Nonnull final EntityLivingBase entity,
			@Nonnull final BlockPos pos) {

		final World world = entity.getEntityWorld();

		if (entity.isInWater())
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
			double xdang = (entity.posX - pos.getX()) * 2 - 1;
			double zdang = (entity.posZ - pos.getZ()) * 2 - 1;
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
			association = this.blockMap.getBlockSubstrateAcoustics(world, above, tPos, Substrate.CARPET);

		if (association == null || association == AcousticsManager.NOT_EMITTER) {
			// This condition implies that if the carpet is NOT_EMITTER, solving
			// will CONTINUE with the actual block surface the player is walking
			// on NOT_EMITTER carpets will not cause solving to skip

			if (in == AIR_STATE) {
				tPos = pos.down();
				final IBlockState below = WorldUtils.getBlockState(world, tPos);
				association = this.blockMap.getBlockSubstrateAcoustics(world, below, tPos, Substrate.FENCE);
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

				if (above != AIR_STATE) {
					IAcoustic[] foliage = this.blockMap.getBlockSubstrateAcoustics(world, above, pos.up(),
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
				return null; // Player has stepped on a non-emitter block as
								// defined in the blockmap
			} else {
				return new Association(in, pos, association);
			}
		} else {
			IAcoustic[] primitive = resolvePrimitive(in);
			if (primitive != null) {
				if (primitive == AcousticsManager.NOT_EMITTER) {
					return null;
				}
				return new Association(in, pos, primitive);
			} else {
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
		IAcoustic[] primitive = ClientRegistry.FOOTSTEPS.getPrimitiveMap().getPrimitiveMapSubstrate(soundName,
				substrate);
		if (primitive == null) {
			if (flag) {
				primitive = ClientRegistry.FOOTSTEPS.getPrimitiveMap().getPrimitiveMapSubstrate(soundName,
						"break_" + soundName); // Check sound
			}
			if (primitive == null) {
				primitive = ClientRegistry.FOOTSTEPS.getPrimitiveMap().getPrimitiveMap(soundName);
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

	/**
	 * Find an association for a certain block assuming the player is standing on
	 * it, using a custom strategy which strategies are defined by the solver.
	 */
	@Nonnull
	protected Association findAssociationMessyFoliage(@Nonnull World world, @Nonnull final BlockPos pos) {

		final BlockPos up = pos.up();
		final IBlockState above = WorldUtils.getBlockState(world, up);

		if (above == AIR_STATE)
			return null;

		IAcoustic[] association = null;
		boolean found = false;

		IAcoustic[] foliage = this.blockMap.getBlockSubstrateAcoustics(world, above, up, Substrate.FOLIAGE);
		if (foliage != null && foliage != AcousticsManager.NOT_EMITTER) {
			// we discard the normal block association, and mark the foliage as
			// detected
			association = foliage;
			IAcoustic[] isMessy = this.blockMap.getBlockSubstrateAcoustics(world, above, up, Substrate.MESSY);

			if (isMessy != null && isMessy == AcousticsManager.MESSY_GROUND)
				found = true;
		}

		if (found && association != null) {
			return association == AcousticsManager.NOT_EMITTER ? null : new Association(association);
		}
		return null;
	}

	/**
	 * Adds additional sound overlays to the acoustic based on other environment
	 * aspects, such as armor being worn.
	 */
	@Nullable
	protected Association addSoundOverlay(@Nonnull final EntityLivingBase entity, @Nullable final Association assoc) {
		// Don't apply overlays if the entity is not on the ground
		if (entity.onGround) {
			final ObjectArray<IAcoustic> accents = new ObjectArray<>();
			final BlockPos pos = assoc != null ? assoc.getPos() : null;
			FootstepAccents.provide(entity, pos, accents);
			if (accents.size() > 0) {
				final Association a = assoc == null ? new Association() : assoc;
				accents.forEvery(acoustic -> a.add(acoustic));
				return a;
			}
		}

		return assoc;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("didJump: ").append(Boolean.toString(this.didJump)).append(' ');
		builder.append("isOnLadder: ").append(Boolean.toString(this.isOnLadder)).append(' ');
		builder.append("isFlying: ").append(Boolean.toString(this.isFlying)).append(' ');
		builder.append("isImmobile: ").append(Boolean.toString(this.isImmobile)).append(' ');
		builder.append("isMessy: ").append(Boolean.toString(this.isMessyFoliage));
		return builder.toString();
	}

}