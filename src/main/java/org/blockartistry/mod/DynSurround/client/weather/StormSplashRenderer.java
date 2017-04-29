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

package org.blockartistry.mod.DynSurround.client.weather;

import java.util.Random;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.fx.ParticleCollections;
import org.blockartistry.mod.DynSurround.client.fx.particle.ParticleHelper;
import org.blockartistry.mod.DynSurround.client.sound.SoundEngine;
import org.blockartistry.mod.DynSurround.registry.BiomeInfo;
import org.blockartistry.mod.DynSurround.registry.BiomeRegistry;
import org.blockartistry.mod.DynSurround.registry.DimensionRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.registry.SeasonRegistry;
import org.blockartistry.mod.DynSurround.util.WorldUtils;
import org.blockartistry.mod.DynSurround.util.random.XorShiftRandom;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class StormSplashRenderer {

	protected static final int PARTICLE_SOUND_CHANCE = 3;
	private static final TIntObjectHashMap<StormSplashRenderer> splashRenderers = new TIntObjectHashMap<StormSplashRenderer>();
	private static final StormSplashRenderer DEFAULT = new StormSplashRenderer();

	static {
		splashRenderers.put(0, DEFAULT);
		splashRenderers.put(-1, new NetherSplashRenderer());
		splashRenderers.put(1, new NullSplashRenderer());
	}

	public static void renderStormSplashes(final int dimensionId, final EntityRenderer renderer) {
		StormSplashRenderer splash = splashRenderers.get(dimensionId);
		if (splash == null)
			splash = DEFAULT;
		splash.addRainParticles(renderer);
	}

	protected final Random RANDOM = new XorShiftRandom();
	protected final NoiseGeneratorSimplex GENERATOR = new NoiseGeneratorSimplex(RANDOM);
	protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private final BiomeRegistry biomes = RegistryManager.get(RegistryType.BIOME);
	private final DimensionRegistry dimensions = RegistryManager.get(RegistryType.DIMENSION);
	private final SeasonRegistry season = RegistryManager.get(RegistryType.SEASON);
	
	protected StormSplashRenderer() {

	}

	protected float calculateRainSoundVolume(final World world) {
		final float currentVolume = WeatherProperties.getCurrentVolume();
		final float bounds = currentVolume * 0.25F;
		final float adjust = MathHelper.clamp(
				(float) (this.GENERATOR.getValue((world.getWorldTime() % 24000L) / 100, 1) / 5.0F), -bounds, bounds);
		return MathHelper.clamp(currentVolume + adjust, 0, 1F);
	}
	
	protected void spawnBlockParticle(final IBlockState state, final boolean dust, final World world, final double x,
			final double y, final double z) {
		final Block block = state.getBlock();
		EnumParticleTypes particleType = null;

		if (dust || block == Blocks.SOUL_SAND) {
			particleType = null;
		} else if ((block == Blocks.NETHERRACK  || block == Blocks.MAGMA) && RANDOM.nextInt(20) == 0) {
			particleType = EnumParticleTypes.LAVA;
		} else if (state.getMaterial() == Material.LAVA) {
			particleType = EnumParticleTypes.SMOKE_NORMAL;
		} else if (WorldUtils.isFullWaterBlock(state)) {
			ParticleCollections.addWaterRipple(world, x, y, z);
		} else if (state.getMaterial() != Material.AIR) {
			ParticleCollections.addRainSplash(world, x, y, z);
		}

		if (particleType != null)
			ParticleHelper.spawnParticle(particleType, x, y, z);
	}

	protected SoundEvent getBlockSoundFX(final Block block, final boolean hasDust, final World world) {
		if (hasDust)
			return WeatherProperties.getIntensity().getDustSound();
		if (block == Blocks.NETHERRACK)
			return SoundEvents.BLOCK_LAVA_POP;
		return WeatherProperties.getIntensity().getStormSound();
	}

	protected BlockPos getPrecipitationHeight(final World world, final int range, final BlockPos pos) {
		return this.season.getPrecipitationHeight(world, pos);
	}

	protected boolean biomeHasDust(final Biome biome) {
		return ModOptions.allowDesertFog && !WeatherProperties.doVanilla() && this.biomes.get(biome).getHasDust();
	}

	protected void playSplashSound(final EntityRenderer renderer, final World world, final Entity player, double x,
			double y, double z) {

		this.pos.setPos(x, y - 1, z);
		final boolean hasDust = biomeHasDust(world.getBiome(this.pos));
		final Block block = WorldUtils.getBlockState(world, this.pos).getBlock();
		final SoundEvent sound = getBlockSoundFX(block, hasDust, world);
		if (sound != null) {
			final float volume = calculateRainSoundVolume(world);
			float pitch = 1.0F;
			final int playerY = MathHelper.floor(player.posY);
			this.pos.setPos(player.posX, 0, player.posZ);
			if (y > player.posY + 1.0D && getPrecipitationHeight(world, 0, this.pos).getY() > playerY)
				pitch = 0.5F;
			pitch -= (this.RANDOM.nextFloat() - this.RANDOM.nextFloat()) * 0.1F;
			this.pos.setPos(x, y, z);
			SoundEngine.instance().playSound(this.pos, sound, SoundCategory.WEATHER, volume, pitch);
		}
	}

	public void addRainParticles(final EntityRenderer theThis) {
		if (theThis.mc.gameSettings.particleSetting == 2)
			return;

		final World world = theThis.mc.world;
		if (!this.dimensions.hasWeather(world))
			return;

		float rainStrengthFactor = WeatherProperties.getIntensityLevel();
		if (!theThis.mc.gameSettings.fancyGraphics)
			rainStrengthFactor /= 2.0F;

		if (rainStrengthFactor <= 0.0F)
			return;

		RANDOM.setSeed((long) theThis.rendererUpdateCount * 312987231L);
		final Entity entity = theThis.mc.getRenderViewEntity();
		final int playerX = MathHelper.floor(entity.posX);
		final int playerY = MathHelper.floor(entity.posY);
		final int playerZ = MathHelper.floor(entity.posZ);
		double spawnX = 0.0D;
		double spawnY = 0.0D;
		double spawnZ = 0.0D;
		int particlesSpawned = 0;

		final int RANGE = Math.max((ModOptions.specialEffectRange + 1) / 2, 10);
		final float rangeFactor = RANGE / 10.0F;
		int particleCount = (int) (ModOptions.particleCountBase * rainStrengthFactor * rainStrengthFactor
				* rangeFactor);

		if (theThis.mc.gameSettings.particleSetting == 1)
			particleCount >>= 1;

		for (int j1 = 0; j1 < particleCount; ++j1) {
			final int locX = playerX + RANDOM.nextInt(RANGE) - RANDOM.nextInt(RANGE);
			final int locZ = playerZ + RANDOM.nextInt(RANGE) - RANDOM.nextInt(RANGE);
			this.pos.setPos(locX, 0, locZ);
			final BlockPos precipHeight = getPrecipitationHeight(world, RANGE / 2, this.pos);
			final BiomeInfo biome = this.biomes.get(world.getBiome(this.pos));
			final boolean hasDust = biome.getHasDust();
			final boolean canSnow = this.season.canWaterFreeze(world, precipHeight);

			if (precipHeight.getY() <= playerY + RANGE && precipHeight.getY() >= playerY - RANGE
					&& (hasDust || (biome.getHasPrecipitation() && !canSnow))) {

				final BlockPos blockPos = precipHeight.down();
				final IBlockState state = WorldUtils.getBlockState(world, blockPos);
				final double posX = locX + RANDOM.nextFloat();
				final double posY = precipHeight.getY() + 0.1F - state.getBoundingBox(world, blockPos).minY;
				final double posZ = locZ + RANDOM.nextFloat();

				spawnBlockParticle(state, hasDust, world, posX, posY, posZ);

				if (RANDOM.nextInt(++particlesSpawned) == 0) {
					spawnX = posX;
					spawnY = posY;
					spawnZ = posZ;
				}
			}
		}

		if (particlesSpawned > 0 && RANDOM.nextInt(PARTICLE_SOUND_CHANCE) < theThis.rainSoundCounter++) {
			theThis.rainSoundCounter = 0;
			playSplashSound(theThis, world, entity, spawnX, spawnY, spawnZ);
		}
	}
}
