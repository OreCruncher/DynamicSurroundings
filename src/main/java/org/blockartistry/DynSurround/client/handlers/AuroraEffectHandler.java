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
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.weather.Aurora;
import org.blockartistry.DynSurround.client.weather.AuroraRenderer;
import org.blockartistry.DynSurround.registry.DimensionRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.DiurnalUtils;
import org.blockartistry.lib.random.MurmurHash3;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public final class AuroraEffectHandler extends EffectHandlerBase {

	// Aurora information
	private static Aurora current;
	private static int dimensionId;

	private final AuroraRenderer auroraRenderer;
	private final DimensionRegistry registry = RegistryManager.get(RegistryType.DIMENSION);

	public AuroraEffectHandler() {
		this.auroraRenderer = new AuroraRenderer();
	}

	@Nullable
	public static Aurora getCurrentAurora() {
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

	@Override
	@Nonnull
	public String getHandlerName() {
		return "AuroraEffectHandler";
	}

	private boolean spawnAurora(@Nonnull final World world) {
		if (!ModOptions.auroraEnable || current != null
				|| Minecraft.getMinecraft().gameSettings.renderDistanceChunks < 6
				|| DiurnalUtils.isAuroraInvisible(world))
			return false;
		return this.registry.hasAuroras(world) && EnvironState.getPlayerBiome().getHasAurora();
	}

	private boolean canAuroraStay(@Nonnull final World world) {
		return Minecraft.getMinecraft().gameSettings.renderDistanceChunks < 6
				|| DiurnalUtils.isAuroraVisible(world) && EnvironState.getPlayerBiome().getHasAurora();
	}

	private long getAuroraSeed(@Nonnull final World world) {
		return MurmurHash3.hash(world.getWorldTime() / 24000L);
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		// Process the current aurora
		if (current != null) {
			// If completed or the player changed dimensions we want to kill
			// outright
			if (current.isComplete() || dimensionId != EnvironState.getDimensionId()) {
				current = null;
			} else {
				current.update();
				if (current.isAlive() && !canAuroraStay(world)) {
					DSurround.log().debug("Aurora fade...");
					current.die();
				}
			}
		}

		// If there isn't a current aurora see if it needs to spawn
		if (spawnAurora(world)) {
			current = new Aurora(getAuroraSeed(world));
			DSurround.log().debug("New aurora [%s]", current.toString());
		}

		// Set the dimension in case it changed
		dimensionId = EnvironState.getDimensionId();
	}

	@SubscribeEvent
	public void doRender(@Nonnull final RenderWorldLastEvent event) {
		if (current != null)
			this.auroraRenderer.renderAurora(event.getPartialTicks(), current);
	}
}
