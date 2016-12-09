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

import java.util.Collection;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.hud.GuiHUDHandler.IGuiOverlay;
import org.blockartistry.mod.DynSurround.util.Color;
import com.google.common.collect.Ordering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//net.minecraft.client.renderer.InventoryEffectRenderer

@SideOnly(Side.CLIENT)
public class PotionHUD extends Gui implements IGuiOverlay {

	private final Color TEXT_POTION_NAME = Color.WHITE;
	private final Color TEXT_DURATION = new Color(0x7F, 0x7F, 0x74);
	private final Color TEXT_DURATION_LOW = Color.RED;
	private final Color TEXT_AMBIENT = Color.GOLD;

	public void doRender(final RenderGameOverlayEvent.Pre event) {

		if (event.getType() != ElementType.POTION_ICONS) {
			return;
		}

		event.setCanceled(true);

		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		final Collection<PotionEffect> collection = player.getActivePotionEffects();
		if (collection.isEmpty())
			return;

		final ScaledResolution resolution = event.getResolution();
		final float GUITOP = ModOptions.potionHudTopOffset;
		final float GUILEFT = ModOptions.potionHudAnchor == 0 ? ModOptions.potionHudLeftOffset
				: resolution.getScaledWidth() - ModOptions.potionHudLeftOffset - 120 * ModOptions.potionHudScale;
		final float SCALE = ModOptions.potionHudScale;

		final Minecraft mc = Minecraft.getMinecraft();
		final FontRenderer font = mc.fontRendererObj;

		final int guiLeft = 2;
		int guiTop = 2;

		GlStateManager.pushMatrix();
		GlStateManager.translate(GUILEFT, GUITOP, 0.0F);
		GlStateManager.scale(SCALE, SCALE, SCALE);
		GlStateManager.enableAlpha();

		mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
		GlStateManager.enableBlend();

		int k = 33;

		if (collection.size() > 7) {
			k = 198 / (collection.size() - 1);
		}

		for (final PotionEffect potioneffect : Ordering.natural().reverse().sortedCopy(collection)) {

			final Potion potion = potioneffect.getPotion();
			if (!potion.shouldRenderHUD(potioneffect))
				continue;

			mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
			GlStateManager.enableBlend();

			GlStateManager.color(1.0F, 1.0F, 1.0F, ModOptions.potionHudTransparency);
			this.drawTexturedModalRect(guiLeft, guiTop, 0, 166, 140, 32);

			if (potion.hasStatusIcon()) {
				final int l = potion.getStatusIconIndex();
				this.drawTexturedModalRect(guiLeft + 6, guiTop + 7, 0 + l % 8 * 18, 198 + l / 8 * 18, 18, 18);
			}

			try {
				potion.renderInventoryEffect(guiLeft, guiTop, potioneffect, mc);
			} catch (final Exception ex) {
				;
			}

			if (!potion.shouldRenderInvText(potioneffect))
				continue;

			String s1 = I18n.format(potion.getName(), new Object[0]);

			if (potioneffect.getAmplifier() == 1) {
				s1 = s1 + " " + I18n.format("enchantment.level.2", new Object[0]);
			} else if (potioneffect.getAmplifier() == 2) {
				s1 = s1 + " " + I18n.format("enchantment.level.3", new Object[0]);
			} else if (potioneffect.getAmplifier() == 3) {
				s1 = s1 + " " + I18n.format("enchantment.level.4", new Object[0]);
			}

			font.drawStringWithShadow(s1, guiLeft + 10 + 18, guiTop + 6,
					TEXT_POTION_NAME.rgbWithAlpha(ModOptions.potionHudTransparency));

			float alpha = ModOptions.potionHudTransparency;
			Color color = potioneffect.getIsAmbient() ? TEXT_AMBIENT : TEXT_DURATION;
			final int threshold = potioneffect.getIsAmbient() ? 170 : 200;
			final int duration = potioneffect.getDuration();

			if (duration <= threshold) {
				color = TEXT_DURATION_LOW;
				if (((duration / 10) & 1) != 0)
					alpha /= 3.0F;
			}

			s1 = Potion.getPotionDurationString(potioneffect, 1.0F);
			font.drawStringWithShadow(s1, guiLeft + 10 + 18, guiTop + 6 + 10, color.rgbWithAlpha(alpha));

			guiTop += k;
		}
		GlStateManager.popMatrix();
	}
}
