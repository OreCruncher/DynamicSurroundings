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

package org.orecruncher.dsurround.client.fx.particle;

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.particle.ParticleBubble;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBubbleBreath extends ParticleBubble {

	public ParticleBubbleBreath(@Nonnull final Entity entity) {
		this(entity, false);
	}

	public ParticleBubbleBreath(@Nonnull final Entity entity, final boolean isDrowning) {
		super(entity.getEntityWorld(), 0, 0, 0, 0, 0, 0);

		final Random random = XorShiftRandom.current();

		final boolean isChild = entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isChild();

		// Generate breath particle vectoring from entities head in the direction
		// they are looking. Need to offset out of the head block and down a little
		// bit towards the mouth.
		final Vec3d eyePosition = eyePosition(entity).subtract(0D, isChild ? 0.05D : 0.1D, 0D);
		final Vec3d look = entity.getLook(1F); // Don't use the other look vector method!
		final Vec3d origin = eyePosition.add(look.scale(isChild ? 0.25D : 0.5D));

		setPosition(origin.x, origin.y, origin.z);
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		// Next we want to give some randomness to the breath stream so it
		// just doesn't look like a line. This also causes it to drift to the side
		// a little bit giving the impression of a slight breeze.
		final Vec3d trajectory = look.rotateYaw(random.nextFloat() * 2F).rotatePitch(random.nextFloat() * 2F)
				.normalize();
		final double factor = isDrowning ? 0.02D : 0.005D;
		this.motionX = trajectory.x * factor;
		this.motionZ = trajectory.z * factor;
		this.motionY = 0.06D;

		this.particleAlpha = 0.2F;

		this.particleGravity = 0F;
		this.particleScale *= isChild ? 0.125F : 0.25F;

		if (isDrowning)
			this.particleScale *= 2.0F;
	}

	/*
	 * Use some corrective lenses because the MC routine just doesn't lower the
	 * height enough for our rendering purpose.
	 */
	protected Vec3d eyePosition(final Entity e) {
		Vec3d t = e.getPositionEyes(1F);
		if (e.isSneaking())
			t = t.subtract(0D, 0.25D, 0D);
		return t;
	}

	@Override
	public boolean shouldDisableDepth() {
		return true;
	}

}
