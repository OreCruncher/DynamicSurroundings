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

package org.blockartistry.DynSurround.client.fx.particle.system;

import org.blockartistry.DynSurround.client.fx.ParticleCollections;
import org.blockartistry.DynSurround.client.fx.WaterSplashJetEffect;
import org.blockartistry.DynSurround.client.fx.particle.mote.IParticleMote;
import org.blockartistry.DynSurround.client.sound.PositionedEmitter;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.client.sound.Sounds;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleWaterSplash extends ParticleJet {

	private static final SoundEffect[] fallSounds = new SoundEffect[11];
	static {
		fallSounds[0] = Sounds.WATERFALL0;
		fallSounds[1] = Sounds.WATERFALL0;
		fallSounds[2] = Sounds.WATERFALL1;
		fallSounds[3] = Sounds.WATERFALL1;
		fallSounds[4] = Sounds.WATERFALL2;
		fallSounds[5] = Sounds.WATERFALL3;
		fallSounds[6] = Sounds.WATERFALL3;
		fallSounds[7] = Sounds.WATERFALL4;
		fallSounds[8] = Sounds.WATERFALL4;
		fallSounds[9] = Sounds.WATERFALL5;
		fallSounds[10] = Sounds.WATERFALL5;
	}

	private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private final BlockPos location;
	private PositionedEmitter emitter;

	public ParticleWaterSplash(final int strength, final World world, final BlockPos loc, final double x, final double y, final double z) {
		super(strength, world, x, y, z);
		this.location = loc.toImmutable();
	}

	@Override
	public boolean shouldDie() {
		return !WaterSplashJetEffect.isValidSpawnBlock(WorldUtils.getDefaultBlockStateProvider(), this.location);
	}

	private boolean setupSound() {
		return this.isAlive() && this.emitter == null && RANDOM.nextInt(4) == 0;
	}

	@Override
	protected void soundUpdate() {
		if (setupSound()) {
			pos.setPos(this.posX, this.posY, this.posZ);
			final int idx = MathStuff.clamp(this.jetStrength, 0, fallSounds.length - 1);
			this.emitter = new PositionedEmitter(fallSounds[idx], pos);
			this.emitter.setPitch(1F + 0.2F * (RANDOM.nextFloat() - RANDOM.nextFloat()));
		}

		if (this.emitter != null)
			this.emitter.update();
	}

	@Override
	protected void cleanUp() {
		if (this.emitter != null)
			this.emitter.stop();
		this.emitter = null;
		super.cleanUp();
	}

	// Entity.resetHeight()
	@Override
	protected void spawnJetParticle() {

		final int splashCount = Math.min(this.getParticleLimit() - getCurrentParticleCount(), 100);

		for (int j = 0; (float) j < splashCount; ++j) {
			final double xOffset = (RANDOM.nextDouble() * 2.0F - 1.0F);
			final double zOffset = (RANDOM.nextDouble() * 2.0F - 1.0F);
			if (WorldUtils.isSolidBlock(this.world, pos.setPos(this.posX + xOffset, this.posY, this.posZ + zOffset)))
				continue;

			final double motionX = xOffset * (this.jetStrength / 25.0D);
			final double motionY = 0.1D + RANDOM.nextDouble() * this.jetStrength / 20.0D;
			final double motionZ = zOffset * (this.jetStrength / 25.D);
			final IParticleMote particle = ParticleCollections.addWaterSpray(this.world, this.posX + xOffset,
					(double) (this.posY), this.posZ + zOffset, motionX, motionY, motionZ);
			if (particle != null)
				addParticle(particle);
		}
	}

}
