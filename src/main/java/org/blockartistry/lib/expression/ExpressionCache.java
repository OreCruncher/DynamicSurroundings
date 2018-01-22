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
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.lib.logging.ModLog;

import net.minecraft.util.ITickable;

public class ExpressionCache implements ITickable {

	protected final ModLog logger;
	protected final List<DynamicVariantList> variants = new ArrayList<DynamicVariantList>();
	protected final IdentityHashMap<String, Expression> cache = new IdentityHashMap<String, Expression>();
	protected final List<String> naughtyList = new ArrayList<String>();

	public ExpressionCache(final ModLog logger) {
		this.logger = logger;
	}

	/*
	 * Adds a DynamicVariantList to the cache. New expressions that are created will
	 * automatically have these variables added to them. These variants will be
	 * updated when the ExpressionCache gets ticked.
	 */
	public void add(final DynamicVariantList dvl) {
		this.variants.add(dvl);
	}

	/*
	 * Returns a list of expressions that have been examined by the ExpressionCache
	 * but failed due to some error. Used for diagnostics because ideally there
	 * should be no failures.
	 */
	@Nonnull
	public List<String> getNaughtyList() {
		return this.naughtyList;
	}

	/*
	 * Returns a list of all dynamic variants available to the ExpressionCache. Used
	 * for diagnostic purpose.  The list is sorted before returning.
	 */
	@Nonnull
	public List<IDynamicVariant<?>> getVariantList() {
		final List<IDynamicVariant<?>> result = new ArrayList<IDynamicVariant<?>>();
		for (final DynamicVariantList dvl : this.variants)
			result.addAll(dvl.getList());

		Collections.sort(result, new Comparator<IDynamicVariant<?>>() {
			@Override
			public int compare(@Nonnull final IDynamicVariant<?> o1, @Nonnull final IDynamicVariant<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		return result;
	}

	/*
	 * Ticks the internally cached DynamicVariableList entities.
	 */
	@Override
	public void update() {
		for (final DynamicVariantList dvl : this.variants)
			dvl.update();
	}

	/*
	 * This forces a compile and validation of the expression that is passed in.
	 * This will make use of any supplied built-in references. Custom instance
	 * variables, functions, or operators will cause this to fail since they cannot
	 * be set unless there is an Expression instance. Symbols in the built-in tables
	 * will work, however.
	 *
	 * Expressions are cached. If multiple requests come in for the same expression
	 * the cached version is reused.
	 */
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

	/*
	 * Evaluates the expression and returns the result. The resulting parse tree is
	 * cached for performance when a requery is made.
	 */
	@Nonnull
	public Variant eval(@Nonnull final String script) {
		return compile(script.intern()).eval();
	}

	/*
	 * Evaluates the expression and returns the result as a boolean.
	 */
	public boolean check(@Nonnull final String conditions) {
		// If the string is empty return true.
		if (StringUtils.isEmpty(conditions))
			return true;

		return eval(conditions).asBoolean();
	}

}
