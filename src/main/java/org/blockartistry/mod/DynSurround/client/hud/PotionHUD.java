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
import org.blockartistry.mod.DynSurround.client.hud.GuiHUDHandler.GuiOverlay;
import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.Localization;

import com.google.common.collect.Ordering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//net.minecraft.client.renderer.InventoryEffectRenderer

@SideOnly(Side.CLIENT)
public class PotionHUD extends GuiOverlay {

	private final Color TEXT_POTION_NAME = Color.WHITE;
	private final Color TEXT_POTION_NAME_BAD = Color.RED;
	private final Color TEXT_POTION_NAME_AMBIENT = Color.GOLD;
	private final Color TEXT_DURATION = new Color(220, 220, 220);
	private final Color TEXT_DURATION_LOW = Color.RED;

	public void doRender(final RenderGameOverlayEvent.Pre event) {
		
		if(ModOptions.potionHudNone && event.getType() == ElementType.POTION_ICONS) {
			event.setCanceled(true);
			return;
		}

		if (!ModOptions.potionHudEnabled || event.getType() != ElementType.POTION_ICONS) {
			return;
		}

		event.setCanceled(true);

		final EntityPlayer player = Minecraft.getMinecraft().player;
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

		int k = 33;

		if (collection.size() > 7) {
			k = 198 / (collection.size() - 1);
		}

		for (final PotionEffect effect : Ordering.natural().reverse().sortedCopy(collection)) {

			final Potion potion = effect.getPotion();
			if (!potion.shouldRenderHUD(effect))
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
				potion.renderInventoryEffect(guiLeft, guiTop, effect, mc);
			} catch (final Exception ex) {
				;
			}

			if (!potion.shouldRenderInvText(effect))
				continue;

			String s1 = Localization.format(potion.getName(), new Object[0]);

			if (effect.getAmplifier() == 1) {
				s1 = s1 + " " + Localization.format("enchantment.level.2", new Object[0]);
			} else if (effect.getAmplifier() == 2) {
				s1 = s1 + " " + Localization.format("enchantment.level.3", new Object[0]);
			} else if (effect.getAmplifier() == 3) {
				s1 = s1 + " " + Localization.format("enchantment.level.4", new Object[0]);
			}

			Color color = potion.isBadEffect() ? TEXT_POTION_NAME_BAD
					: effect.getIsAmbient() ? TEXT_POTION_NAME_AMBIENT : TEXT_POTION_NAME;
			font.drawStringWithShadow(s1, guiLeft + 10 + 18, guiTop + 6,
					color.rgbWithAlpha(ModOptions.potionHudTransparency));

			float alpha = ModOptions.potionHudTransparency;
			final int threshold = effect.getIsAmbient() ? 170 : 200;
			final int duration = effect.getDuration();

			color = TEXT_DURATION;
			if (duration <= threshold) {
				color = TEXT_DURATION_LOW;
				if (((duration / 10) & 1) != 0)
					alpha /= 3.0F;
			}

			s1 = Potion.getPotionDurationString(effect, 1.0F);
			font.drawStringWithShadow(s1, guiLeft + 10 + 18, guiTop + 6 + 10, color.rgbWithAlpha(alpha));

			guiTop += k;
		}
		GlStateManager.popMatrix();
	}
}
