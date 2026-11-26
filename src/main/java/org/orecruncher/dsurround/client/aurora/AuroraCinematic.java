/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The cinematic aurora: the night's ribbons ({@link CinematicAurora}), as active as the night is at the time
 * ({@link AuroraNight}), drawn by {@link CinematicAuroraRenderer}, fading in and out as the classic one does
 * ({@link AuroraLifeTracker}). If its shaders can't draw, the night's classic aurora is drawn in its place, with the
 * same fade. Ported from the 26.x line.
 */
@SideOnly(Side.CLIENT)
public final class AuroraCinematic implements IAurora {

	/**
	 * Percent of nights that are storms (brightest, fastest, the ribbons drawn in overhead at their height)
	 */
	static final int STORM_CHANCE = 10;

	private final long seed;
	private final CinematicAurora aurora;
	private final AuroraNight night;
	private final CinematicAuroraMotion motion = new CinematicAuroraMotion();
	private final AuroraLifeTracker tracker;
	// Takes over, with the same fade, if the cinematic aurora can't be drawn
	private AuroraClassic fallback;

	public AuroraCinematic(final long seed) {
		this(seed, new AuroraLifeTracker(AuroraUtils.AURORA_PEAK_AGE, AuroraUtils.AURORA_AGE_RATE));
	}

	/**
	 * With another aurora's fade, taking over from it when the style changes
	 */
	AuroraCinematic(final long seed, @Nonnull final AuroraLifeTracker tracker) {
		this.seed = seed;
		this.aurora = CinematicAurora.create(seed);
		this.night = AuroraNight.of(seed, STORM_CHANCE);
		this.tracker = tracker;
	}

	@Override
	public long getSeed() {
		return this.seed;
	}

	@Override
	public boolean isCinematic() {
		return true;
	}

	@Override
	@Nonnull
	public AuroraLifeTracker getTracker() {
		return this.tracker;
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
		return !this.tracker.isAlive();
	}

	@Override
	public void render(final float partialTick) {
		if (!CinematicAuroraRenderer.isAvailable()) {
			if (this.fallback == null)
				this.fallback = new AuroraClassic(this.seed, this.tracker);
			this.fallback.render(partialTick);
			return;
		}
		final Minecraft mc = Minecraft.getMinecraft();
		if (mc.world == null)
			return;
		final float time = AuroraUtils.getTimeSeconds();
		final AuroraNight.Moment moment = this.night.at(AuroraNight.progressOf(mc.world.getWorldTime()));
		this.motion.advance(this.aurora, moment.speed, time);
		CinematicAuroraRenderer.render(this.aurora, moment, this.motion, this.tracker.ageRatio(),
				!"fancy".equalsIgnoreCase(ModOptions.aurora.cinematicQuality), time);
	}

	@Override
	@Nonnull
	public String toString() {
		return String.format("<CINEMATIC> %s, %d ribbons, %s night, strength %.2f%s", this.aurora.layout,
				this.aurora.ribbons.size(), this.night.activity, this.tracker.ageRatio(),
				this.fallback != null ? ", drawn classic" : this.tracker.isFading() ? ", fading" : "");
	}
}
