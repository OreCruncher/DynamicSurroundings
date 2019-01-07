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

package org.orecruncher.dsurround.client.gui;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.lib.ReflectedField.ObjectField;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Bulk of the class is taken from Vanilla and tweaked. I could have made this
 * cleaner by using the AT file but I am trying to avoid doing that.
 */
@EventBusSubscriber(value = Side.CLIENT, modid = ModInfo.MOD_ID)
public class GuiScreenOptionsSoundReplace extends GuiScreenOptionsSounds {

	private static final int SLIDER_HEIGHT = 22; // 24

	private static final ObjectField<GuiScreenOptionsSounds, GuiScreen> parent = new ObjectField<>(
			GuiScreenOptionsSounds.class, "parent", "field_146505_f");

	private final GameSettings settings;
	private String offDisplayString;

	public GuiScreenOptionsSoundReplace(GuiScreen parentIn, GameSettings settingsIn) {
		super(parentIn, settingsIn);
		this.settings = settingsIn;
	}

	@Override
	public void initGui() {
		this.title = I18n.format("options.sounds.title");
		this.offDisplayString = I18n.format("options.off");
		int i = 0;
		this.buttonList.add(new GuiScreenOptionsSoundReplace.Button(SoundCategory.MASTER.ordinal(),
				this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + SLIDER_HEIGHT * (i >> 1),
				SoundCategory.MASTER, true));
		i = i + 2;

		for (final SoundCategory soundcategory : SoundCategory.values()) {
			if (soundcategory != SoundCategory.MASTER) {
				this.buttonList.add(new GuiScreenOptionsSoundReplace.Button(soundcategory.ordinal(),
						this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + SLIDER_HEIGHT * (i >> 1),
						soundcategory, false));
				++i;
			}
		}

		final int j = this.width / 2 - 75;
		final int k = this.height / 6 - 12;
		++i;
		this.buttonList.add(new GuiOptionButton(201, j, k + SLIDER_HEIGHT * (i >> 1),
				GameSettings.Options.SHOW_SUBTITLES, this.settings.getKeyBinding(GameSettings.Options.SHOW_SUBTITLES)));
		this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done")));
	}

	@Override
	protected String getDisplayString(SoundCategory category) {
		final float f = this.settings.getSoundLevel(category);
		return f == 0.0F ? this.offDisplayString : (int) (f * 100.0F) + "%";
	}

	@SideOnly(Side.CLIENT)
	class Button extends GuiButton {
		private final SoundCategory category;
		private final String categoryName;
		public float volume = 1.0F;
		public boolean pressed;

		public Button(int buttonId, int x, int y, SoundCategory categoryIn, boolean master) {
			super(buttonId, x, y, master ? 310 : 150, 20, "");
			this.category = categoryIn;
			this.categoryName = I18n.format("soundCategory." + categoryIn.getName());
			this.displayString = this.categoryName + ": " + getDisplayString(categoryIn);
			this.volume = GuiScreenOptionsSoundReplace.this.settings.getSoundLevel(categoryIn);
		}

		/**
		 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this
		 * button and 2 if it IS hovering over this button.
		 */
		@Override
		protected int getHoverState(boolean mouseOver) {
			return 0;
		}

		/**
		 * Fired when the mouse button is dragged. Equivalent of
		 * MouseListener.mouseDragged(MouseEvent e).
		 */
		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			if (this.visible) {
				if (this.pressed) {
					this.volume = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
					this.volume = MathHelper.clamp(this.volume, 0.0F, 1.0F);
					mc.gameSettings.setSoundLevel(this.category, this.volume);
					mc.gameSettings.saveOptions();
					this.displayString = this.categoryName + ": " + getDisplayString(this.category);
				}

				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				this.drawTexturedModalRect(this.x + (int) (this.volume * (this.width - 8)), this.y, 0, 66, 4, 20);
				this.drawTexturedModalRect(this.x + (int) (this.volume * (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
			}
		}

		/**
		 * Returns true if the mouse has been pressed on this control. Equivalent of
		 * MouseListener.mousePressed(MouseEvent e).
		 */
		@Override
		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			if (super.mousePressed(mc, mouseX, mouseY)) {
				this.volume = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
				this.volume = MathHelper.clamp(this.volume, 0.0F, 1.0F);
				mc.gameSettings.setSoundLevel(this.category, this.volume);
				mc.gameSettings.saveOptions();
				this.displayString = this.categoryName + ": " + getDisplayString(this.category);
				this.pressed = true;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void playPressSound(SoundHandler soundHandlerIn) {
		}

		/**
		 * Fired when the mouse button is released. Equivalent of
		 * MouseListener.mouseReleased(MouseEvent e).
		 */
		@Override
		public void mouseReleased(int mouseX, int mouseY) {
			if (this.pressed) {
				GuiScreenOptionsSoundReplace.this.mc.getSoundHandler()
						.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}

			this.pressed = false;
		}
	}

	@SubscribeEvent
	public static void guiOpenHandler(@Nonnull final GuiOpenEvent evt) {
		if (evt.getGui() instanceof GuiScreenOptionsSounds) {
			try {
				final GuiScreen p = parent.get((GuiScreenOptionsSounds) evt.getGui());
				evt.setGui(new GuiScreenOptionsSoundReplace(p, Minecraft.getMinecraft().gameSettings));
				evt.setResult(Result.ALLOW);
			} catch (@Nonnull final Throwable t) {
				ModBase.log().error("Unable to replace volume control screen", t);
			}
		}
	}

}
