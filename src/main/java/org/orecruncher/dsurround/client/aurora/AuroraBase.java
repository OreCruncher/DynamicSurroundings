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
package org.orecruncher.dsurround.client.aurora;

import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.dimension.IDimensionInfo;
import org.orecruncher.dsurround.client.aurora.AuroraFactory.AuroraGeometry;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class AuroraBase implements IAurora {

	protected final Random random;
	protected final AuroraBand band;
	protected final int bandCount;
	protected final float offset;
	protected final AuroraLifeTracker tracker;
	protected final AuroraColor colors;

	protected final EntityPlayer player;
	protected final IDimensionInfo dimInfo;

	public AuroraBase(final long seed) {
		this(seed, false);
	}

	public AuroraBase(final long seed, final boolean flag) {
		this(new XorShiftRandom(seed), flag);
	}

	public AuroraBase(final Random rand, final boolean flag) {
		this.random = rand;
		this.bandCount = Math.min(this.random.nextInt(3) + 1, ModOptions.aurora.maxBands);
		this.offset = this.random.nextInt(20) + 20;
		this.colors = AuroraColor.get(this.random);

		final AuroraGeometry geo = AuroraGeometry.get(this.random);
		this.band = new AuroraBand(this.random, geo, flag, flag);
		this.tracker = new AuroraLifeTracker(AuroraUtils.AURORA_PEAK_AGE, AuroraUtils.AURORA_AGE_RATE);

		this.player = EnvironState.getPlayer();
		this.dimInfo = EnvironState.getDimensionInfo();
	}

	private boolean isAlive() {
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
		return (this.tracker.ageRatio() * this.band.getAlphaLimit()) / 255;
	}

	protected double getTranslationX(final float partialTick) {
		return this.player.posX
				- (this.player.lastTickPosX + (this.player.posX - this.player.lastTickPosX) * partialTick);
	}

	protected double getTranslationZ(final float partialTick) {
		return (this.player.posZ - AuroraUtils.PLAYER_FIXED_Z_OFFSET)
				- (this.player.lastTickPosZ + (this.player.posZ - this.player.lastTickPosZ) * partialTick);
	}

	protected double getTranslationY(final float partialTick) {
		if (this.player.posY > dimInfo.getSeaLevel()) {
			final double limit = (this.dimInfo.getSkyHeight() + this.dimInfo.getCloudHeight()) / 2D;
			final double d1 = limit - this.dimInfo.getSeaLevel();
			final double d2 = player.posY - this.dimInfo.getSeaLevel();
			return AuroraUtils.PLAYER_FIXED_Y_OFFSET * (d1 - d2) / d1;
		}

		return AuroraUtils.PLAYER_FIXED_Y_OFFSET;
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
		builder.append("bands: ").append(this.bandCount);
		builder.append(", off: ").append(this.offset);
		builder.append(", len: ").append(this.band.length);
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
