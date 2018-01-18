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

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBreath extends ParticleCloud {

	protected static final Field sizeField = ReflectionHelper.findField(ParticleCloud.class, "field_70569_a", "oSize");

	public ParticleBreath(final EntityPlayer player) {
		super(player.world, 0, 0, 0, 0, 0, 0);

		final Random random = XorShiftRandom.current();

		// Generate steam particle vectoring from player's head in the direction
		// they are looking. Need to offset out of the head block and down a little
		// bit towards the mouth.
		final Vec3d eyePosition = EnvironState.getPlayer().getPositionEyes(1.0F);
		final Vec3d vector = EnvironState.getPlayer().getLookVec();
		final Vec3d origin = eyePosition.add(vector.addVector(0D, -0.2D, 0D).scale(0.5D));
		final Vec3d jitter = new Vec3d(random.nextGaussian() / 5F, random.nextGaussian() / 5F,
				random.nextGaussian() / 5F);
		final Vec3d acc = vector.add(jitter).scale(0.01D);

		this.setPosition(origin.xCoord, origin.yCoord, origin.zCoord);
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.motionX = acc.xCoord;
		this.motionY = acc.yCoord;
		this.motionZ = acc.zCoord;

		this.particleAlpha = 0.25F;

		this.particleGravity = 0F;
		this.particleScale *= 0.25F;

		try {
			sizeField.set(this, this.particleScale);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}

		this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;

		if (this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}
	}

}
