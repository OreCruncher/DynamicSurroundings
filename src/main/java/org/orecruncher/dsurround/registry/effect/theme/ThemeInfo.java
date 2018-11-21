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
package org.orecruncher.dsurround.registry.effect.theme;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThemeInfo {

	protected final String name;
	protected boolean doBiomeFog = true;
	protected boolean doMorningFog = true;
	protected boolean doWeatherFog = true;
	protected boolean doElevationHaze = true;
	protected boolean doBedrockFog = true;

	protected int maxLightLevel = 15;

	protected float minFogDistance = 0F;
	protected float maxFogDistance = 0F;

	public ThemeInfo() {
		this.name = "DEFAULT";
	}

	@Nonnull
	public String name() {
		return this.name;
	}

	public boolean doBiomeFog() {
		return this.doBiomeFog && ModOptions.fog.enableBiomeFog;
	}

	public boolean doMorningFog() {
		return this.doMorningFog && ModOptions.fog.enableMorningFog;
	}

	public boolean doWeatherFog() {
		return this.doWeatherFog && ModOptions.fog.enableWeatherFog;
	}

	public boolean doElevationHaze() {
		return this.doElevationHaze && ModOptions.fog.enableElevationHaze;
	}

	public boolean doBedrockFog() {
		return this.doBedrockFog && ModOptions.fog.enableBedrockFog;
	}

	public boolean doMaxLightLevel() {
		return this.maxLightLevel >= 0 && this.maxLightLevel < 16;
	}

	public int getMaxLightLevel() {
		return this.maxLightLevel;
	}

	public boolean doFixedFog() {
		return this.minFogDistance >= 0 && this.minFogDistance < this.maxFogDistance;
	}

	public float getMinFogDistance() {
		return this.minFogDistance;
	}

	public float getMaxFogDistance() {
		return this.maxFogDistance;
	}

}
