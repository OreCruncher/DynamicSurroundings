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

package org.orecruncher.lib.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.lib.logging.ModLog;

import net.minecraft.util.ITickable;

public class ExpressionCache implements ITickable {

	protected final ModLog logger;
	protected final List<DynamicVariantList> variants = new ArrayList<>();
	protected final IdentityHashMap<String, LazyVariant> cache = new IdentityHashMap<>();
	protected final List<String> naughtyList = new ArrayList<>();

	protected List<IDynamicVariant<?>> cachedList;

	public ExpressionCache(@Nonnull final ModLog logger) {
		this.logger = logger;
	}

	/**
	 * Adds a DynamicVariantList to the cache. New expressions that are created
	 * using this cache will automatically have these variables added to them. These
	 * variants will be updated when the ExpressionCache gets ticked.
	 *
	 * @param dvl
	 *            DynamicVariantList to add to the cache
	 */
	public void add(@Nonnull final DynamicVariantList dvl) {
		this.variants.add(dvl);
		this.cachedList = null;
	}

	/**
	 * Returns a list of expressions that have been examined by the ExpressionCache
	 * but failed due to some error. Used for diagnostics because ideally there
	 * should be no failures.
	 *
	 * @return List of expressions that failed compilation
	 */
	@Nonnull
	public List<String> getNaughtyList() {
		return this.naughtyList;
	}

	/**
	 * Returns a list of all dynamic variants available to the ExpressionCache. Used
	 * for diagnostic purpose. The list is sorted before returning.
	 *
	 * @return List of dynamic variants registered with the expression cache
	 */
	@Nonnull
	public List<IDynamicVariant<?>> getVariantList() {
		if (this.cachedList == null) {
			this.cachedList = new ArrayList<>();
			this.variants.forEach(dvl -> this.cachedList.addAll(dvl.getList()));
			Collections.sort(this.cachedList, (o1, o2) -> {
				return o1.getName().compareTo(o2.getName());
			});
		}

		return this.cachedList;
	}

	/**
	 * Ticks the internally cached DynamicVariableList entities.
	 */
	@Override
	public void update() {
		this.variants.forEach(DynamicVariantList::update);
	}

	/**
	 * This forces a compile and validation of the expression that is passed in.
	 * This will make use of any supplied built-in references. Custom instance
	 * variables, functions, or operators will cause this to fail since they cannot
	 * be set unless there is an Expression instance. Symbols in the built-in tables
	 * will work, however.
	 *
	 * Expressions are cached. If multiple requests come in for the same expression
	 * the cached version is reused.
	 *
	 * @param expression
	 *            The expression to compile
	 * @return The compiled expression. If there was a failure the resulting
	 *         expression will have the error message.
	 */
	@Nonnull
	private LazyVariant compile(@Nonnull final String expression) {
		LazyVariant exp = null;

		try {
			exp = this.cache.get(expression);
			if (exp == null) {
				final Expression x = new Expression(expression);
				this.variants.forEach(dvl -> dvl.attach(x));
				exp = x.getProgram();
				this.cache.put(expression, exp);
			}
		} catch (final Throwable t) {
			this.naughtyList.add(expression);
			this.cache.put(expression, exp = new StringValue(t.getMessage()));
			this.logger.warn("Unable to compile [%s]: %s", expression, t.getMessage());
		}
		return exp;
	}

	/**
	 * Evaluates the expression and returns the result. The resulting parse tree is
	 * cached for performance when a requery is made.
	 *
	 * @return Variant containing the results of the evaluation
	 */
	@Nonnull
	public Variant eval(@Nonnull final String script) {
		return compile(script.intern()).eval();
	}

	/**
	 * Evaluates the expression and returns the result as a boolean.
	 *
	 * @param expression
	 *            The expression to evaluate
	 * @return true of the expression evaluates true, false otherwise
	 */
	public boolean check(@Nonnull final String expression) {
		// If the string is empty return true.
		if (StringUtils.isEmpty(expression))
			return true;

		return eval(expression).asBoolean();
	}

}
