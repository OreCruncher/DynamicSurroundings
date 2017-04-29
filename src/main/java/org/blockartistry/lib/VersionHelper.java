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

package org.blockartistry.lib;

import javax.annotation.Nonnull;

import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

public final class VersionHelper {

	private VersionHelper() {
	}

	public static int compareVersions(@Nonnull final String v1, @Nonnull final String v2) {
		if (StringUtils.isNullOrEmpty(v1) || v1.charAt(0) == '@')
			return -1;
		else if (StringUtils.isNullOrEmpty(v2) || v2.charAt(0) == '@')
			return 1;

		final ComparableVersion ver1 = new ComparableVersion(v1);
		final ComparableVersion ver2 = new ComparableVersion(v2);
		return ver1.compareTo(ver2);
	}
}
