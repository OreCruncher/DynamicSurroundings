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

package org.blockartistry.mod.DynSurround.client.fx.particle;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundManager;

import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleFireJet extends ParticleJet {

	private static final SoundEffect FIRE = new SoundEffect("minecraft:fire.fire");

	protected final boolean isLava;

	public ParticleFireJet(final int strength, final World world, final double x, final double y, final double z) {
		super(strength, world, x, y, z);
		this.isLava = RANDOM.nextInt(3) == 0;
	}

	@Override
	public void playSound() {
		final int x = MathHelper.floor_double(this.posX);
		final int y = MathHelper.floor_double(this.posY);
		final int z = MathHelper.floor_double(this.posZ);
		SoundManager.playSoundAt(new BlockPos(x, y, z), FIRE, 0, SoundCategory.BLOCKS);
	}

	@Override
	protected void spawnJetParticle() {
		if (this.isLava) {
			ParticleHelper.spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
		} else {
			final ParticleFlame flame = (ParticleFlame) ParticleHelper.spawnParticle(EnumParticleTypes.FLAME, this.posX,
					this.posY, this.posZ, this.jetStrength / 10.0D);
			flame.flameScale *= this.jetStrength;
		}
	}
}
