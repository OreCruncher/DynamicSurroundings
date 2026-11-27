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

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.fx.ParticleCollections;
import org.orecruncher.dsurround.client.fx.WaterfallColumn;
import org.orecruncher.dsurround.client.fx.WaterSplashJetEffect;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.chunk.ClientChunkCache;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A waterfall's splash where the falling water lands, from the 26.x line's WaterfallEffect (without its mist and
 * foam): splashes thrown up from the impact, more and further for a bigger drop, fewer far from the camera. Its
 * strength is the height of the drop ({@link WaterfallColumn#strength}); it checks every half second that it is still
 * a waterfall and recounts the drop, taking a new strength when it changed. Its sound is {@link WaterfallSounds}'.
 */
@SideOnly(Side.CLIENT)
public class ParticleWaterSplash extends ParticleJet {

	// Splashes live this many times a rain splash's life, as the 26.x line's
	static final int SPLASH_LIFE = 2;

	private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private final BlockPos location;
	private int strength;
	protected int particleLimit;
	private boolean tracked;

	public ParticleWaterSplash(final int strength, final World world, final BlockPos loc, final double x,
			final double y, final double z) {
		super(0, strength, world, x, y, z, 4);
		this.location = loc.toImmutable();
		setStrength(strength);
	}

	private void setStrength(final int strength) {
		this.strength = strength;
		// 5 splashes, and 2.5 more a block of drop: the counts there were when strength counted 2 more than the drop
		this.particleLimit = MathStuff.clamp(5 + (int) (strength * 2.5F), 5, 20);
	}

	public int getStrength() {
		return this.strength;
	}

	public double getPosX() {
		return this.posX;
	}

	public double getPosY() {
		return this.posY;
	}

	public double getPosZ() {
		return this.posZ;
	}

	public double distanceSq(@Nonnull final Vec3d point) {
		return point.squareDistanceTo(this.posX, this.posY, this.posZ);
	}

	@Override
	public boolean shouldDie() {
		// Every half second: is it still a waterfall, and is the drop the same height?
		if ((this.particleAge % 10) != 0)
			return false;
		if (!WaterSplashJetEffect.isValidSpawnBlock(WorldUtils.getDefaultBlockStateProvider(), this.location))
			return true;
		final int now = WaterfallColumn.strength(WorldUtils.getDefaultBlockStateProvider(), this.location);
		if (now != this.strength) {
			setStrength(now);
			WaterfallSounds.restart(this);
		}
		return this.strength <= 0;
	}

	@Override
	protected void soundUpdate() {
		if (!this.tracked) {
			WaterfallSounds.track(this);
			this.tracked = true;
		}
	}

	@Override
	protected void cleanUp() {
		WaterfallSounds.untrack(this);
		this.tracked = false;
		super.cleanUp();
	}

	@Override
	protected void spawnJetParticle() {
		if (!ParticleCollections.canFitWaterSpray())
			return;
		final Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
		final double distanceSq = camera == null ? 0D : camera.getDistanceSq(this.posX, this.posY, this.posZ);
		final int splashCount = WaterfallSplashes.byDistance(
				WaterfallSplashes.count(this.particleLimit, SETTINGS.particleSetting, RANDOM.nextInt(Integer.MAX_VALUE)),
				distanceSq);
		// How far splashes fly: grows with the drop, from 0.2 for the smallest waterfall
		final double motionStr = (this.strength + 3) / 20D;
		for (int j = 0; j < splashCount; ++j) {
			final double xOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
			final double zOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
			if (ClientChunkCache.instance()
					.getBlockState(pos.setPos(this.posX + xOffset, this.posY, this.posZ + zOffset)).getMaterial()
					.isSolid())
				continue;
			final double motionX = xOffset * motionStr;
			final double motionZ = zOffset * motionStr;
			final double motionY = 0.1D + RANDOM.nextFloat() * motionStr;
			// If it could not be added the collection is full: no sense beating a dead horse
			if (ParticleCollections.addWaterSpray(this.world, this.posX + xOffset, this.posY, this.posZ + zOffset,
					motionX, motionY, motionZ, SPLASH_LIFE) == null)
				break;
		}
	}
}
