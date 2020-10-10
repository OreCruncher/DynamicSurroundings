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

package org.orecruncher.dsurround.client.renderer;

import java.util.Set;

import javax.annotation.Nonnull;

import org.lwjgl.input.Keyboard;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.keyboard.KeyHandler;
import org.orecruncher.dsurround.client.renderer.BadgeRenderLayer.IBadgeDisplayCheck;
import org.orecruncher.dsurround.client.renderer.BadgeRenderLayer.IEntityBadgeProvider;

import com.animania.addons.farm.common.entity.pigs.EntityAnimaniaPig;
import com.animania.addons.farm.common.entity.pigs.EntityPigletBase;
import com.animania.addons.farm.common.entity.sheep.EntityEweBase;
import com.animania.api.interfaces.IFoodEating;
import com.animania.api.interfaces.ISleeping;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AnimaniaBadge implements IEntityBadgeProvider {

	// Possible food/water items for badging.
	private final static ItemStack WATER_BUCKET = new ItemStack(Items.WATER_BUCKET);

	private static final IBadgeDisplayCheck BADGE_DISPLAY_CHECK = () -> KeyHandler.ANIMANIA_BADGES == null
			|| KeyHandler.ANIMANIA_BADGES.isKeyDown() || KeyHandler.ANIMANIA_BADGES.getKeyCode() == Keyboard.KEY_NONE;

	@Override
	public ItemStack getStackToDisplay(final EntityLivingBase e) {
		final IFoodEating fe = (IFoodEating) e;
		if (!fe.getWatered())
			return WATER_BUCKET;
		else if (!fe.getFed())
			return getFoodItem(fe);
		else
			return ItemStack.EMPTY;
	}

	private ItemStack getFoodItem(IFoodEating fe) {
		final Set<ItemStack> food = fe.getFoodItems();
		if (food.size() > 0) {
			final ItemStack item = food.iterator().next();
			return item.copy();
		}
		return ItemStack.EMPTY;
	}

	/**
	 * Micro adjustment of vertical position of the badge
	 *
	 * @param e Entity that is being rendered
	 * @return Vertical adjustment to the icon position
	 */
	@Override
	public float adjustY(@Nonnull final EntityLivingBase e) {
		if (e instanceof EntityEweBase)
			return 1F;
		if (e instanceof EntityPigletBase)
			return -0.5F;
		return 0.15F;
	}

	/**
	 * Scale factor to apply to the badge when rendering
	 *
	 * @param e Entity that is being rendered
	 * @return Scale factor to apply to icon render
	 */
	@Override
	public float scale(@Nonnull final EntityLivingBase e) {
		if (e instanceof EntityEweBase)
			return 1.5F;
		if (e instanceof EntityAnimaniaPig && !(e instanceof EntityPigletBase))
			return 0.75F;
		return 0.5F;
	}

	/**
	 * Checks to see if the badge should be shown. Good for conditionals like
	 * sleeping, etc.
	 *
	 * @param e Entity that is being rendered
	 * @return true if a badges is to be shown; false otherwise
	 */
	@Override
	public boolean show(@Nonnull final EntityLivingBase e) {
		if (e instanceof ISleeping)
			return !((ISleeping) e).getSleeping();
		return true;
	}

	// ====================================================================
	//
	// Initialization for the entity models and what not
	//
	// ====================================================================
	public static void intitialize() {

		if (!ModOptions.speechbubbles.enableAnimaniaBadges)
			return;

		// Iterate our entity class hierarchy looking for matches and make sure
		// they are in the map directly - avoid repeated lookups at run time.
		final BadgeRenderLayer layer = new BadgeRenderLayer(BADGE_DISPLAY_CHECK, new AnimaniaBadge());
		final RenderManager rm = Minecraft.getMinecraft().getRenderManager();
		for (final ResourceLocation r : EntityList.getEntityNameList()) {
			final Class<? extends Entity> clazz = EntityList.getClass(r);
			if (clazz != null && IFoodEating.class.isAssignableFrom(clazz)) {
				final Render<Entity> renderer = rm.getEntityClassRenderObject(clazz);
				if (renderer instanceof RenderLivingBase) {
					((RenderLivingBase<?>) renderer).addLayer(layer);
				}
			}
		}
	}
}
