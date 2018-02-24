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

package org.blockartistry.DynSurround.client.fx.particle;

import java.lang.reflect.Field;
import java.util.Random;

import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBreath extends ParticleCloud {

	protected static final Field sizeField = ReflectionHelper.findField(ParticleCloud.class, "field_70569_a", "oSize");

	public ParticleBreath(final Entity entity) {
		super(entity.getEntityWorld(), 0, 0, 0, 0, 0, 0);

		final Random random = XorShiftRandom.current();

		final boolean isChild = entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isChild();

		// Generate breath particle vectoring from entities head in the direction
		// they are looking. Need to offset out of the head block and down a little
		// bit towards the mouth.
		final Vec3d eyePosition = eyePosition(entity).subtract(0D, isChild ? 0.1D : 0.2D, 0D);
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
		this.motionX = trajectory.x * 0.01D;
		this.motionY = trajectory.y * 0.01D;
		this.motionZ = trajectory.z * 0.01D;

		this.particleAlpha = 0.2F;

		this.particleGravity = 0F;
		this.particleScale *= isChild ? 0.125F : 0.25F;

		try {
			sizeField.set(this, this.particleScale);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
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

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge) {
			setExpired();
		}

		setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
		move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;

		if (this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}
	}

}
