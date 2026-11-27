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

package org.orecruncher.dsurround.client.handlers;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.scanners.AlwaysOnBlockEffectScanner;
import org.orecruncher.dsurround.client.handlers.scanners.ClientPlayerLocus;
import org.orecruncher.dsurround.client.handlers.scanners.RandomBlockEffectScanner;
import org.orecruncher.dsurround.proxy.ProxyClient;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AreaBlockEffectsHandler extends EffectHandlerBase {

	//@formatter:off
	protected final ClientPlayerLocus locus = new ClientPlayerLocus();
	protected final RandomBlockEffectScanner nearEffects = new RandomBlockEffectScanner(
		this.locus,
		RandomBlockEffectScanner.NEAR_RANGE
	);
	protected final RandomBlockEffectScanner farEffects = new RandomBlockEffectScanner(
		this.locus,
		RandomBlockEffectScanner.FAR_RANGE
	);
	protected final AlwaysOnBlockEffectScanner alwaysOn = new AlwaysOnBlockEffectScanner(
		this.locus,
		ModOptions.effects.blockEffectRange
	);
	//@formatter:on

	// The range the chunk cache was last sized for (ProxyClient sizes it at startup)
	protected int cacheRange = ProxyClient.chunkCacheRange();

	public AreaBlockEffectsHandler() {
		super("Area Block Effects");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		// A changed range starts the scan over (blocks are only looked at as they come into range, so
		// effects past a lowered range would never come back), and the chunk cache the effects read their
		// neighbors through has to reach as far. The cache is resized first: a new one is empty until the
		// next tick fills it, and a scan started against it would find no neighbors.
		final int range = ModOptions.effects.blockEffectRange;
		final int cache = ProxyClient.chunkCacheRange();
		final boolean resized = cache != this.cacheRange;
		if (resized) {
			ProxyClient.sizeChunkCache();
			this.cacheRange = cache;
		} else {
			this.alwaysOn.setRange(range);
		}
		this.nearEffects.update();
		this.farEffects.update();
		if (!resized)
			this.alwaysOn.update();
	}

	@Override
	public void onConnect() {
		MinecraftForge.EVENT_BUS.register(this.alwaysOn);
	}

	@Override
	public void onDisconnect() {
		MinecraftForge.EVENT_BUS.unregister(this.alwaysOn);
	}

}
