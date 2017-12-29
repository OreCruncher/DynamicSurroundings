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

package org.blockartistry.DynSurround.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.DiurnalUtils;
import org.blockartistry.lib.Localization;
import org.blockartistry.lib.MathStuff;
import org.blockartistry.lib.PlayerUtils;
import org.blockartistry.lib.DiurnalUtils.DayCycle;
import org.blockartistry.lib.gui.TextPanel;
import org.blockartistry.lib.gui.Panel.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CompassHUD extends GuiOverlay {

	private static final int BAND_WIDTH = 65;
	private static final int BAND_HEIGHT = 12;
	private static final int ROSE_DIM = 256;

	private static final float TEXT_LINE_START = 1.5F;

	private static final String NO_SKY = Localization.format("format.NoSky");
	private static final String SUNRISE = Localization.format("format.Sunrise");
	private static final String SUNSET = Localization.format("format.Sunset");
	private static final String DAYTIME = Localization.format("format.Daytime");
	private static final String NIGHTTIME = Localization.format("format.Nighttime");

	private static enum Style {
		BAND_0(false, "textures/gui/compass/compass.png", BAND_WIDTH, BAND_HEIGHT), BAND_1(false,
				"textures/gui/compass/compass.png", BAND_WIDTH,
				BAND_HEIGHT), BAND_2(false, "textures/gui/compass/compass.png", BAND_WIDTH, BAND_HEIGHT), BAND_3(false,
						"textures/gui/compass/compass.png", BAND_WIDTH,
						BAND_HEIGHT), ROSE_1(true, "textures/gui/compass/compassrose1.png", ROSE_DIM, ROSE_DIM), ROSE_2(
								true, "textures/gui/compass/compassrose2.png", ROSE_DIM,
								ROSE_DIM), ROSE_3(true, "textures/gui/compass/compassrose3.png", ROSE_DIM, ROSE_DIM);

		private final boolean isRose;
		private final ResourceLocation texture;
		private final int width;
		private final int height;

		private Style(final boolean isRose, @Nonnull final String texture, final int w, final int h) {
			this.isRose = isRose;
			this.texture = new ResourceLocation(DSurround.RESOURCE_ID, texture);
			this.width = w;
			this.height = h;
		}

		public boolean isRose() {
			return this.isRose;
		}

		public ResourceLocation getTextureResource() {
			return this.texture;
		}

		public int getWidth() {
			return this.width;
		}

		public int getHeight() {
			return this.height;
		}

		public static Style getStyle(final int index) {
			if (index < 0 || index >= values().length)
				return BAND_0;
			return values()[index];
		}
	}

	private final TextPanel textPanel = new TextPanel();
	private boolean showCompass = false;

	@Nonnull
	protected String getLocationString() {
		final BlockPos pos = EnvironState.getPlayerPosition();
		return TextFormatting.AQUA
				+ String.format(Locale.getDefault(), ModOptions.compassCoordFormat, pos.getX(), pos.getY(), pos.getZ());
	}

	@Nonnull
	protected String getBiomeName() {
		return TextFormatting.GOLD + EnvironState.getBiomeName();
	}

	protected boolean showCompass() {
		return ModOptions.enableCompass && PlayerUtils.isHolding(EnvironState.getPlayer(), Items.COMPASS);
	}

	protected boolean showClock() {
		if (ModOptions.enableClock) {
			if (PlayerUtils.isHolding(EnvironState.getPlayer(), Items.CLOCK))
				return true;
			final Entity e = PlayerUtils.entityImLookingAt(EnvironState.getPlayer());
			if (e instanceof EntityItemFrame) {
				final ItemStack stack = ((EntityItemFrame) e).getDisplayedItem();
				return stack != null && !stack.isEmpty() && stack.getItem() == Items.CLOCK;
			}
		}
		return false;
	}

	@Nonnull
	private static String diurnalName() {
		final DayCycle cycle = DiurnalUtils.getCycle(EnvironState.getWorld());
		switch (cycle) {
		case NO_SKY:
			return CompassHUD.NO_SKY;
		case SUNRISE:
			return CompassHUD.SUNRISE;
		case SUNSET:
			return CompassHUD.SUNSET;
		case DAYTIME:
			return CompassHUD.DAYTIME;
		default:
			return CompassHUD.NIGHTTIME;
		}
	}

	@Override
	public void doTick(final int tickRef) {
		if (tickRef != 0 && tickRef % 4 == 0) {

			this.textPanel.resetText();

			final List<String> text = new ArrayList<String>();

			if (this.showCompass = showCompass()) {
				text.add(getLocationString());
				text.add(getBiomeName());
			}

			if (showClock()) {
				if (text.size() > 0)
					text.add("");

				long time = DSurround.proxy().currentSessionDuration();
				final int elapsedHours = (int) (time / 3600000);
				time -= elapsedHours * 3600000;
				final int elapsedMinutes = (int) (time / 60000);
				time -= elapsedMinutes * 60000;
				final int elapsedSeconds = (int) (time / 1000);

				text.add(EnvironState.getClock().toString());
				text.add(diurnalName());
				text.add(Localization.format("format.SessionTime", elapsedHours, elapsedMinutes, elapsedSeconds));
			}

			if (text.size() > 0)
				this.textPanel.setText(text);
		}
	}

	@Override
	public void doRender(@Nonnull final RenderGameOverlayEvent.Pre event) {

		if (event.getType() != ElementType.CROSSHAIRS || !this.textPanel.hasText())
			return;

		final Minecraft mc = Minecraft.getMinecraft();
		final FontRenderer font = mc.fontRenderer;

		final ScaledResolution resolution = event.getResolution();
		final int centerX = (resolution.getScaledWidth() + 1) / 2;
		final int centerY = (resolution.getScaledHeight() + 1) / 2;

		this.textPanel.setAlpha(ModOptions.compassTransparency);
		this.textPanel.render(centerX, centerY + (int) (font.FONT_HEIGHT * TEXT_LINE_START), Reference.TOP_CENTER);

		final Style style = Style.getStyle(ModOptions.compassStyle);
		mc.getTextureManager().bindTexture(style.getTextureResource());

		GlStateManager.color(1F, 1F, 1F, ModOptions.compassTransparency);

		if (this.showCompass)
			if (!style.isRose()) {

				final int direction = MathHelper.floor(((mc.player.rotationYaw * 256F) / 360F) + 0.5D) & 255;
				final int x = (resolution.getScaledWidth() - style.getWidth() + 1) / 2;
				final int y = (resolution.getScaledHeight() - style.getHeight() + 1) / 2 - style.getHeight();

				if (direction < 128)
					drawTexturedModalRect(x, y, direction, (ModOptions.compassStyle * (style.getHeight() * 2)),
							style.getWidth(), style.getHeight());
				else
					drawTexturedModalRect(x, y, direction - 128,
							(ModOptions.compassStyle * (style.getHeight() * 2)) + style.getHeight(), style.getWidth(),
							style.getHeight());
			} else {
				GlStateManager.pushMatrix();
				GlStateManager.translate(centerX, centerY - BAND_HEIGHT * 2.5F, 0);
				GlStateManager.rotate(70, 1F, 0F, 0F);
				GlStateManager.rotate(-MathStuff.wrapDegrees(mc.player.rotationYaw + 180F), 0F, 0F, 1F);
				final int x = -(style.getWidth() + 1) / 2;
				final int y = -(style.getHeight() + 1) / 2;
				drawTexturedModalRect(x, y, 0, 0, style.getWidth(), style.getHeight());
				GlStateManager.popMatrix();
			}

		GlStateManager.color(1F, 1F, 1F, 1F);
	}
}
