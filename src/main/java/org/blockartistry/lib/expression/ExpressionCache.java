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

package org.blockartistry.lib.expression;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.lib.logging.ModLog;

public class ExpressionCache {

	protected final ModLog logger;
	protected final List<DynamicVariantList> variants = new ArrayList<DynamicVariantList>();
	protected final IdentityHashMap<String, Expression> cache = new IdentityHashMap<String, Expression>();
	protected final List<String> naughtyList = new ArrayList<String>();

	public ExpressionCache(final ModLog logger) {
		this.logger = logger;
	}
	
	public void add(final DynamicVariantList dvl) {
		this.variants.add(dvl);
	}
	
	@Nonnull
	public List<String> getNaughtyList() {
		return this.naughtyList;
	}

	// This forces a compile and validation of the expression
	// that is passed in. This will make use of any supplied
	// built-in references. Custom instance variables, functions,
	// or operators will cause this to fail since they cannot
	// be set unless there is an Expression instance. Symbols
	// in the built-in tables will work, however.
	//
	// Expressions are cached. If multiple requests come in for
	// the same expression the cached version is reused.
	@Nonnull
	private Expression compile(final String expression) {
		Expression exp = null;

		try {
			exp = this.cache.get(expression);
			if (exp == null) {
				exp = new Expression(expression);
				for (final DynamicVariantList dvl : this.variants)
					dvl.attach(exp);
				exp.getRPN();
				this.cache.put(expression, exp);
			}
		} catch (final Throwable t) {
			this.naughtyList.add(expression);
			exp = new Expression("'" + t.getMessage() + "'");
			this.cache.put(expression, exp);
			this.logger.warn("Unable to compile [%s]: %s", expression, t.getMessage());
		}
		return exp;
	}

	@Nonnull
	public Variant eval(@Nonnull final String script) {
		return compile(script.intern()).eval();
	}

	public boolean check(@Nonnull final String conditions) {
		// If the string is empty return true. They are always
		// true.
		if (StringUtils.isEmpty(conditions))
			return true;

		return eval(conditions).asBoolean();
	}
}
