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

package org.orecruncher.dsurround.registry.biome;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FakeBiome implements IBiome {

	private static int biomeIdCounter = -200;

	protected final int biomeId = --biomeIdCounter;
	protected final String name;
	protected final ResourceLocation key;

	protected BiomeInfo biomeData;

	public FakeBiome(@Nonnull final String name) {
		this.name = name;
		this.key = new ResourceLocation(ModInfo.RESOURCE_ID, ("fake_" + name).replace(' ', '_'));
	}

	@Nullable
	public BiomeInfo getBiomeData() {
		return this.biomeData;
	}

	public void setBiomeData(@Nullable BiomeInfo data) {
		this.biomeData = data;
	}

	@Override
	public int getId() {
		return this.biomeId;
	}

	@Override
	public boolean canRain() {
		return getTrueBiome().canRain();
	}

	@Override
	public boolean getEnableSnow() {
		return getTrueBiome().getEnableSnow();
	}

	@Override
	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return getTrueBiome().getFloatTemperature(pos);
	}

	@Override
	public float getTemperature() {
		return getTrueBiome().getTemperature();
	}

	@Override
	public TempCategory getTempCategory() {
		return getTrueBiome().getTempCategory();
	}

	@Override
	public boolean isHighHumidity() {
		return getTrueBiome().isHighHumidity();
	}

	@Override
	public float getRainfall() {
		final BiomeInfo info = getTrueBiome();
		return info == null ? 0F : info.getRainfall();
	}

	@Override
	public Biome getBiome() {
		return null;
	}

	@Override
	public ResourceLocation getKey() {
		return this.key;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Set<Type> getTypes() {
		return ImmutableSet.of();
	}

	@Override
	public boolean isFake() {
		return true;
	}

	private static BiomeInfo getTrueBiome() {
		return EnvironState.getTruePlayerBiome();
	}

}
