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

import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Nonnull;

import org.lwjgl.input.Keyboard;
import org.orecruncher.dsurround.client.keyboard.KeyHandler;
import org.orecruncher.dsurround.client.renderer.BadgeRenderLayer.IItemStackProvider;
import org.orecruncher.dsurround.client.renderer.BadgeRenderLayer.IShowBadge;

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
public final class AnimaniaBadge2 implements IItemStackProvider {

	// Possible food/water items for badging.
	private final static ItemStack WATER_BUCKET = new ItemStack(Items.WATER_BUCKET);

	private static Class<?> xface;
	private static Method getFed;
	private static Method getWatered;
	private static Method getFoodItems;

	static {
		try {
			xface = Class.forName("com.animania.common.entities.interfaces.IFoodEating");
			getFed = xface.getMethod("getFed");
			getWatered = xface.getMethod("getWatered");
			getFoodItems = xface.getMethod("getFoodItems");
		} catch (@Nonnull final Throwable t) {
			xface = null;
			getFed = null;
			getWatered = null;
			getFoodItems = null;
		}
	}

	private static final IShowBadge BADGE_DISPLAY_CHECK = () -> {
		return KeyHandler.ANIMANIA_BADGES == null || KeyHandler.ANIMANIA_BADGES.isKeyDown()
				|| KeyHandler.ANIMANIA_BADGES.getKeyCode() == Keyboard.KEY_NONE;
	};

	private AnimaniaBadge2() {
	}

	@Override
	public float adjustY() {
		return 0;
	}

	@Override
	public ItemStack getStackToDisplay(final EntityLivingBase e) {
		if (!getWatered(e))
			return WATER_BUCKET;
		else if (!getFed(e))
			return getFoodItem(e);
		else
			return ItemStack.EMPTY;
	}

	private boolean getFed(final Entity e) {
		try {
			return (boolean) AnimaniaBadge2.getFed.invoke(e);
		} catch (final Throwable t) {
			return true;
		}
	}

	private boolean getWatered(final Entity e) {
		try {
			return (boolean) AnimaniaBadge2.getWatered.invoke(e);
		} catch (final Throwable t) {
			return true;
		}
	}

	private ItemStack getFoodItem(final Entity e) {
		try {
			@SuppressWarnings("unchecked")
			final Set<Item> food = (Set<Item>) AnimaniaBadge2.getFoodItems.invoke(e);
			if (food.size() > 0) {
				final Item item = food.iterator().next();
				return new ItemStack(item);
			}
			return ItemStack.EMPTY;
		} catch (final Throwable t) {
			return ItemStack.EMPTY;
		}
	}

	// ====================================================================
	//
	// Initialization for the entity models and what not
	//
	// ====================================================================
	public static void intitialize() {

		// Iterate our entity class hierarchy looking for matches and make sure
		// they are in the map directly - avoid repeated lookups at run time.
		final AnimaniaBadge2 singleton = new AnimaniaBadge2();
		final RenderManager rm = Minecraft.getMinecraft().getRenderManager();
		for (final ResourceLocation r : EntityList.getEntityNameList()) {
			final Class<? extends Entity> clazz = EntityList.getClass(r);
			if (clazz != null && xface.isAssignableFrom(clazz)) {
				final Render<Entity> renderer = rm.getEntityClassRenderObject(clazz);
				if (renderer instanceof RenderLivingBase) {
					((RenderLivingBase<?>) renderer).addLayer(new BadgeRenderLayer(BADGE_DISPLAY_CHECK, singleton));
				}
			}
		}
	}
}
