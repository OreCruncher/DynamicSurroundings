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

package org.orecruncher.dsurround.client.weather.frost;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilitySeasonInfo;
import org.orecruncher.dsurround.capabilities.season.ISeasonInfo;
import org.orecruncher.dsurround.client.weather.Windows;
import org.orecruncher.dsurround.client.weather.particle.ParticleWeather;
import org.orecruncher.dsurround.event.BlockUpdateEvent;
import org.orecruncher.dsurround.event.DiagnosticEvent;
import org.orecruncher.dsurround.lib.profiling.RenderTimer;
import org.orecruncher.dsurround.profiling.RenderTimers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Frost on the outside of windows when it is cold (see {@link FrostConditions}), as the 26.x line's window frost
 * handler: on glass blocks and panes near the player ({@link FrostField}), stained glass included. Drawn after the
 * translucent blocks, before the rain and snow where the mod draws the weather (see {@link #renderBeforeWeather}),
 * otherwise at the end of the world's drawing.
 * <p>
 * Nothing is looked for where it is too warm for frost near the player. Off without shaders, under an OptiFine shader
 * pack, and after a failure to draw it. Client thread only.
 */
@SideOnly(Side.CLIENT)
public final class WindowFrost {

	/**
	 * Windows are looked for only while it is this much warmer at the player than frost needs, at most: a mountain
	 * or a cold biome nearby may be colder
	 */
	static final float WARM_MARGIN = 0.3F;

	private static final RenderTimer RENDER_TIMER = RenderTimers.add(new RenderTimer("Window Frost"));
	private static final FrostField FIELD = new FrostField();
	private static final FrostRenderer RENDERER = new FrostRenderer();
	private static final ClientWorld FROST_WORLD = new ClientWorld();
	@Nullable
	private static World world;
	private static float forced = Float.NaN;
	private static int drawn;
	// Drawn this frame before the weather, so not again at the end of it
	private static boolean drawnThisFrame;

	private WindowFrost() {
	}

	/**
	 * Whether frost is drawn on windows: turned on, and it can be drawn
	 */
	public static boolean isActive() {
		return ModOptions.rain.windowFrost && RENDERER.isAvailable() && !ParticleWeather.shaderPackInUse();
	}

	@SubscribeEvent
	public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		final Minecraft mc = Minecraft.getMinecraft();
		final World current = mc.world;
		final Entity player = mc.player;
		if (!isActive() || current == null || player == null) {
			clear();
			return;
		}
		if (mc.isGamePaused())
			return;
		// Another dimension: nothing found in the last one applies
		if (current != world) {
			FIELD.clear();
			world = current;
			FROST_WORLD.world = current;
		}

		final BlockPos pos = new BlockPos(player);
		final boolean isForced = !Float.isNaN(forced);
		if (!isForced && FrostConditions.coldness(FROST_WORLD.temperature(pos) - WARM_MARGIN) <= 0F) {
			FIELD.clear();
			return;
		}

		// The sun's angle, as a share of the day; 0 is noon
		final float timeOfDay = current.getCelestialAngle(1F);
		final float clearSky = 1F - Math.min(1F, current.getRainStrength(1F));
		final float held = forced;
		final FrostField.FrostTarget target = isForced ? face -> held
				: face -> FrostConditions.target(face, timeOfDay, clearSky);
		FIELD.update(current, FROST_WORLD, pos.getX(), pos.getY(), pos.getZ(), target);
		// Set by /dsweather: shown as it is set, without growing or melting into it
		if (isForced)
			FIELD.forEachFace(face -> face.show(held));
	}

	/**
	 * Draws the frost before the rain and snow, from the mod's weather renderer: the rain behind a window is then seen
	 * through its frost, not over it
	 */
	public static void renderBeforeWeather() {
		draw();
		drawnThisFrame = true;
	}

	/**
	 * Draws the frost at the end of the world's drawing, if the mod's weather renderer didn't (another mod's weather
	 * renderer for the dimension, or the weather hook off)
	 */
	@SubscribeEvent
	public static void onRenderWorldLast(@Nonnull final RenderWorldLastEvent event) {
		if (!drawnThisFrame)
			draw();
		drawnThisFrame = false;
	}

	private static void draw() {
		drawn = 0;
		if (!isActive() || world == null || FIELD.faceCount() == 0)
			return;
		// Made the first time, on a thread of its own: no frost until it is ready
		final int texture = FrostTexture.texture();
		if (texture < 0)
			return;
		final Minecraft mc = Minecraft.getMinecraft();
		final RenderManager manager = mc.getRenderManager();
		RENDER_TIMER.begin();
		try {
			mc.entityRenderer.enableLightmap();
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			GlStateManager.bindTexture(texture);
			GlStateManager.disableCull();
			GlStateManager.enableDepth();
			GlStateManager.depthMask(false);
			GlStateManager.disableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1F, 1F, 1F, 1F);

			drawn = RENDERER.draw(FIELD, manager.viewerPosX, manager.viewerPosY, manager.viewerPosZ);

			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.depthMask(true);
			GlStateManager.enableCull();
			mc.entityRenderer.disableLightmap();
		} finally {
			RENDER_TIMER.end();
		}
	}

	/**
	 * Gives every window this much frost, 0 to 1, whatever the weather, until {@link #clearForced}; for /dsweather
	 */
	public static void setForced(final float frost) {
		forced = frost;
	}

	public static void clearForced() {
		forced = Float.NaN;
	}

	/**
	 * The frost set by /dsweather, or NaN if none is
	 */
	public static float forced() {
		return forced;
	}

	private static void clear() {
		FIELD.clear();
		world = null;
		FROST_WORLD.world = null;
	}

	/**
	 * Disconnected: nothing kept, and nothing held by /dsweather
	 */
	public static void disconnect() {
		clear();
		clearForced();
	}

	@SubscribeEvent
	public static void onWorldUnload(@Nonnull final WorldEvent.Unload event) {
		if (event.getWorld() == world)
			clear();
	}

	@SubscribeEvent
	public static void onBlockUpdate(@Nonnull final BlockUpdateEvent event) {
		if (event.world == world)
			FIELD.blockChanged(event.pos.getX(), event.pos.getY(), event.pos.getZ());
	}

	@SubscribeEvent
	public static void onChunkLoad(@Nonnull final ChunkEvent.Load event) {
		if (event.getWorld() == world)
			FIELD.chunkChanged(event.getChunk().x, event.getChunk().z);
	}

	@SubscribeEvent
	public static void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		final String text;
		if (!ModOptions.rain.windowFrost)
			text = "Window frost: off";
		else if (!isActive())
			text = "Window frost: off (no shaders, failed, or a shader pack in use)";
		else
			text = String.format(Locale.ROOT, "Window frost: %d faces in %d sections, %d drawn%s", FIELD.faceCount(),
					FIELD.sectionCount(), drawn, Float.isNaN(forced) ? "" : " (set by /dsweather)");
		event.output.add(TextFormatting.AQUA + text);
	}

	/**
	 * The client's world, as frost sees it
	 */
	private static final class ClientWorld implements IFrostWorld {
		World world;

		@Override
		@Nonnull
		public IBlockAccess blocks() {
			return this.world;
		}

		@Override
		public boolean frosts(@Nonnull final IBlockState state) {
			return Windows.isWindow(state);
		}

		@Override
		public boolean seesSky(@Nonnull final BlockPos pos) {
			// The precipitation height: glass blocks movement, so a greenhouse roof is a roof
			return pos.getY() >= this.world.getPrecipitationHeight(pos).getY();
		}

		@Override
		public int skyLight(@Nonnull final BlockPos pos) {
			return this.world.getLightFor(EnumSkyBlock.SKY, pos);
		}

		@Override
		public int packedLight(@Nonnull final BlockPos pos) {
			return this.world.getCombinedLight(pos, 0);
		}

		@Override
		public float temperature(@Nonnull final BlockPos pos) {
			final ISeasonInfo season = CapabilitySeasonInfo.getCapability(this.world);
			return season == null ? this.world.getBiome(pos).getTemperature(pos) : season.getTemperature(pos);
		}
	}
}
