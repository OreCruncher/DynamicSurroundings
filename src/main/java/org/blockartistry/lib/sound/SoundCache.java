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

package org.blockartistry.lib.sound;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.lib.LibLog;

import com.google.common.io.ByteStreams;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SoundCache {

	private static final int BUFFER_SIZE = 64 * 1024;
	private static final byte[] BUFFER = new byte[BUFFER_SIZE];
	private static final IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
	private static final Map<ResourceLocation, URL> cache = new HashMap<ResourceLocation, URL>(256);

	private static final ResourceLocation SILENCE_RESOURCE = new ResourceLocation(DSurround.RESOURCE_ID, "sounds/ambient/silence.ogg"); 
	private static final byte[] SILENCE = getBuffer(SILENCE_RESOURCE);
	private static URL SILENCE_URL;
	
	static {
		try {
			final MemoryStreamHandler handler = new MemoryStreamHandler(SILENCE_RESOURCE, SILENCE);
			SILENCE_URL = new URL((URL) null, handler.getSpec(), handler);
		} catch (Exception e) {
			e.printStackTrace();
			SILENCE_URL = null;
		}
	}

	private static byte[] getBuffer(@Nonnull final ResourceLocation resource) {
		try (final InputStream stream = manager.getResource(resource).getInputStream()) {
			// It's possible that available() returns 0. This generally means
			// the stream has no idea about the number of bytes. If it reports
			// 64K or greater assume it needs to be streamed from the JAR.
			if (stream == null) {
				LibLog.log().warn("No stream returned for [%s]", resource.toString());
				return SILENCE;
			} else if (stream.available() < BUFFER_SIZE) {
				final int bytesRead = ByteStreams.read(stream, BUFFER, 0, BUFFER_SIZE);
				// If no bytes were returned, or the total read was 64K, assume
				// that it needs to be streamed.
				if (bytesRead == 0 || bytesRead == BUFFER_SIZE)
					return null;
				// Make a new array containing the data. Don't want to
				// pass back BUFFER.
				return Arrays.copyOf(BUFFER, bytesRead);
			}
		} catch (@Nonnull final Throwable t) {
			LibLog.log().warn("Error reading stream [%s]", resource.toString());
			return SILENCE;
		}

		return null;
	}

	private static URL load(@Nonnull final ResourceLocation key) throws Exception {

		final byte[] buffer = getBuffer(key);
		final SoundStreamHandler handler;

		if (buffer == null) {
			handler = new SoundStreamHandler(key);
		} else {
			handler = new MemoryStreamHandler(key, buffer);
		}

		return new URL((URL) null, handler.getSpec(), handler);
	}

	private SoundCache() {

	}

	public static URL getURLForSoundResource(@Nonnull final ResourceLocation soundResource) {
		URL result = cache.get(soundResource);
		if (result == null)
			try {
				cache.put(soundResource, result = load(soundResource));
			} catch (@Nonnull final Throwable t) {
				LibLog.log().warn("Unable to load sound resource for [%s]", soundResource.toString());
				result = SILENCE_URL;
			}
		return result;
	}

}
