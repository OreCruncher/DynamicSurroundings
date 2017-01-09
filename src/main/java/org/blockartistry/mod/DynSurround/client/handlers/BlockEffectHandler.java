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

package org.blockartistry.mod.DynSurround.client.handlers;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.handlers.scanners.AlwaysOnBlockEffectScanner;
import org.blockartistry.mod.DynSurround.client.handlers.scanners.RandomBlockEffectScanner;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

/*
 * Based on doVoidParticles().
 */
@SideOnly(Side.CLIENT)
public class BlockEffectHandler extends EffectHandlerBase {

	//protected final RandomBlockEffectScanner effects = new RandomBlockEffectScannerThreaded(ModOptions.specialEffectRange);
	//protected final AlwaysOnBlockEffectScanner alwaysOn = new AlwaysOnBlockEffectScannerThreaded(ModOptions.specialEffectRange);

	protected final RandomBlockEffectScanner effects = new RandomBlockEffectScanner(ModOptions.specialEffectRange);
	protected final AlwaysOnBlockEffectScanner alwaysOn = new AlwaysOnBlockEffectScanner(ModOptions.specialEffectRange);

//	protected Future<?> effectsCall;
//	protected Future<?> alwaysOnCall;

	@Override
	public String getHandlerName() {
		return "BlockEffectHandler";
	}

	@Override
	public void pre() {
//		if (Minecraft.getMinecraft().isGamePaused())
//			return;
//
//		this.effectsCall = ScannerThreadPool.submit(this.effects);
//		this.alwaysOnCall = ScannerThreadPool.submit(this.alwaysOn);
	}

	@Override
	public void post() {
//		try {
//			if (this.effectsCall != null)
//				this.effectsCall.get();
//			if (this.alwaysOnCall != null)
//				this.alwaysOnCall.get();
//			ScannerThreadPool.processResults();
//		} catch (final InterruptedException e) {
//		} catch (final ExecutionException e) {
//		}
//		
//		this.effectsCall = null;
//		this.alwaysOnCall = null;
	}

	@Override
	public void process(final World world, final EntityPlayer player) {
		if (Minecraft.getMinecraft().isGamePaused())
			return;

		this.effects.update();
		this.alwaysOn.update();

		if (EnvironState.isPlayerOnGround() && EnvironState.isPlayerMoving()) {
			final BlockPos pos = EnvironState.getPlayerPosition().down(1);
			final IBlockState state = world.getBlockState(pos);
			final SoundEffect sound = getBlockRegistry().getStepSound(state, RANDOM);
			if (sound != null)
				sound.doEffect(state, world, pos, RANDOM);
		}
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
