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
import org.blockartistry.DynSurround.client.swing.DiagnosticPanel;
import org.blockartistry.DynSurround.expression.ExpressionEngine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Calculates and caches predefined sets of data points for script evaluation
 * during a tick. Goal is to minimize script evaluation overhead as much as
 * possible.
 */
@SideOnly(Side.CLIENT)
public class ExpressionStateHandler extends EffectHandlerBase {

	public ExpressionStateHandler() {
		super("ExpressionStateHandler");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		ExpressionEngine.instance().update();

		if (ModOptions.showDebugDialog)
			DiagnosticPanel.refresh();
	}

	@Override
	public void onConnect() {
		if (ModOptions.showDebugDialog)
			DiagnosticPanel.create();
	}

	@Override
	public void onDisconnect() {
		if (ModOptions.showDebugDialog)
			DiagnosticPanel.destroy();
	}

}
