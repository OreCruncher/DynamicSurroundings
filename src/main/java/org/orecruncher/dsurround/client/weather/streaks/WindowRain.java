/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.client.weather.streaks;

import java.util.Locale;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilityDimensionInfo;
import org.orecruncher.dsurround.capabilities.dimension.IDimensionInfo;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.client.weather.particle.RainColor;
import org.orecruncher.dsurround.client.weather.particle.WorldColumns;
import org.orecruncher.dsurround.client.weather.wind.WindService;
import org.orecruncher.dsurround.event.DiagnosticEvent;
import org.orecruncher.dsurround.lib.profiling.RenderTimer;
import org.orecruncher.dsurround.profiling.RenderTimers;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Rain running down windows, as the 26.x line's precipitation handler runs its window streaks: started on the windward
 * sides of windows near the camera ({@link RainOnWindows}), run down the glass ({@link WindowStreaks}), dripping off
 * the bottom edge over open air as vanilla's dripping water. Whether the rain is drawn as sheets or as particle
 * weather. Drawn untextured in the rain's color, lit by the lightmap, with no shader, after the window frost and before
 * the rain where the mod draws the weather, otherwise at the end of the world's drawing. Client thread only.
 */
@SideOnly(Side.CLIENT)
public final class WindowRain {

	// Drops on windows at once, at most
	static final int MAX_WINDOW_STREAKS = 384;
	// Where a drip hangs under the edge it ran off
	static final double DRIP_BELOW_EDGE = 0.05D;
	// The bead's half size, smallest and how much larger a large drop's is; how solid it is; and its trail's width, as
	// a share of the bead's, and how solid that is, fading out toward its top
	static final float STREAK_BEAD_MIN_HALF_SIZE = 0.018F;
	static final float STREAK_BEAD_HALF_SIZE_RANGE = 0.017F;
	static final float STREAK_BEAD_ALPHA = 0.6F;
	static final float STREAK_TRAIL_WIDTH = 0.45F;
	static final float STREAK_TRAIL_ALPHA = 0.5F;
	// Drawn this far around the camera at most
	static final float DRAW_RANGE = 48F;

	private static final RenderTimer RENDER_TIMER = RenderTimers.add(new RenderTimer("Window Streaks"));
	private static final WindowStreaks STREAKS = new WindowStreaks(MAX_WINDOW_STREAKS);
	private static final RainOnWindows SEEDER = new RainOnWindows();
	private static final WorldColumns COLUMNS = new WorldColumns();
	private static final Random RANDOM = new XorShiftRandom();
	@Nullable
	private static World world;
	// Drawn this frame before the weather, so not again at the end of it
	private static boolean drawnThisFrame;

	private WindowRain() {
	}

	public static boolean isActive() {
		return ModOptions.rain.windowStreaks;
	}

