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

package org.blockartistry.DynSurround.client.renderer;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.ForgeUtils;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
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
public class AnimaniaBadge implements LayerRenderer<EntityLivingBase> {

	protected final static ItemStack WATER_BUCKET = new ItemStack(Items.WATER_BUCKET);
	protected final static ItemStack WATER = new ItemStack(Items.WATER_BUCKET);
	protected final static ItemStack SEEDS = new ItemStack(Items.WHEAT_SEEDS);
	protected final static ItemStack WHEAT = new ItemStack(Items.WHEAT);
	protected final static ItemStack APPLE = new ItemStack(Items.APPLE);
	protected final static ItemStack CARROT = new ItemStack(Items.CARROT);
	protected final static ItemStack EGG = new ItemStack(Items.EGG);
	protected final static ItemStack RABBIT_FOOD = CARROT;

	protected final static ItemStack HAMSTER_FOOD;

	static {
		final Item item = ForgeUtils.getItem("animania:hamster_food");
		HAMSTER_FOOD = item != null ? new ItemStack(item) : ItemStack.EMPTY;
	}

	private static class Accessor {

		protected Method food;
		protected Method water;
		protected ItemStack foodItem;

		public Accessor(final Class<?> clazz, final ItemStack food) {
			try {
				this.food = clazz.getMethod("getFed");
				this.water = clazz.getMethod("getWatered");
				this.foodItem = food;
			} catch (final Throwable t) {
				this.food = null;
				this.water = null;
			}
		}

		public ItemStack getBadgeToDisplay(final Entity e) {
			if (!getWatered(e))
				return WATER_BUCKET;
			if (!getFed(e))
				return this.foodItem;
			return ItemStack.EMPTY;
		}

		public boolean getFed(final Entity e) {
			try {
				return (boolean) this.food.invoke(e);
			} catch (final Throwable t) {
				return true;
			}
		}

		public boolean getWatered(final Entity e) {
			try {
				return (boolean) this.water.invoke(e);
			} catch (final Throwable t) {
				return true;
			}
		}

		public boolean isValid() {
			return this.food != null && this.water != null;
		}
	}

	protected final Accessor accessor;

	public AnimaniaBadge(final Accessor a) {
		this.accessor = a;
	}

	// Thank you Darkhax!
	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		final ItemStack stackToRender = this.accessor.getBadgeToDisplay(entity);
		if (stackToRender.isEmpty())
			return;

		final float age = entity.ticksExisted + partialTicks;
		final double height = entity.height - 0.15 + (MathStuff.sin(age / 20D)) / 3D;
		final float s = scale * 10F;

		GlStateManager.pushMatrix();
		GlStateManager.rotate(180, 0, 0, 1);
		// GlStateManager.scale(0.6, 0.6, 0.6);
		GlStateManager.scale(s, s, s);
		GlStateManager.rotate((age) / 20.0F * (180F / MathStuff.PI_F), 0F, 1F, 0F);
		GlStateManager.translate(0, height, 0);
		Minecraft.getMinecraft().getRenderItem().renderItem(stackToRender, ItemCameraTransforms.TransformType.FIXED);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

	// ====================================================================
	//
	// Initialization for the entity models and what not
	//
	// ====================================================================

	private static void add(final Map<Class<?>, Accessor> theMap, final String className, final ItemStack food) {
		try {
			final Class<?> clazz = Class.forName(className);
			final Accessor a = new Accessor(clazz, food);
			if (a.isValid())
				theMap.put(clazz, a);
		} catch (final Throwable t) {
			;
		}
	}

	public static void intitialize() {
		if (!ModOptions.speechbubbles.enableAnimaniaBadges)
			return;

		final Map<Class<?>, Accessor> classMap = new IdentityHashMap<>();
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
		for (final ResourceLocation r : EntityList.getEntityNameList()) {
			final Class<? extends Entity> clazz = EntityList.getClass(r);
			if (clazz != null) {
				final Iterator<Entry<Class<?>, Accessor>> itr = classMap.entrySet().iterator();
				while (itr.hasNext()) {
					final Entry<Class<?>, Accessor> e = itr.next();
					if (e.getKey().isAssignableFrom(clazz)) {
						final Render<Entity> renderer = Minecraft.getMinecraft().getRenderManager()
								.getEntityClassRenderObject(clazz);
						if (renderer instanceof RenderLivingBase) {
							((RenderLivingBase<?>) renderer).addLayer(new AnimaniaBadge(e.getValue()));
						}
						break;
					}
				}
			}
		}
	}
}
