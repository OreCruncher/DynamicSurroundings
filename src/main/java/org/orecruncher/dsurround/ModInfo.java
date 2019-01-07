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

package org.orecruncher.dsurround;

public final class ModInfo {

	public static final String MOD_ID = "dsurround";
	public static final String API_ID = MOD_ID + "API";
	public static final String RESOURCE_ID = "dsurround";
	public static final String MOD_NAME = "Dynamic Surroundings";
	public static final String VERSION = "@VERSION@";
	public static final String MINECRAFT_VERSIONS = "[1.12.2,)";
	public static final String GUI_FACTORY = "org.orecruncher.dsurround.client.gui.ConfigGuiFactory";
	public static final String UPDATE_URL = "@UPDATEURL@";
	public static final String FINGERPRINT = "@FINGERPRINT@";
	public static final String REMOTE_VERSIONS = "*";

	//@formatter:off
	public static final String DEPENDENCIES =
		"required-after:forge@[14.23.5.2768,);" +
		"required-after:dsurroundcore;" +
		"required-after:orelib@[3.5.2.1,);" +
		"after:sereneseasons@[1.2.13,);" +
		"after:galacticraftcore;" +
		"after:ambientsounds;";
	//@formatter:on
}
