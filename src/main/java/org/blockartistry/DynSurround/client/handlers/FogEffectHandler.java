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

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.event.DiagnosticEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.handlers.scanners.AreaFogScanner;
import org.blockartistry.lib.Color;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class FogEffectHandler extends EffectHandlerBase {

	private AreaFogScanner scanner = new AreaFogScanner();

	public FogEffectHandler() {
	}

	@Override
	public String getHandlerName() {
		return "FogEffectHandler";
	}
	
	private static boolean ignoreFog() {
		return !(ModOptions.enableBiomeFog || ModOptions.allowDesertFog);
	}

	@Override
	public void process(final World world, final EntityPlayer player) {
		if(!ignoreFog())
			this.scanner.update();
	}

	/*
	 * Hook the fog color event so we can tell the renderer what color the fog
	 * should be.
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void fogColorEvent(final EntityViewRenderEvent.FogColors event) {

		if(ignoreFog())
			return;
		
		final IBlockState block = ActiveRenderInfo.getBlockStateAtEntityViewpoint(event.getEntity().world,
				event.getEntity(), (float) event.getRenderPartialTicks());
		if (block.getMaterial() == Material.LAVA || block.getMaterial() == Material.WATER)
			return;

		final Color color = this.scanner.getFogColor(EnvironState.getWorld(), (float) event.getRenderPartialTicks());
		if (color != null) {
			event.setRed(color.red);
			event.setGreen(color.green);
			event.setBlue(color.blue);
		}
	}

	/*
	 * Hook the fog density event so that the fog settings can be reset based on
	 * RAIN rainIntensity. This routine will overwrite what the vanilla code has
	 * done in terms of fog.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void fogRenderEvent(final EntityViewRenderEvent.RenderFogEvent event) {
		if (ignoreFog() || event.getResult() != Result.DEFAULT)
			return;

		final float planeDistance = this.scanner.getPlaneDistance(event.getFarPlaneDistance());
		if (event.getFogMode() < 0) {
			GlStateManager.setFogStart(0F);
			GlStateManager.setFogEnd(planeDistance);
		} else {
			GlStateManager.setFogStart(planeDistance * this.scanner.getPlaneDistanceScale());
			GlStateManager.setFogEnd(planeDistance);
		}

		event.setResult(Result.ALLOW);
	}

	@SubscribeEvent
	public void diagnostics(final DiagnosticEvent.Gather event) {
		if(ignoreFog())
			event.output.add("FOG: IGNORED");
		else
			event.output.add(this.scanner.toString());
	}

}
