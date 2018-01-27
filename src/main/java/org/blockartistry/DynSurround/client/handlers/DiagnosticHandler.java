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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.swing.DiagnosticPanel;
import org.blockartistry.DynSurround.event.DiagnosticEvent;
import org.blockartistry.DynSurround.event.ServerDataEvent;

import com.google.common.collect.ImmutableList;

import gnu.trove.procedure.TIntDoubleProcedure;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Calculates and caches predefined sets of data points for script evaluation
 * during a tick. Goal is to minimize script evaluation overhead as much as
 * possible.
 */
@SideOnly(Side.CLIENT)
public class DiagnosticHandler extends EffectHandlerBase {

	// Diagnostic strings to display in the debug HUD
	private List<String> diagnostics = ImmutableList.of();

	// TPS status strings to display
	private List<String> serverDataReport = ImmutableList.of();

	public DiagnosticHandler() {
		super("DiagnosticHandler");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		// Gather diagnostics if needed
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo && ModOptions.enableDebugLogging) {
			final DiagnosticEvent.Gather gather = new DiagnosticEvent.Gather(player.worldObj, player);
			MinecraftForge.EVENT_BUS.post(gather);
			this.diagnostics = gather.output;
		} else {
			this.diagnostics = null;
		}

		if (ModOptions.showDebugDialog)
			DiagnosticPanel.refresh();
	}

	@Override
	public void onConnect() {
		this.diagnostics = null;
		this.serverDataReport = null;

		if (ModOptions.showDebugDialog)
			DiagnosticPanel.create();
	}

	@Override
	public void onDisconnect() {
		this.diagnostics = null;
		this.serverDataReport = null;

		if (ModOptions.showDebugDialog)
			DiagnosticPanel.destroy();
	}

	/**
	 * Hook the Forge text event to add on our diagnostics
	 */
	@SubscribeEvent
	public void onGatherText(@Nonnull final RenderGameOverlayEvent.Text event) {
		if (this.diagnostics != null && !this.diagnostics.isEmpty()) {
			event.getLeft().add("");
			event.getLeft().addAll(this.diagnostics);
		}

		if (Minecraft.getMinecraft().gameSettings.showDebugInfo && this.serverDataReport != null) {
			event.getRight().add(" ");
			event.getRight().addAll(this.serverDataReport);
		}
	}

	@Nonnull
	private static TextFormatting getTpsFormatPrefix(final int tps) {
		if (tps <= 10)
			return TextFormatting.RED;
		if (tps <= 15)
			return TextFormatting.YELLOW;
		return TextFormatting.GREEN;
	}

	@SubscribeEvent
	public void serverDataEvent(final ServerDataEvent event) {
		final ArrayList<String> data = new ArrayList<String>();

		final int diff = event.total - event.free;

		data.add(TextFormatting.GOLD + "Server Information");
		data.add(String.format("Mem: %d%% %03d/%3dMB", diff * 100 / event.max, diff, event.max));
		data.add(String.format("Allocated: %d%% %3dMB", event.total * 100 / event.max, event.total));
		final int tps = (int) Math.min(1000.0D / event.meanTickTime, 20.0D);
		data.add(String.format("Ticktime Overall:%s %5.3fms (%d TPS)", getTpsFormatPrefix(tps), event.meanTickTime,
				tps));
		event.dimTps.forEachEntry(new TIntDoubleProcedure() {
			@Override
			public boolean execute(int a, double b) {
				final String dimName = DimensionManager.getProviderType(a).getName();
				final int tps = (int) Math.min(1000.0D / b, 20.0D);
				data.add(String.format("%s (%d):%s %7.3fms (%d TPS)", dimName, a, getTpsFormatPrefix(tps), b, tps));
				return true;
			}

		});

		Collections.sort(data.subList(4, data.size()));
		this.serverDataReport = data;
	}

}
