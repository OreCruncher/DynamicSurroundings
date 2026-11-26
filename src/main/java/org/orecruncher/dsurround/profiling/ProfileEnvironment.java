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

package org.orecruncher.dsurround.profiling;

import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;
import org.orecruncher.dsurround.ModInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * What a profile was recorded on: the mod and Forge, the graphics card as OpenGL names it, the video settings that
 * change what a frame costs, Java and the OS, and the installed mods. Captured on the render thread as /dsprofile
 * starts (OpenGL can only be asked there), written into the recording ({@link EnvironmentEvent}, {@link ModEvent}).
 * Nothing private: no paths, names or addresses. Uses nothing from {@code jdk.jfr}.
 */
@SideOnly(Side.CLIENT)
final class ProfileEnvironment {

	/**
	 * The system property a development run names itself in
	 */
	static final String DEV_RUN_PROPERTY = "dsurround.devRun";

	final String mod;
	final int mods;
	final String gpu;
	final String gpuVendor;
	final String gpuType;
	final String driver;
	final String backend;
	final String graphics;
	final boolean shaderPack;
	final int renderDistance;
	final int width;
	final int height;
	final int frameRateLimit;
	final boolean vsync;
	final String devRun;
	final String java;
	final String os;
	final List<ProfileMod> modList;

	private ProfileEnvironment(@Nonnull final Minecraft mc) {
		final GameSettings options = mc.gameSettings;
		this.mod = String.format("%s %s (Forge %s)", ModInfo.MOD_NAME, ModInfo.VERSION, ForgeVersion.getVersion());
		this.mods = Loader.instance().getActiveModList().size();
		this.gpu = ProfileMod.clean(GL11.glGetString(GL11.GL_RENDERER));
		this.gpuVendor = ProfileMod.clean(GL11.glGetString(GL11.GL_VENDOR));
		// OpenGL doesn't say whether a card is integrated or discrete
		this.gpuType = "unknown";
		this.driver = ProfileMod.clean(GL11.glGetString(GL11.GL_VERSION));
		this.backend = "OpenGL";
		this.graphics = String.format("%s, smooth lighting %s, particles %s", options.fancyGraphics ? "fancy" : "fast",
				options.ambientOcclusion == 0 ? "off" : options.ambientOcclusion == 1 ? "min" : "max",
				options.particleSetting == 0 ? "all" : options.particleSetting == 1 ? "decreased" : "minimal");
		this.shaderPack = isShaderPackInUse();
		this.renderDistance = options.renderDistanceChunks;
		this.width = mc.displayWidth;
		this.height = mc.displayHeight;
		this.frameRateLimit = options.limitFramerate;
		this.vsync = options.enableVsync;
		final String run = System.getProperty(DEV_RUN_PROPERTY);
		this.devRun = run == null ? "" : run;
		this.java = String.format("%s (%s)", System.getProperty("java.version"), System.getProperty("java.vendor"));
		this.os = String.format("%s %s %s", System.getProperty("os.name"), System.getProperty("os.version"),
				System.getProperty("os.arch"));
		this.modList = ProfileMod.capture();
	}

	/**
	 * What the game runs on now; the render thread only
	 */
	@Nonnull
	static ProfileEnvironment capture() {
		return new ProfileEnvironment(Minecraft.getMinecraft());
	}

	// Whether OptiFine has a shader pack loaded (its shaders draw most of the frame then)
	private static boolean isShaderPackInUse() {
		try {
			final Class<?> shaders = Class.forName("net.optifine.shaders.Shaders");
			return shaders.getField("shaderPackLoaded").getBoolean(null);
		} catch (final Throwable t) {
			return false;
		}
	}
}
