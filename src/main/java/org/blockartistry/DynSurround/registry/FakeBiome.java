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

package org.blockartistry.DynSurround.registry;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FakeBiome extends Biome {

	private static int biomeIdCounter = -200;

	protected final int biomeId = --biomeIdCounter;

	public FakeBiome(@Nonnull final String name) {
		super(new BiomeProperties(name));

		this.flowers = null;
		this.spawnableCaveCreatureList = null;
		this.spawnableCreatureList = null;
		this.spawnableMonsterList = null;
		this.spawnableWaterCreatureList = null;
		this.decorator = null;

		this.setRegistryName(DSurround.RESOURCE_ID, ("fake_" + name).replace(' ', '_'));
	}

	public int getBiomeId() {
		return this.biomeId;
	}

	private static BiomeInfo getTrueBiome() {
		// Nasty hack to work around dedicated server exception
		if (DSurround.proxy().isRunningAsServer())
			return RegistryManager.get().<BiomeRegistry>get(BiomeRegistry.class).get(Biomes.PLAINS);
		return EnvironState.getTruePlayerBiome();
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
}
