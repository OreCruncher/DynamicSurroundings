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

package org.orecruncher.dsurround.client.weather.particle;

import org.orecruncher.dsurround.client.weather.streaks.WindowRain;
import org.orecruncher.dsurround.client.weather.wind.WindService;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.event.DiagnosticEvent;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Particle weather: rain and snow drawn as single drops and flakes around the player, in place of vanilla's sheets,
 * ported from the 26.x line ({@link PrecipitationField} and {@link DropRenderer}), blown by the scene's wind
 * ({@link WindService}). Where snow lands it leaves flecks that melt away, on the ground and, blown by the wind,
 * on the sides of walls and trees ({@link Landings}, {@link SnowFlecks}); rain blown against a window runs down it
 * ({@link WindowRain}). The splashes are still the storm splash renderer's. Dust is still drawn as sheets. Off under an OptiFine shader pack, which draws the weather its own way.
 * <p>
 * Run from the weather renderer, so it only runs where the mod draws the weather: the drops are ticked by the first
 * frame of each client tick (the renderer's tick count), then drawn every frame: uploaded once a tick for the GPU to
 * make their quads ({@link GpuDrops}), or, without shaders, built on the CPU each frame ({@link DropRenderer}). The
 * render thread only.
 */
@SideOnly(Side.CLIENT)
public final class ParticleWeather {

	/**
	 * Drops at full strength, at the field's range; the 26.x line's default
	 */
	static final int MAX_DROPS = 6000;
	/**
	 * Snow flecks at once, the 26.x line's default. Snow lands far more slowly than rain, and the flecks melt in a few
	 * seconds.
	 */
	static final int MAX_SNOW_FLECKS = 1024;
	// Where a rain drop blown in meets a window: it runs down the glass
	private static final Landings.WindowHit WINDOWS = WindowRain::blownIntoWindow;

	private static final ParticleWeather INSTANCE = new ParticleWeather();

	private final PrecipitationField field = new PrecipitationField(MAX_DROPS);
	private final WorldColumns columns = new WorldColumns();
	private final Snowflakes snowflakes = new Snowflakes();
	private final SnowFlecks flecks = new SnowFlecks(MAX_SNOW_FLECKS);
	private final Landings landings = new Landings();
	private final DropRenderer.Quads fleckQuads = new DropRenderer.Quads();
	private final DropRenderer.Quads rain = new DropRenderer.Quads();
	private final DropRenderer.Quads snow = new DropRenderer.Quads();
	private final GpuDrops gpu = new GpuDrops();
	// The drops changed since they were last uploaded for the GPU
	private boolean uploadDue;
	private final Random random = new XorShiftRandom();
	private World world;
	private int lastTick = Integer.MIN_VALUE;

	private ParticleWeather() {
	}

	/**
	 * Whether particle weather draws now, in place of the sheets of rain and snow: turned on, and no shader pack
	 */
	public static boolean isActive() {
		return ModOptions.rain.particleWeather && !shaderPackInUse();
	}

	/**
	 * Resources have loaded again: the snowflakes are looked up afresh
	 */
	public static void reload() {
		INSTANCE.snowflakes.reload();
	}

	/**
	 * Ticks the drops if a client tick has gone by since the last frame, and draws them
	 *
	 * @param tick      the renderer's tick count, which goes up once a client tick
	 * @param rainLevel how strong the storm is, 0 to 1
	 */
	public static void render(@Nonnull final EntityRenderer renderer, @Nonnull final World world,
			@Nonnull final Entity camera, final int tick, final float rainLevel, final float partialTicks) {
		INSTANCE.tickIfDue(world, camera, tick, rainLevel);
		INSTANCE.draw(renderer, partialTicks);
	}

	private void tickIfDue(@Nonnull final World world, @Nonnull final Entity camera, final int tick,
			final float rainLevel) {
		if (world != this.world) {
			this.world = world;
			this.columns.setWorld(world);
			this.field.clear();
			this.flecks.clear();
			this.lastTick = Integer.MIN_VALUE;
		}
		if (tick == this.lastTick)
			return;
		// They melt whether or not anything falls
		this.flecks.tick();
		// Ticks missed (the game paused, or the weather not drawn): the columns found then may be out of date
		if (tick != this.lastTick + 1)
			this.field.idle();
		this.lastTick = tick;
		if (this.field.isIdle(rainLevel)) {
			this.field.idle();
			return;
		}
		this.field.tick(this.columns, camera.posX, camera.posY + camera.getEyeHeight(), camera.posZ, rainLevel,
				density(Minecraft.getMinecraft().gameSettings.particleSetting), WindService.wind(), this.random);
		this.uploadDue = true;
		this.landings.land(world, this.field, this.flecks, Minecraft.getMinecraft().gameSettings.particleSetting,
				ModOptions.rain.snowOnSides, WindowRain.isActive() ? WINDOWS : null, this.random);
	}

	/**
	 * Lets go of a world as it unloads, with its drops
	 */
	@SubscribeEvent
	public static void onWorldUnload(@Nonnull final WorldEvent.Unload event) {
		if (event.getWorld() == INSTANCE.world) {
			INSTANCE.world = null;
			INSTANCE.columns.setWorld(null);
			INSTANCE.field.clear();
			INSTANCE.flecks.clear();
		}
	}

	@SubscribeEvent
	public static void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		if (!isActive() || INSTANCE.world == null)
			return;
		event.output.add(TextFormatting.AQUA + String.format(Locale.ROOT, "Particle weather: %d drops, %d snow flecks",
				INSTANCE.field.count(), INSTANCE.flecks.count()));
	}

	/**
	 * The share of the drops a storm calls for that there are: all of them, half or a quarter as the Particles option
	 * is All, Decreased or Minimal
	 */
	static float density(final int particleSetting) {
		return particleSetting <= 0 ? 1F : particleSetting == 1 ? 0.5F : 0.25F;
	}

	private void draw(@Nonnull final EntityRenderer renderer, final float partialTicks) {
		if (this.field.count() == 0 && this.flecks.count() == 0)
			return;
		final Minecraft mc = Minecraft.getMinecraft();
		final RenderManager manager = mc.getRenderManager();
		final DropRenderer.View view = new DropRenderer.View(manager.viewerPosX, manager.viewerPosY,
				// As vanilla turns particles: left is (X, 0, Z), up is (YZ, XZ, XY)
				manager.viewerPosZ, ActiveRenderInfo.getRotationX(), 0F, ActiveRenderInfo.getRotationZ(),
				ActiveRenderInfo.getRotationYZ(), ActiveRenderInfo.getRotationXZ(), ActiveRenderInfo.getRotationXY(),
				mc.gameSettings.thirdPersonView != 0 ? 4F : 0F);
		final List<Snowflakes.Flake> flakes = this.snowflakes.flakes(mc.getResourceManager());
		final boolean onGpu = ModOptions.rain.particleWeatherOnGpu && this.gpu.isAvailable();
		DropRenderer.build(this.flecks, this.fleckQuads, view, partialTicks, flakes, this.snowflakes.largest());
		if (onGpu) {
			// Uploaded once a tick; the shader makes each frame's quads
			if (this.uploadDue) {
				this.uploadDue = false;
				this.gpu.upload(this.field, view.x, view.y, view.z, flakes, this.snowflakes.largest());
			}
		} else {
			this.uploadDue = true;
			DropRenderer.build(this.field, this.rain, this.snow, view, partialTicks, flakes,
					this.snowflakes.largest(), RainColor.USUAL);
			if (this.rain.count() == 0 && this.snow.count() == 0 && this.fleckQuads.count() == 0)
				return;
		}

		renderer.enableLightmap();
		GlStateManager.disableCull();
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		// The streaks fade to nothing toward their tails: only what is fully clear is left out
		GlStateManager.alphaFunc(516, 0.003921569F);
		GlStateManager.depthMask(false);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		if (onGpu) {
			// Rain and snow in one draw: the shader leaves the rain untextured
			mc.getTextureManager().bindTexture(Snowflakes.TEXTURE);
			if (this.field.count() > 0)
				this.gpu.draw(view.x, view.y, view.z, partialTicks, view, this.field.radius(), RainColor.USUAL);
		} else {
			GlStateManager.disableTexture2D();
			this.rain.draw();
			GlStateManager.enableTexture2D();
			if (this.snow.count() > 0) {
				mc.getTextureManager().bindTexture(Snowflakes.TEXTURE);
				this.snow.draw();
			}
		}
		// The flecks, cut from the snow texture as the falling flakes are (plain without one)
		if (this.fleckQuads.count() > 0) {
			if (flakes.isEmpty()) {
				GlStateManager.disableTexture2D();
				this.fleckQuads.draw();
				GlStateManager.enableTexture2D();
			} else {
				mc.getTextureManager().bindTexture(Snowflakes.TEXTURE);
				this.fleckQuads.draw();
			}
		}

		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(516, 0.1F);
		renderer.disableLightmap();
	}

	// OptiFine's shaders, looked up once: their Shaders class's shaderPackLoaded, or none
	private static Field shaderPackLoaded;
	private static boolean lookedForShaders;
	// Set if reading it failed: taken as a shader pack in use from then on
	private static boolean shadersUnknown;

	/**
	 * Whether an OptiFine shader pack is in use
	 */
	public static boolean shaderPackInUse() {
		if (!lookedForShaders) {
			lookedForShaders = true;
			shaderPackLoaded = shadersField("net.optifine.shaders.Shaders");
			if (shaderPackLoaded == null)
				shaderPackLoaded = shadersField("shadersmod.client.Shaders");
		}
		if (shaderPackLoaded == null)
			return shadersUnknown;
		try {
			return shaderPackLoaded.getBoolean(null);
		} catch (final Exception e) {
			ModBase.log().error("Unable to tell whether a shader pack is in use; particle weather is off", e);
			shadersUnknown = true;
			shaderPackLoaded = null;
			return true;
		}
	}

	@Nullable
	private static Field shadersField(@Nonnull final String className) {
		try {
			final Field field = Class.forName(className).getDeclaredField("shaderPackLoaded");
			field.setAccessible(true);
			return field;
		} catch (final Throwable t) {
			return null;
		}
	}
}
