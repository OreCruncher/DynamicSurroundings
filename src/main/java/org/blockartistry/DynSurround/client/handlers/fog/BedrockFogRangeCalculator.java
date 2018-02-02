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
package org.blockartistry.DynSurround.client.handlers.fog;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.WorldUtils;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Implements the void fog (the fog at bedrock) of older versions of Minecraft.
 */
@SideOnly(Side.CLIENT)
public class BedrockFogRangeCalculator extends VanillaFogRangeCalculator {

	protected final FogResult cached = new FogResult();
	protected double skyLight;

	public BedrockFogRangeCalculator() {

	}

	@Override
	@Nonnull
	public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {

		this.cached.set(event);
		if (ModOptions.fog.enableBedrockFog && WorldUtils.hasVoidPartiles(EnvironState.getWorld())) {
			final EntityLivingBase player = EnvironState.getPlayer();
			final double factor = (player.lastTickPosY
					+ (player.posY - player.lastTickPosY) * (double) event.getRenderPartialTicks() + 4.0D) / 32.0D;
			double d0 = (this.skyLight / 16.0D) + factor;

			float end = event.getFarPlaneDistance();
			if (d0 < 1.0D) {
				if (d0 < 0.0D) {
					d0 = 0.0D;
				}

				d0 *= d0;
				float f2 = 100.0F * (float) d0;

				if (f2 < 5.0F) {
					f2 = 5.0F;
				}

				if (end > f2) {
					end = f2;
				}
			}

			this.cached.set(event.getFogMode(), end, FogResult.DEFAULT_PLANE_SCALE);
		}

		return this.cached;
	}

	@Override
	public void tick() {
		this.skyLight = (EnvironState.getPlayer().getBrightnessForRender(1F) & 0xF00000) >> 20;
	}
}
