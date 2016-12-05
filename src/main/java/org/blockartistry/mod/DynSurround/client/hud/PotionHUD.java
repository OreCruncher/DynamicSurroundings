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
import java.util.Iterator;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.hud.GuiHUDHandler.IGuiOverlay;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//net.minecraft.client.renderer.InventoryEffectRenderer

@SideOnly(Side.CLIENT)
public class PotionHUD extends Gui implements IGuiOverlay {

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/inventory.png");

	public void doRender(final RenderGameOverlayEvent event) {

		if (event.isCancelable() || event.getType() != ElementType.EXPERIENCE) {
			return;
		}

		final int TEXT_POTION_NAME = (int) (255 * ModOptions.potionHudTransparency) << 24 | 0xFFFFFF;
		final int TEXT_DURATION = (int) (255 * ModOptions.potionHudTransparency) << 24 | 0x7F7F7F;
		final int TEXT_DURATION_LOW = (int) (255 * ModOptions.potionHudTransparency) << 24 | 0xFF0000;
		final float GUITOP = ModOptions.potionHudTopOffset;
		final float GUILEFT = ModOptions.potionHudLeftOffset;
		final float SCALE = ModOptions.potionHudScale;

		final Minecraft mc = Minecraft.getMinecraft();
		final FontRenderer font = mc.fontRendererObj;
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		int guiLeft = 2;
		int guiTop = 2;

		final Collection<PotionEffect> collection = player.getActivePotionEffects();

		if (!collection.isEmpty()) {

			GL11.glPushMatrix();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, ModOptions.potionHudTransparency);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glTranslatef(GUILEFT, GUITOP, 0.0F);
			GL11.glScalef(SCALE, SCALE, SCALE);
			int k = 33;

			if (collection.size() > 7) {
				k = 198 / (collection.size() - 1);
			}

			for (Iterator<PotionEffect> iterator = collection.iterator(); iterator.hasNext(); guiTop += k) {
				final PotionEffect potioneffect = iterator.next();
				final Potion potion = potioneffect.getPotion();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, ModOptions.potionHudTransparency);
				mc.getTextureManager().bindTexture(TEXTURE);
				this.drawTexturedModalRect(guiLeft, guiTop, 0, 166, 140, 32);

				if (potion.hasStatusIcon()) {
					int l = potion.getStatusIconIndex();
					this.drawTexturedModalRect(guiLeft + 6, guiTop + 7, 0 + l % 8 * 18, 198 + l / 8 * 18, 18, 18);
				}

				try {
					potion.renderInventoryEffect(guiLeft, guiTop, potioneffect, mc);
				} catch(final Exception ex) {
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

				font.drawStringWithShadow(s1, guiLeft + 10 + 18, guiTop + 6, TEXT_POTION_NAME);
				String s = Potion.getPotionDurationString(potioneffect, 1.0F);
				font.drawStringWithShadow(s, guiLeft + 10 + 18, guiTop + 6 + 10,
						potioneffect.getDuration() <= 200 ? TEXT_DURATION_LOW : TEXT_DURATION);
			}
			GL11.glPopMatrix();
		}
	}
}
