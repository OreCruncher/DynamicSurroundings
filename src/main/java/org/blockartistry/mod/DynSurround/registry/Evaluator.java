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

package org.blockartistry.mod.DynSurround.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.script.Expression;
import org.blockartistry.mod.DynSurround.util.script.Variant;

public class Evaluator {

	private static final List<String> naughtyList = new ArrayList<String>();

	@Nonnull
	public static List<String> getNaughtyList() {
		return naughtyList;
	}
	
	@Nonnull
	public static Variant eval(@Nonnull final String script) {
		return Expression.compile(script).eval();
	}
	
	public static boolean check(@Nonnull final String conditions) {
		// Existing default regex - short circuit to make it faster
		if (StringUtils.isEmpty(conditions) || conditions.startsWith(".*"))
			return true;

		// Older regex supplied so it needs to be processed old school
		if (conditions.startsWith("(?i)")) {
			// Old school
			final String ev = EnvironState.getConditions();
			return Pattern.matches(conditions, ev);
		}

		// If it was bad the first time around it is doubtful it
		// changed it's ways.
		if (naughtyList.contains(conditions))
			return false;

		// New stuff. Compile the expression and evaluate
		try {
			final Variant result = eval(conditions);
			return result.asNumber() != 0.0F;
		} catch (final Throwable t) {
			ModLog.error("Unable to execute check: " + conditions, t);
			naughtyList.add(conditions);
		}

		// Something bad happened, so return no match
		return false;
	}
}
