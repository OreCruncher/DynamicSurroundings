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

import java.util.Collection;
import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.Localization;
import org.blockartistry.lib.collections.ObjectArray;

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

	private static final Color TEXT_POTION_NAME = Color.MC_WHITE;
	private static final Color TEXT_POTION_NAME_BAD = Color.MC_RED;
	private static final Color TEXT_POTION_NAME_AMBIENT = Color.MC_GOLD;
	private static final Color TEXT_POTION_DURATION = Color.MC_GREEN;
	private static final Color TEXT_POTION_DURATION_LOW = Color.MC_RED;
	private static final Color TEXT_POTION_DURATION_LOW_DARK = Color.MC_DARKRED;

	private static class PotionInfo {

		private final PotionEffect potionEffect;
		private final Potion potion;

		private final int effectColor;
		private final String effectText;
		private final int durationColor;
		private final String durationText;

		public PotionInfo(@Nonnull final PotionEffect potion) {
			this.potionEffect = potion;
			this.potion = this.potionEffect.getPotion();

			if (this.potion.shouldRenderInvText(this.potionEffect)) {

				String s1 = Localization.format(this.potion.getName(), new Object[0]);

				if (this.potionEffect.getAmplifier() == 1) {
					s1 = s1 + " " + Localization.format("enchantment.level.2", new Object[0]);
				} else if (this.potionEffect.getAmplifier() == 2) {
					s1 = s1 + " " + Localization.format("enchantment.level.3", new Object[0]);
				} else if (this.potionEffect.getAmplifier() == 3) {
					s1 = s1 + " " + Localization.format("enchantment.level.4", new Object[0]);
				}

				this.effectText = s1;
				this.effectColor = (this.potion.isBadEffect() ? TEXT_POTION_NAME_BAD
						: this.potionEffect.getIsAmbient() ? TEXT_POTION_NAME_AMBIENT : TEXT_POTION_NAME)
								.rgbWithAlpha(ModOptions.potionHudTransparency);

				final int threshold = this.potionEffect.getIsAmbient() ? 170 : 200;
				final int duration = this.potionEffect.getDuration();

				this.durationColor = (duration <= threshold
						? (((duration / 10) & 1) != 0 ? TEXT_POTION_DURATION_LOW_DARK : TEXT_POTION_DURATION_LOW)
						: TEXT_POTION_DURATION).rgbWithAlpha(ModOptions.potionHudTransparency);
				this.durationText = Potion.getPotionDurationString(this.potionEffect, 1.0F);

			} else {
				this.effectText = null;
				this.durationText = null;
				this.durationColor = -1;
				this.effectColor = -1;
			}
		}

		public PotionEffect getPotionEffect() {
			return this.potionEffect;
		}

		public Potion getPotion() {
			return this.potion;
		}

		public boolean hasStatusIcon() {
			return this.potion.hasStatusIcon();
		}

		public int getStatusIconIndex() {
			return this.potion.getStatusIconIndex();
		}

		public String getEffectText() {
			return this.effectText;
		}

		public int getEffectColor() {
			return this.effectColor;
		}

		public String getDurationText() {
			return this.durationText;
		}
		
		public int getDurationColor() {
			return this.durationColor;
		}
	}

	protected final ObjectArray<PotionInfo> potions = new ObjectArray<PotionInfo>();
	
	private boolean skipDisplay(@Nonnull final PotionEffect effect) {
		final Potion potion = effect.getPotion();
		return potion == null || !potion.shouldRenderHUD(effect) || !potion.shouldRenderInvText(effect);
	}

	@Override
	public void doTick(final int tickRef) {
		this.potions.clear();
		if (!ModOptions.potionHudEnabled || ModOptions.potionHudNone)
			return;

		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (player == null)
			return;

		final Collection<PotionEffect> collection = player.getActivePotionEffects();
		if (collection == null || collection.isEmpty())
			return;

		for (final PotionEffect effect : Ordering.natural().reverse().sortedCopy(collection)) {
			if (!this.skipDisplay(effect))
				this.potions.add(new PotionInfo(effect));
		}
	}

	public void doRender(final RenderGameOverlayEvent.Pre event) {

		if (ModOptions.potionHudNone && event.getType() == ElementType.POTION_ICONS) {
			event.setCanceled(true);
			return;
		}

		if (event.getType() != ElementType.POTION_ICONS || this.potions.size() == 0) {
			return;
		}

		event.setCanceled(true);

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

		if (this.potions.size() > 7) {
			k = 198 / (this.potions.size() - 1);
		}

		for (final PotionInfo potion : this.potions) {

			mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
			GlStateManager.enableBlend();

			GlStateManager.color(1.0F, 1.0F, 1.0F, ModOptions.potionHudTransparency);
			this.drawTexturedModalRect(guiLeft, guiTop, 0, 166, 140, 32);

			if (potion.hasStatusIcon()) {
				final int l = potion.getStatusIconIndex();
				this.drawTexturedModalRect(guiLeft + 6, guiTop + 7, 0 + l % 8 * 18, 198 + l / 8 * 18, 18, 18);
			}

			try {
				potion.getPotion().renderInventoryEffect(guiLeft, guiTop, potion.getPotionEffect(), mc);
			} catch (final Exception ex) {
				;
			}

			final String potionText = potion.getEffectText();
			final String durationText = potion.getDurationText();
			if (potionText != null) {
				font.drawStringWithShadow(potionText, guiLeft + 10 + 18, guiTop + 6, potion.getEffectColor());
				font.drawStringWithShadow(durationText, guiLeft + 10 + 18, guiTop + 6 + 10, potion.getDurationColor());
			}

			guiTop += k;
		}
		GlStateManager.popMatrix();
	}
}
