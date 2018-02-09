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
package org.blockartistry.DynSurround.client.footsteps.system.accents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IFootstepAccentProvider;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.weather.Weather;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.collections.ObjectArray;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RainSplashAccent implements IFootstepAccentProvider {

	@Override
	@Nonnull
	public String getName() {
		return "Rain Splash Accent";
	}

	@Override
	@Nonnull
	public ObjectArray<IAcoustic> provide(@Nonnull final EntityLivingBase entity, @Nullable final BlockPos blockPos,
			@Nonnull final ObjectArray<IAcoustic> in) {
		if (Weather.isRaining() && EnvironState.isPlayer(entity)) {
			final BlockPos.MutableBlockPos pos;
			if (blockPos != null) {
				pos = new BlockPos.MutableBlockPos(blockPos);
				pos.move(EnumFacing.UP);
			} else {
				pos = new BlockPos.MutableBlockPos();
				pos.setPos(entity);
			}
			final int precipHeight = ClientRegistry.SEASON.getPrecipitationHeight(entity.getEntityWorld(), pos).getY();
			if (precipHeight == pos.getY()) {
				final BiomeInfo biome = ClientRegistry.BIOME.get(WorldUtils.getBiome(entity.getEntityWorld(), pos));
				if (biome.hasWeatherEffect() && !biome.getHasDust()) {
					pos.setPos(pos.getX(), precipHeight, pos.getZ());
					final boolean canSnow = ClientRegistry.SEASON.canWaterFreeze(entity.getEntityWorld(), pos);
					if (!canSnow)
						in.addAll(AcousticsManager.SPLASH);
				}
			}
		}
		return in;
	}

}
