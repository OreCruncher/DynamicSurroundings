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

package org.blockartistry.mod.DynSurround.client.hud;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.hud.GuiHUDHandler.IGuiOverlay;
import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CompassHUD extends Gui implements IGuiOverlay {

	private static final int WIDTH = 65;
	private static final int HEIGHT = 12;

	private static final Color COORDINATE_COLOR = Color.MC_AQUA;
	private static final Color BIOME_NAME_COLOR = Color.MC_GOLD;
	private static final ResourceLocation TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/gui/compass.png");

	@Nonnull
	protected String getLocationString() {
		final StringBuilder builder = new StringBuilder();
		final BlockPos pos = EnvironState.getPlayerPosition();
		return builder.append("x: ").append(pos.getX()).append(", z: ").append(pos.getZ()).toString();
	}

	@Nonnull
	protected String getBiomeName() {
		return EnvironState.getBiomeName();
	}

	protected boolean showCompass() {
		return ModOptions.enableCompass && PlayerUtils.isHolding(EnvironState.getPlayer(), Items.COMPASS);
	}

	@Override
	public void doRender(@Nonnull final RenderGameOverlayEvent.Pre event) {

		if (event.getType() != ElementType.CROSSHAIRS || !showCompass())
			return;

		final Minecraft mc = Minecraft.getMinecraft();
		final ScaledResolution resolution = event.getResolution();
		final int x = (resolution.getScaledWidth() - WIDTH + 1) / 2;
		final int y = (resolution.getScaledHeight() - HEIGHT + 1) / 2 - HEIGHT;
		final int direction = MathHelper.floor_double(((mc.thePlayer.rotationYaw * 256F) / 360F) + 0.5D) & 255;

		mc.getTextureManager().bindTexture(TEXTURE);
		GlStateManager.color(1F, 1F, 1F, ModOptions.compassTransparency);
		GlStateManager.enableBlend();

		if (direction < 128)
			drawTexturedModalRect(x, y, direction, (ModOptions.compassStyle * (HEIGHT * 2)), WIDTH, HEIGHT);
		else
			drawTexturedModalRect(x, y, direction - 128, (ModOptions.compassStyle * (HEIGHT * 2)) + HEIGHT, WIDTH,
					HEIGHT);

		String temp = getLocationString();
		mc.fontRendererObj.drawStringWithShadow(temp, x + (WIDTH - mc.fontRendererObj.getStringWidth(temp)) / 2,
				y + HEIGHT * 3, COORDINATE_COLOR.rgbWithAlpha(ModOptions.compassTransparency));

		temp = getBiomeName();
		mc.fontRendererObj.drawStringWithShadow(temp, x + (WIDTH - mc.fontRendererObj.getStringWidth(temp)) / 2,
				y + HEIGHT * 4, BIOME_NAME_COLOR.rgbWithAlpha(ModOptions.compassTransparency));

		GlStateManager.color(1F, 1F, 1F, 1F);
	}
}
