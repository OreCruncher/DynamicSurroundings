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
package org.orecruncher.dsurround.client.footsteps.system.accents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.footsteps.implem.AcousticsManager;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;
import org.orecruncher.dsurround.client.footsteps.interfaces.IFootstepAccentProvider;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.registry.PrecipitationType;
import org.orecruncher.dsurround.registry.season.SeasonInfo;
import org.orecruncher.lib.collections.ObjectArray;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RainSplashAccent implements IFootstepAccentProvider {

	protected final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	@Override
	@Nonnull
	public String getName() {
		return "Rain Splash Accent";
	}

	@Override
	@Nonnull
	public ObjectArray<IAcoustic> provide(@Nonnull final EntityLivingBase entity, @Nullable final BlockPos blockPos,
			@Nonnull final ObjectArray<IAcoustic> in) {
		if (ModOptions.sound.enablePuddleSound && Weather.isRaining() && EnvironState.isPlayer(entity)) {
			if (blockPos != null) {
				this.mutable.setPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
			} else {
				this.mutable.setPos(entity);
			}
			final World world = entity.getEntityWorld();
			final SeasonInfo season = ClientRegistry.SEASON.getData(world);
			final int precipHeight = season.getPrecipitationHeight(world, this.mutable).getY();
			if (precipHeight == this.mutable.getY()) {
				final PrecipitationType pt = season.getPrecipitationType(world, this.mutable, null);
				if (pt == PrecipitationType.RAIN)
					in.addAll(AcousticsManager.SPLASH);
			}
		}
		return in;
	}

}
