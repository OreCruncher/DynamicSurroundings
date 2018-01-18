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

package org.blockartistry.DynSurround.client.handlers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.aurora.AuroraEngineClassic;
import org.blockartistry.DynSurround.client.aurora.AuroraEngineShader;
import org.blockartistry.DynSurround.client.aurora.AuroraUtils;
import org.blockartistry.DynSurround.client.aurora.IAurora;
import org.blockartistry.DynSurround.client.aurora.IAuroraEngine;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.shader.Shaders;
import org.blockartistry.lib.DiurnalUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public final class AuroraEffectHandler extends EffectHandlerBase {

	private final IAuroraEngine auroraEngine;

	private static IAurora current;
	private static int dimensionId;

	public AuroraEffectHandler() {
		super("AuroraEffectHandler");

		if (ModOptions.auroraUseShader && Shaders.areShadersSupported())
			this.auroraEngine = new AuroraEngineShader();
		else
			this.auroraEngine = new AuroraEngineClassic();
	}

	@Nullable
	public static IAurora getCurrentAurora() {
		return current;
	}

	@Override
	public void onConnect() {
		current = null;
	}

	@Override
	public void onDisconnect() {
		current = null;
	}

	private boolean spawnAurora(@Nonnull final World world) {
		if (!ModOptions.auroraEnable)
			return false;

		if (current != null || Minecraft.getMinecraft().gameSettings.renderDistanceChunks < 6
				|| DiurnalUtils.isAuroraInvisible(world))
			return false;
		return AuroraUtils.hasAuroras() && EnvironState.getPlayerBiome().getHasAurora();
	}

	private boolean canAuroraStay(@Nonnull final World world) {
		if (!ModOptions.auroraEnable)
			return false;

		return Minecraft.getMinecraft().gameSettings.renderDistanceChunks < 6
				|| DiurnalUtils.isAuroraVisible(world) && EnvironState.getPlayerBiome().getHasAurora();
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		// Process the current aurora
		if (current != null) {
			// If completed or the player changed dimensions we want to kill
			// outright
			if (current.isComplete() || dimensionId != EnvironState.getDimensionId() || !ModOptions.auroraEnable) {
				current = null;
			} else {
				current.update();
				final boolean isDying = current.isDying();
				final boolean canStay = canAuroraStay(player.worldObj);
				if (isDying && canStay) {
					DSurround.log().debug("Unfading aurora...");
					current.setFading(false);
				} else if (!isDying && !canStay) {
					DSurround.log().debug("Aurora fade...");
					current.setFading(true);
				}
			}
		}

		// If there isn't a current aurora see if it needs to spawn
		if (spawnAurora(player.worldObj)) {
			current = this.auroraEngine.produce(AuroraUtils.getSeed());
			DSurround.log().debug("New aurora [%s]", current.toString());
		}

		// Set the dimension in case it changed
		dimensionId = EnvironState.getDimensionId();
	}

}
