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
package org.blockartistry.DynSurround.client.aurora;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.registry.DimensionInfo;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class AuroraBase implements IAurora {

	protected final Random random;
	protected final long seed;
	protected final AuroraBand[] bands;
	protected final AuroraLifeTracker tracker;
	protected final AuroraColor colors;

	public AuroraBase(final long seed) {
		this(seed, false);
	}

	public AuroraBase(final long seed, final boolean flag) {
		this.seed = seed;
		this.tracker = new AuroraLifeTracker(AuroraUtils.AURORA_PEAK_AGE, AuroraUtils.AURORA_AGE_RATE);
		this.random = new XorShiftRandom(seed);
		this.bands = new AuroraBand[this.random.nextInt(3) + 1];
		this.colors = AuroraColor.get(this.random);

		final AuroraGeometry geo = AuroraGeometry.get(this.random);
		this.bands[0] = new AuroraBand(this.random, geo, flag, flag);
		if (this.bands.length > 1) {
			for (int i = 1; i < this.bands.length; i++)
				this.bands[i] = this.bands[0].copy(geo.bandOffset * i);
		}
	}

	@Override
	public boolean isAlive() {
		return this.tracker.isAlive();
	}

	@Override
	public void setFading(final boolean flag) {
		this.tracker.setFading(flag);
	}

	@Override
	public boolean isDying() {
		return this.tracker.isFading();
	}

	@Override
	public void update() {
		this.tracker.update();
	}

	@Override
	public boolean isComplete() {
		return !isAlive();
	}

	protected float getAlpha() {
		return (this.tracker.ageRatio() * this.bands[0].getAlphaLimit()) / 255;
	}

	protected double getTranslationX(final float partialTick) {
		final Minecraft mc = Minecraft.getMinecraft();
		return mc.player.posX - (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTick);
	}

	protected double getTranslationZ(final float partialTick) {
		final Minecraft mc = Minecraft.getMinecraft();
		return (mc.player.posZ - AuroraUtils.PLAYER_FIXED_Z_OFFSET)
				- (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTick);
	}

	protected double getTranslationY(final float partialTick) {
		final DimensionInfo dimInfo = EnvironState.getDimensionInfo();
		final Minecraft mc = Minecraft.getMinecraft();
		double heightScale = 1D;
		if (mc.player.posY > dimInfo.getSeaLevel()) {
			final double limit = (dimInfo.getSkyHeight() + dimInfo.getCloudHeight()) / 2D;
			final double d1 = limit - dimInfo.getSeaLevel();
			final double d2 = mc.player.posY - dimInfo.getSeaLevel();
			heightScale = (d1 - d2) / d1;
		}

		return AuroraUtils.PLAYER_FIXED_Y_OFFSET * heightScale;
	}

	@Nonnull
	protected Color getBaseColor() {
		return this.colors.baseColor;
	}

	@Nonnull
	protected Color getFadeColor() {
		return this.colors.fadeColor;
	}

	@Nonnull
	protected Color getMiddleColor() {
		return this.colors.middleColor;
	}

	@Override
	public abstract void render(final float partialTick);

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("seed: ").append(this.seed);
		builder.append(", bands: ").append(this.bands.length);
		builder.append(", base: ").append(getBaseColor().toString());
		builder.append(", fade: ").append(getFadeColor().toString());
		builder.append(", alpha: ").append((int) (getAlpha() * 255));
		if (!this.tracker.isAlive())
			builder.append(", DEAD");
		else if (this.tracker.isFading())
			builder.append(", FADING");
		return builder.toString();
	}

}
