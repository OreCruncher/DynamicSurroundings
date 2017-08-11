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

import java.util.IdentityHashMap;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.Color;
import elucent.albedo.event.GatherLightsEvent;
import elucent.albedo.lighting.Light;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = DSurround.MOD_ID)
public class LightingEffectHUD extends GuiOverlay {

	private static final class LightSourceData {
		public final int color;
		public final int radius;

		public LightSourceData(final Color color, final int radius, final float alpha) {
			this.color = color.rgbWithAlpha(alpha);
			this.radius = radius;
		}
	}

	private static final LightSourceData TORCH_SOURCE = new LightSourceData(new Color(255, 204, 178), 8, 0.75F);
	private static final LightSourceData REDSTONE_TORCH_SOURCE = new LightSourceData(Color.MC_RED, 6, 0.5F);
	private static final LightSourceData NETHER_STAR_SOURCE = new LightSourceData(Color.MC_WHITE, 12, 1.0F);
	private static final LightSourceData GLOWSTONE_SOURCE = new LightSourceData(Color.MC_YELLOW, 8, 0.75F);
	private static final LightSourceData SEA_LANTERN_SOURCE = new LightSourceData(Color.MC_AQUA, 8, 0.75F);

	private static final IdentityHashMap<Item, LightSourceData> sources = new IdentityHashMap<Item, LightSourceData>();

	static {
		sources.put(Item.getItemFromBlock(Blocks.TORCH), TORCH_SOURCE);
		sources.put(Item.getItemFromBlock(Blocks.REDSTONE_TORCH), REDSTONE_TORCH_SOURCE);
		sources.put(Items.NETHER_STAR, NETHER_STAR_SOURCE);
		sources.put(Item.getItemFromBlock(Blocks.GLOWSTONE), GLOWSTONE_SOURCE);
		sources.put(Item.getItemFromBlock(Blocks.SEA_LANTERN), SEA_LANTERN_SOURCE);
	}

	private static Vec3d getPoint() {
		return EnvironState.getPlayer().getEntityBoundingBox().getCenter();
	}

	private static LightSourceData resolveLightSource(final ItemStack stack) {
		LightSourceData result = null;
		if (stack != null && !stack.isEmpty()) {
			final Item item = stack.getItem();
			if (item != null)
				result = sources.get(item);
		}
		return result;
	}

	private static LightSourceData resolveLightSource() {
		LightSourceData result = resolveLightSource(EnvironState.getPlayer().getHeldItemMainhand());
		if (result == null)
			result = resolveLightSource(EnvironState.getPlayer().getHeldItemOffhand());
		return result;
	}

	private static LightSourceData source = null;

	public void doTick(final int tickRef) {

		source = null;

		if (ModOptions.enableAlbedoSupport && ModOptions.enablePlayerLighting) {
			source = resolveLightSource();
		}
	}

	@Optional.Method(modid = "albedo")
	@SubscribeEvent
	public static void onGatherLight(@Nonnull final GatherLightsEvent event) {
		if (source != null) {
			final Light l = Light.builder().pos(getPoint()).color(source.color, true).radius(source.radius).build();
			event.getLightList().add(l);
		}
	}
}
