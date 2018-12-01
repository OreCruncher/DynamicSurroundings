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

package org.orecruncher.dsurround.client.fx.particle.system;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.fx.ParticleCollections;
import org.orecruncher.dsurround.client.fx.WaterSplashJetEffect;
import org.orecruncher.dsurround.client.fx.particle.mote.IParticleMote;
import org.orecruncher.dsurround.client.sound.PositionedEmitter;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.client.sound.Sounds;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.math.MathStuff;

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
	protected int particleLimit;

	public ParticleWaterSplash(final int strength, final World world, final BlockPos loc, final double x,
			final double y, final double z) {
		super(0, strength, world, x, y, z, 2);
		this.location = loc.toImmutable();
		setSpawnCount((int) (strength * 2.5F));
	}

	public void setSpawnCount(final int limit) {
		this.particleLimit = MathStuff.clamp(limit, 5, 15);
	}

	public int getSpawnCount() {
		switch (SETTINGS.particleSetting) {
		case 2:
			return 0;
		case 0:
			return this.particleLimit;
		default:
			return this.particleLimit / 2;
		}
	}

	@Override
	public boolean shouldDie() {
		return !WaterSplashJetEffect.isValidSpawnBlock(WorldUtils.getDefaultBlockStateProvider(), this.location);
	}

	private boolean setupSound() {
		return isAlive() && this.jetStrength >= ModOptions.effects.waterfallCutoff && this.emitter == null
				&& RANDOM.nextInt(6) == 0;
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
		if (ParticleCollections.canFitWaterSpray()) {
			final int splashCount = getSpawnCount();

			for (int j = 0; (float) j < splashCount; ++j) {
				final double xOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
				final double zOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
				if (WorldUtils.isSolidBlock(this.world,
						pos.setPos(this.posX + xOffset, this.posY, this.posZ + zOffset)))
					continue;

				final double motionX = xOffset * (this.jetStrength / 25.0D);
				final double motionZ = zOffset * (this.jetStrength / 25.0D);
				final double motionY = 0.1D + RANDOM.nextFloat() * this.jetStrength / 20.0D;
				final IParticleMote particle = ParticleCollections.addWaterSpray(this.world, this.posX + xOffset,
						(this.posY), this.posZ + zOffset, motionX, motionY, motionZ);
				// If we could not add the collection is full. No sense beating a dead horse.
				if (particle == null)
					break;
			}
		}
	}

}
