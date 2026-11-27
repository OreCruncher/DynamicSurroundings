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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.registry.biome.traits.BiomeEvidence;
import org.orecruncher.dsurround.registry.biome.traits.BiomeTraits;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BiomeHandler implements IBiome {

	protected final Biome biome;
	protected final int id;
	protected String name;
	protected final BiomeTraits traits;
	protected final Set<Type> types;

	public BiomeHandler(@Nonnull final Biome biome) {
		this.biome = biome;
		this.id = Biome.getIdForBiome(this.biome);
		this.name = BiomeUtil.getBiomeName(this.biome);
		// The biome dictionary's types, and those worked out where its mod gave too few (BiomeTraits)
		this.traits = BiomeTraits.from(evidence(this.biome, this.name));
		this.types = this.traits.names().stream().map(BiomeDictionary.Type::getType).collect(Collectors.toSet());
	}

	@Override
	public Biome getBiome() {
		return this.biome;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public ResourceLocation getKey() {
		return this.biome.getRegistryName();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Set<Type> getTypes() {
		return this.types;
	}

	@Override
	public BiomeTraits getTraits() {
		return this.traits;
	}

	@Nonnull
	private static BiomeEvidence evidence(@Nonnull final Biome biome, @Nonnull final String name) {
		final ResourceLocation key = biome.getRegistryName();
		final Set<String> types = BiomeUtil.getBiomeTypes(biome).stream().map(Type::getName).collect(Collectors.toSet());
		return new BiomeEvidence(key == null ? "" : key.getResourcePath(), name, types, biome.getDefaultTemperature(),
				biome.getRainfall(), biome.canRain(), biome.getEnableSnow(), biome.isHighHumidity(), blockId(biome.topBlock));
	}

	@Nonnull
	private static String blockId(final IBlockState state) {
		final ResourceLocation key = state == null ? null : state.getBlock().getRegistryName();
		return key == null ? "" : key.toString();
	}

	@Override
	public boolean canRain() {
		return this.biome.canRain();
	}

	@Override
	public boolean getEnableSnow() {
		return this.biome.getEnableSnow();
	}

	@Override
	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return this.biome.getTemperature(pos);
	}

	@Override
	public float getTemperature() {
		return this.biome.getDefaultTemperature();
	}

	@Override
	public TempCategory getTempCategory() {
		return this.biome.getTempCategory();
	}

	@Override
	public boolean isHighHumidity() {
		return this.biome.isHighHumidity();
	}

	@Override
	public float getRainfall() {
		return this.biome.getRainfall();
	}

	@Nonnull
	public static ResourceLocation getKey(@Nonnull final Biome biome) {
		ResourceLocation res = biome.getRegistryName();
		if (res == null) {
			final String name = biome.getClass().getName() + "_" + biome.getBiomeName().replace(' ', '_').toLowerCase();
			res = new ResourceLocation(ModInfo.RESOURCE_ID, name);
		}
		return res;
	}

}
