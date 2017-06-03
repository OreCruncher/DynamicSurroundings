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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.io.ByteStreams;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.actors.threadpool.Arrays;

@SideOnly(Side.CLIENT)
public class SoundCache {

	private static final int BUFFER_SIZE = 64 * 1024;
	private static final Map<ResourceLocation, URL> handlers = new HashMap<ResourceLocation, URL>();

	private SoundCache() {

	}

	protected static byte[] getBuffer(@Nonnull final ResourceLocation resource) {


		try {
			try (final InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(resource)
					.getInputStream()) {
				if (stream != null && stream.available() < BUFFER_SIZE) {
					final byte[] buffer = new byte[BUFFER_SIZE];
					final int bytesRead = ByteStreams.read(stream, buffer, 0, BUFFER_SIZE);
					if (bytesRead == 0 || bytesRead == BUFFER_SIZE)
						return null;
					return Arrays.copyOf(buffer, bytesRead);
				}
			}

		} catch (@Nonnull final Throwable t) {
			return null;
		}

		return null;
	}

	public static URL getURLForSoundResource(@Nonnull final ResourceLocation soundResource) {
		URL url = handlers.get(soundResource);

		if (url == null) {

			final byte[] buffer = getBuffer(soundResource);
			final SoundStreamHandler handler;

			if (buffer == null)
				handler = new SoundStreamHandler(soundResource);
			else
				handler = new MemoryStreamHandler(soundResource, buffer);

			try {
				url = new URL((URL) null, handler.getSpec(), handler);
				handlers.put(soundResource, url);
			} catch (@Nonnull final MalformedURLException t) {
				throw new Error("TODO: Sanely handle url exception! :D");
			}

		}
		return url;
	}

}
