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

package org.blockartistry.mod.DynSurround.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;

public final class Localization {

	private static Local impl;

	private abstract static class Local {
		public abstract String format(final String translateKey, final Object... parameters);
	}

	private static class ClientImpl extends Local {
		public ClientImpl() {
		}

		public String format(final String translateKey, final Object... parameters) {
			// Let I18n do the heavy lifting
			return I18n.format(translateKey, parameters);
		}
	}

	// Manually loads the en_US language file. Not looking for translations -
	// just want to reuse the strings in the language file.
	private static class ServerImpl extends Local {

		private final Map<String, String> lookup = new HashMap<String, String>();

		public ServerImpl() {
			InputStream stream = null;

			try {
				stream = ServerImpl.class.getResourceAsStream("/assets/dsurround/lang/en_US.lang");
				if (stream != null)
					net.minecraftforge.fml.common.FMLCommonHandler.instance().loadLanguage(lookup, stream);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (stream != null)
						stream.close();
				} catch (final Throwable t) {
					;
				}
			}
		}

		@Override
		public String format(final String translateKey, final Object... parameters) {
			final String xlated = lookup.get(translateKey);
			return xlated == null ? translateKey : String.format(xlated, parameters);
		}
	}

	public static void initialize(final Side side) {
		if (side == Side.SERVER) {
			impl = new ServerImpl();
		} else {
			impl = new ClientImpl();
		}
	}

	public static String format(final String translateKey, final Object... parameters) {
		return impl.format(translateKey, parameters);
	}
}
