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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.footsteps.implem.NormalVariator;
import org.blockartistry.DynSurround.client.footsteps.interfaces.EventType;
import org.blockartistry.lib.TimeUtils;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Generator {

	private static final NormalVariator VAR = new NormalVariator();

	// Construct
	final protected Isolator mod;

	// FootstepsRegistry
	protected float dmwBase;
	protected float dwmYChange;
	protected double yPosition;

	// Airborne
	protected boolean isFlying;
	protected float fallDistance;

	protected float lastReference;
	protected boolean isImmobile;
	protected long timeImmobile;

	protected boolean isRightFoot;

	protected double xMovec;
	protected double zMovec;
	protected boolean scalStat;
	private boolean stepThisFrame;

	private boolean isMessyFoliage;
	private long brushesTime;

	public Generator(@Nonnull final Isolator isolator) {
		this.mod = isolator;
	}

	public void generateFootsteps(@Nonnull final EntityPlayer ply) {
		simulateFootsteps(ply);
		simulateAirborne(ply);
		simulateBrushes(ply);
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

	protected void simulateFootsteps(@Nonnull final EntityPlayer ply) {
		final float distanceReference = ply.distanceWalkedOnStepModified;

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

			if (this.scalStat && VAR.PLAY_WANDER && !this.mod.getSolver().hasSpecialStoppingConditions(ply)) {
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
				distance = VAR.DISTANCE_LADDER;
			} else if (!ply.isInWater() && MathStuff.abs(this.yPosition - ply.posY) > 0.4d) {
				// This ensures this does not get recorded as landing, but as a
				// step
				if (this.yPosition < ply.posY) { // Going upstairs
					distance = VAR.DISTANCE_STAIR;
					event = speedDisambiguator(ply, EventType.UP, EventType.UP_RUN);
				} else if (!ply.isSneaking()) { // Going downstairs
					distance = -1f;
					event = speedDisambiguator(ply, EventType.DOWN, EventType.DOWN_RUN);
				}

				this.dwmYChange = distanceReference;

			} else {
				distance = VAR.DISTANCE_HUMAN;
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

	protected void stepped(@Nonnull final EntityPlayer ply, @Nonnull final EventType event) {
	}

	protected float reevaluateDistance(@Nonnull final EventType event, final float distance) {
		return distance;
	}

	protected void produceStep(@Nonnull final EntityPlayer ply, @Nonnull final EventType event) {
		produceStep(ply, event, 0d);
	}

	protected void produceStep(@Nonnull final EntityPlayer ply, @Nullable EventType event,
			final double verticalOffsetAsMinus) {
		if (!this.mod.getSolver().playSpecialStoppingConditions(ply)) {
			if (event == null)
				event = speedDisambiguator(ply, EventType.WALK, EventType.RUN);
			playSinglefoot(ply, verticalOffsetAsMinus, event, this.isRightFoot);
			this.isRightFoot = !this.isRightFoot;
		}

		this.stepThisFrame = true;
	}

	protected void simulateAirborne(@Nonnull final EntityPlayer ply) {
		if ((ply.onGround || ply.isOnLadder()) == this.isFlying) {
			this.isFlying = !this.isFlying;
			simulateJumpingLanding(ply);
		}

		if (this.isFlying)
			this.fallDistance = ply.fallDistance;
	}

	protected void simulateJumpingLanding(@Nonnull final EntityPlayer ply) {
		if (this.mod.getSolver().hasSpecialStoppingConditions(ply))
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
				playSinglefoot(ply, 0d, speedDisambiguator(ply, EventType.CLIMB, EventType.CLIMB_RUN), this.isRightFoot);
				this.isRightFoot = !this.isRightFoot;
			}

		}
	}

	protected EventType speedDisambiguator(@Nonnull final EntityPlayer ply, @Nonnull final EventType walk,
			@Nonnull final EventType run) {
		final double velocity = ply.motionX * ply.motionX + ply.motionZ * ply.motionZ;
		return velocity > VAR.SPEED_TO_RUN ? run : walk;
	}

	private void simulateBrushes(@Nonnull final EntityPlayer ply) {
		final long current = TimeUtils.currentTimeMillis();
		if (this.brushesTime > current)
			return;

		this.brushesTime = current + 100;

		if ((ply.motionX == 0d && ply.motionZ == 0d) || ply.isSneaking())
			return;

		final int yy = MathStuff.floor(ply.posY - 0.1d - ply.getYOffset() - (ply.onGround ? 0d : 0.25d));
		final Association assos = this.mod.getSolver().findAssociationMessyFoliage(new BlockPos(ply.posX, yy, ply.posZ));
		if (assos != null) {
			if (!this.isMessyFoliage) {
				this.isMessyFoliage = true;
				this.mod.getSolver().playAssociation(ply, assos, EventType.WALK);
			}
		} else {
			this.isMessyFoliage = false;
		}
	}

	protected void playSinglefoot(@Nonnull final EntityPlayer ply, final double verticalOffsetAsMinus,
			@Nonnull final EventType eventType, final boolean foot) {
		final Association assos = this.mod.getSolver().findAssociationForPlayer(ply, verticalOffsetAsMinus, foot);
		this.mod.getSolver().playAssociation(ply, assos, eventType);
	}

	protected void playMultifoot(@Nonnull final EntityPlayer ply, final double verticalOffsetAsMinus,
			final EventType eventType) {
		// STILL JUMP
		final Solver s = this.mod.getSolver();
		final Association leftFoot = s.findAssociationForPlayer(ply, verticalOffsetAsMinus, false);
		final Association rightFoot = s.findAssociationForPlayer(ply, verticalOffsetAsMinus, true);
		s.playAssociation(ply, leftFoot, eventType);
		s.playAssociation(ply, rightFoot, eventType);
	}

	protected float scalex(final float number, final float min, final float max) {
		return MathStuff.clamp((number - min) / (max - min), 0.0F, 1.0F);
	}
}