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

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.handlers.scanners.AlwaysOnBlockEffectScanner;
import org.blockartistry.DynSurround.client.handlers.scanners.BiomeScanner;
import org.blockartistry.DynSurround.client.handlers.scanners.RandomBlockEffectScanner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AreaBlockEffectsHandler extends EffectHandlerBase {

	protected static AreaBlockEffectsHandler instance;

	public static AreaBlockEffectsHandler instance() {
		return instance;
	}

	protected final RandomBlockEffectScanner nearEffects = new RandomBlockEffectScanner(
			RandomBlockEffectScanner.NEAR_RANGE);
	protected final RandomBlockEffectScanner farEffects = new RandomBlockEffectScanner(
			RandomBlockEffectScanner.FAR_RANGE);
	protected final AlwaysOnBlockEffectScanner alwaysOn = new AlwaysOnBlockEffectScanner(
			ModOptions.general.specialEffectRange);
	protected final BiomeScanner biomes = new BiomeScanner();

	public AreaBlockEffectsHandler() {
		super("Area Block Effects");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		this.nearEffects.update();
		this.farEffects.update();
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