	@SubscribeEvent
	public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		final Minecraft mc = Minecraft.getMinecraft();
		final World current = mc.world;
		final Entity camera = mc.getRenderViewEntity();
		if (!isActive() || current == null || camera == null) {
			clear();
			return;
		}
		if (mc.isGamePaused())
			return;
		if (current != world) {
			STREAKS.clear();
			world = current;
			COLUMNS.setWorld(current);
		}
		if (STREAKS.count() > 0)
			STREAKS.tick(RANDOM, (x, y, z) -> current.spawnParticle(EnumParticleTypes.DRIP_WATER, x,
					y - DRIP_BELOW_EDGE, z, 0D, 0D, 0D));
		final IDimensionInfo info = CapabilityDimensionInfo.getCapability(current);
		if (info != null && !info.hasWeather())
			return;
		final float rainLevel = MathHelper.clamp(Weather.getIntensityLevel(), 0F, 1F);
		SEEDER.seedWindows(current, COLUMNS, camera.posX, camera.posY + camera.getEyeHeight(), camera.posZ, rainLevel,
				WindService.wind(), STREAKS, RANDOM);
	}

	/**
	 * Draws the streaks before the rain, from the mod's weather renderer, after the window frost
	 */
	public static void renderBeforeWeather(final float partialTicks) {
		draw(partialTicks);
		drawnThisFrame = true;
	}

	/**
	 * Draws the streaks at the end of the world's drawing, if the mod's weather renderer didn't
	 */
	@SubscribeEvent
	public static void onRenderWorldLast(@Nonnull final RenderWorldLastEvent event) {
		if (!drawnThisFrame)
			draw(event.getPartialTicks());
		drawnThisFrame = false;
	}

	private static void draw(final float partialTicks) {
		if (!isActive() || world == null || STREAKS.count() == 0)
			return;
		final Minecraft mc = Minecraft.getMinecraft();
		final RenderManager manager = mc.getRenderManager();
		RENDER_TIMER.begin();
		try {
			mc.entityRenderer.enableLightmap();
			GlStateManager.disableTexture2D();
			GlStateManager.disableCull();
			GlStateManager.enableDepth();
			GlStateManager.depthMask(false);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			// The trails fade to nothing toward their tops: only what is fully clear is left out
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
			GlStateManager.color(1F, 1F, 1F, 1F);

			final Tessellator tessellator = Tessellator.getInstance();
			final BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			build(STREAKS, buffer, manager.viewerPosX, manager.viewerPosY, manager.viewerPosZ, partialTicks,
					RainColor.USUAL);
			tessellator.draw();

			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
			GlStateManager.enableCull();
			GlStateManager.enableTexture2D();
			mc.entityRenderer.disableLightmap();
		} finally {
			RENDER_TIMER.end();
		}
	}

	/**
	 * Writes the drops running down windows as particle quads around the camera: a bead, and above it the wet trail it
	 * has left, fading toward its top, untextured in the rain's color
	 */
	static void build(@Nonnull final WindowStreaks streaks, @Nonnull final BufferBuilder buffer, final double camX,
			final double camY, final double camZ, final float partialTick, @Nonnull final RainColor rainColor) {
		final float rangeSq = DRAW_RANGE * DRAW_RANGE;
		for (int i = 0; i < streaks.count; i++) {
			final float fade = streaks.fade(i, partialTick);
			if (fade <= 0F)
				continue;
			final double x = streaks.x[i] - camX;
			final double y = streaks.prevY[i] + (streaks.y[i] - streaks.prevY[i]) * partialTick - camY;
			final double z = streaks.z[i] - camZ;
			if (x * x + y * y + z * z > rangeSq)
				continue;
			final float trailTop = (float) (streaks.prevTrailTop[i]
					+ (streaks.trailTop[i] - streaks.prevTrailTop[i]) * partialTick - camY);
			final float half = STREAK_BEAD_MIN_HALF_SIZE + STREAK_BEAD_HALF_SIZE_RANGE * streaks.size[i];
			final byte face = streaks.face[i];
			final int sky = streaks.light[i] >> 16 & 0xFFFF, block = streaks.light[i] & 0xFFFF;
			final float a = STREAK_BEAD_ALPHA * fade;

			// The bead
			corner(buffer, face, x, y, z, -half, -half, rainColor, a, sky, block);
			corner(buffer, face, x, y, z, half, -half, rainColor, a, sky, block);
			corner(buffer, face, x, y, z, half, half, rainColor, a, sky, block);
			corner(buffer, face, x, y, z, -half, half, rainColor, a, sky, block);

			// The trail above it, narrower and fainter, fading out toward its top
			final float trail = (float) (trailTop - y) - half;
			if (trail > 0F) {
				final float width = half * STREAK_TRAIL_WIDTH;
				final float trailAlpha = a * STREAK_TRAIL_ALPHA;
				corner(buffer, face, x, y, z, -width, half, rainColor, trailAlpha, sky, block);
				corner(buffer, face, x, y, z, width, half, rainColor, trailAlpha, sky, block);
				corner(buffer, face, x, y, z, width, half + trail, rainColor, 0F, sky, block);
				corner(buffer, face, x, y, z, -width, half + trail, rainColor, 0F, sky, block);
			}
		}
	}

	/**
	 * Writes a corner of something on the side of a window, offset from its center by (u, v) in the plane of the
	 * glass: z and y on a west or east side, x and y on a north or south side
	 */
	private static void corner(@Nonnull final BufferBuilder buffer, final byte face, final double x, final double y,
			final double z, final float u, final float v, @Nonnull final RainColor color, final float alpha,
			final int sky, final int block) {
		final boolean alongZ = face == WindowStreaks.FACE_WEST || face == WindowStreaks.FACE_EAST;
		buffer.pos(alongZ ? x : x + u, y + v, alongZ ? z + u : z).tex(0D, 0D)
				.color(color.red, color.green, color.blue, alpha).lightmap(sky, block).endVertex();
	}

	private static void clear() {
		STREAKS.clear();
		world = null;
		COLUMNS.setWorld(null);
	}

	@SubscribeEvent
	public static void onWorldUnload(@Nonnull final WorldEvent.Unload event) {
		if (event.getWorld() == world)
			clear();
	}

	@SubscribeEvent
	public static void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		if (isActive() && world != null)
			event.output.add(TextFormatting.AQUA
					+ String.format(Locale.ROOT, "Window streaks: %d", STREAKS.count()));
	}
}
