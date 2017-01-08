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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.util.script.Expression;
import org.blockartistry.mod.DynSurround.util.script.Variant;

public final class Evaluator {

	private static final Map<String, Expression> cache = new HashMap<String, Expression>();
	private static final List<String> naughtyList = new ArrayList<String>();

	@Nonnull
	public static List<String> getNaughtyList() {
		return naughtyList;
	}

	// This forces a compile and validation of the expression
	// that is passed in. This will make use of any supplied
	// built-in references. Custom instance variables, functions,
	// or operators will cause this to fail since they cannot
	// be set unless there is an Expression instance. Symbols
	// in the built-in tables will work, however.
	//
	// Expressions are cached. If multiple requests come in for
	// the same expression an older one is reused.
	@Nonnull
	private static Expression compile(final String expression) {
		Expression exp = null;

		try {
			exp = cache.get(expression);
			if (exp == null) {
				exp = new Expression(expression);
				exp.getRPN();
				cache.put(expression, exp);
			}
		} catch (final Throwable t) {
			naughtyList.add(expression);
			exp = new Expression("'" + t.getMessage() + "'");
			cache.put(expression, exp);
			ModLog.warn("Unable to compile [%s]: %s", expression, t.getMessage());
		}
		return exp;
	}

	@Nonnull
	public static Variant eval(@Nonnull final String script) {
		return compile(script).eval();
	}

	public static boolean check(@Nonnull final String conditions) {
		// Existing default regex - short circuit to make it faster
		if (StringUtils.isEmpty(conditions))
			return true;

		// Older regex supplied so it needs to be processed old school
		if (conditions.startsWith("(?i)")) {
			ModLog.warn("Old style condition string detected [%s]", conditions);
			return false;
		}

		// New stuff. Compile the expression and evaluate
		return eval(conditions).asBoolean();
	}
}
