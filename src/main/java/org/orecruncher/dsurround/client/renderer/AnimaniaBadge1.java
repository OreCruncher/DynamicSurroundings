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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.lwjgl.input.Keyboard;
import org.orecruncher.dsurround.client.keyboard.KeyHandler;
import org.orecruncher.dsurround.client.renderer.BadgeRenderLayer.IItemStackProvider;
import org.orecruncher.dsurround.client.renderer.BadgeRenderLayer.IShowBadge;
import org.orecruncher.lib.ForgeUtils;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
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
public final class AnimaniaBadge1 implements IItemStackProvider {

	// Possible food/water items for badging.
	private final static ItemStack WATER_BUCKET = new ItemStack(Items.WATER_BUCKET);
	private final static ItemStack SEEDS = new ItemStack(Items.WHEAT_SEEDS);
	private final static ItemStack WHEAT = new ItemStack(Items.WHEAT);
	private final static ItemStack APPLE = new ItemStack(Items.APPLE);
	private final static ItemStack CARROT = new ItemStack(Items.CARROT);
	private final static ItemStack EGG = new ItemStack(Items.EGG);
	private final static ItemStack RABBIT_FOOD = CARROT;
	private final static ItemStack HAMSTER_FOOD;

	private static final IShowBadge BADGE_DISPLAY_CHECK = () -> {
		return KeyHandler.ANIMANIA_BADGES == null || KeyHandler.ANIMANIA_BADGES.isKeyDown()
				|| KeyHandler.ANIMANIA_BADGES.getKeyCode() == Keyboard.KEY_NONE;
	};

	static {
		final Item item = ForgeUtils.getItem("animania:hamster_food");
		HAMSTER_FOOD = item != null ? new ItemStack(item) : ItemStack.EMPTY;
	}

	private Method food;
	private Method water;
	private ItemStack foodItem;

	private float dY;

	private AnimaniaBadge1(final Class<?> clazz, final ItemStack food, final float dY) {
		try {
			this.food = clazz.getMethod("getFed");
			this.water = clazz.getMethod("getWatered");
			this.foodItem = food;
			this.dY = dY;
		} catch (final Throwable t) {
			this.food = null;
			this.water = null;
		}
	}

	@Override
	public float adjustY() {
		return this.dY;
	}

	@Override
	public ItemStack getStackToDisplay(final EntityLivingBase e) {
		if (!getWatered(e))
			return WATER_BUCKET;
		else if (!getFed(e))
			return this.foodItem;
		else
			return ItemStack.EMPTY;
	}

	private boolean getFed(final Entity e) {
		try {
			return (boolean) this.food.invoke(e);
		} catch (final Throwable t) {
			return true;
		}
	}

	private boolean getWatered(final Entity e) {
		try {
			return (boolean) this.water.invoke(e);
		} catch (final Throwable t) {
			return true;
		}
	}

	private boolean isValid() {
		return this.food != null && this.water != null;
	}

	// ====================================================================
	//
	// Initialization for the entity models and what not
	//
	// ====================================================================

	private static void add(final Map<Class<?>, AnimaniaBadge1> theMap, final String className, final ItemStack food) {
		add(theMap, className, food, 0F);
	}

	private static void add(final Map<Class<?>, AnimaniaBadge1> theMap, final String className, final ItemStack food,
			final float dY) {
		try {
			final Class<?> clazz = Class.forName(className);
			final AnimaniaBadge1 a = new AnimaniaBadge1(clazz, food, dY);
			if (a.isValid()) {
				theMap.put(clazz, a);
			}
		} catch (final Throwable t) {
			;
		}
	}

	static void intitialize() {
		final Map<Class<?>, AnimaniaBadge1> classMap = new Reference2ObjectOpenHashMap<>();
		add(classMap, "com.animania.common.entities.chickens.EntityAnimaniaChicken", SEEDS);
		add(classMap, "com.animania.common.entities.cows.EntityAnimaniaCow", WHEAT);
		add(classMap, "com.animania.common.entities.goats.EntityAnimaniaGoat", WHEAT);
		add(classMap, "com.animania.common.entities.horses.EntityAnimaniaHorse", APPLE);
		add(classMap, "com.animania.common.entities.peacocks.EntityAnimaniaPeacock", SEEDS);
		add(classMap, "com.animania.common.entities.pigs.EntityAnimaniaPig", CARROT);
		add(classMap, "com.animania.common.entities.sheep.EntityAnimaniaSheep", WHEAT);
		add(classMap, "com.animania.common.entities.rodents.EntityFerretBase", EGG);
		add(classMap, "com.animania.common.entities.rodents.EntityHedgehogBase", CARROT);
		add(classMap, "com.animania.common.entities.rodents.EntityHamster", HAMSTER_FOOD);
		add(classMap, "com.animania.common.entities.rodents.rabbits.EntityAnimaniaRabbit", RABBIT_FOOD);

		// Iterate our entity class hierarchy looking for matches and make sure
		// they are in the map directly - avoid repeated lookups at run time.
		final RenderManager rm = Minecraft.getMinecraft().getRenderManager();
		for (final ResourceLocation r : EntityList.getEntityNameList()) {
			final Class<? extends Entity> clazz = EntityList.getClass(r);
			if (clazz != null) {
				final Optional<Entry<Class<?>, AnimaniaBadge1>> result = classMap.entrySet().stream()
						.filter(e -> e.getKey().isAssignableFrom(clazz)).findFirst();
				if (result.isPresent()) {
					final Render<Entity> renderer = rm.getEntityClassRenderObject(clazz);
					if (renderer instanceof RenderLivingBase) {
						((RenderLivingBase<?>) renderer)
								.addLayer(new BadgeRenderLayer(BADGE_DISPLAY_CHECK, result.get().getValue()));
					}
				}
			}
		}
	}
}
