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

package org.orecruncher.dsurround.client.footsteps.implem;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A ILibrary that can also play sounds and default footsteps.
 */
@SideOnly(Side.CLIENT)
public class AcousticsManager {

	private final Map<String, IAcoustic> acoustics = new Object2ObjectOpenHashMap<>();

	// Special sentinels for equating
	public static final IAcoustic[] EMPTY = {};
	public static final IAcoustic[] NOT_EMITTER = { new BasicAcoustic("NOT_EMITTER") };
	public static final IAcoustic[] MESSY_GROUND = { new BasicAcoustic("MESSY_GROUND") };
	public static IAcoustic[] SWIM;
	public static IAcoustic[] JUMP;
	public static IAcoustic[] SPLASH;

	public AcousticsManager() {
	}

	public void addAcoustic(@Nonnull final IAcoustic acoustic) {
		this.acoustics.put(acoustic.getAcousticName(), acoustic);
	}

	@Nullable
	public IAcoustic getAcoustic(@Nonnull final String name) {
		return this.acoustics.get(name);
	}

	@Nonnull
	public IAcoustic[] compileAcoustics(@Nonnull final String acousticName) {
		if (acousticName.equals("NOT_EMITTER"))
			return NOT_EMITTER;
		else if (acousticName.equals("MESSY_GROUND"))
			return MESSY_GROUND;

		final IAcoustic[] result = Arrays.stream(acousticName.split(",")).map(fragment -> {
			final IAcoustic a = this.acoustics.get(fragment);
			if (a == null)
				ModBase.log().warn("Acoustic '%s' not found!", fragment);
			return a;
		}).filter(a -> {
			return a != null;
		}).toArray(IAcoustic[]::new);

		return result.length == 0 ? EMPTY : result;
	}

}