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
package org.orecruncher.dsurround.expression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.lib.expression.ExpressionCache;
import org.orecruncher.lib.expression.IDynamicVariant;
import org.orecruncher.lib.expression.IVariant;
import org.orecruncher.lib.expression.Script;

public final class ExpressionEngine {

	private static ExpressionEngine instance;

	public static ExpressionEngine instance() {
		if (instance == null)
			instance = new ExpressionEngine();
		return instance;
	}

	private final ExpressionCache cache = new ExpressionCache(ModBase.log());
	// The scripts reported as broken, so each is reported once however many places use it
	private final Set<String> reported = new HashSet<>();

	private ExpressionEngine() {
		this.cache.add(new BattleVariables());
		this.cache.add(new BiomeTypeVariables());
		this.cache.add(new BiomeVariables());
		this.cache.add(new DimensionVariables());
		this.cache.add(new DiurnalVariables());
		this.cache.add(new PlayerVariables());
		this.cache.add(new SeasonVariables());
		this.cache.add(new WeatherVariables());
	}

	public void reset() {
		this.cache.reset();
	}

	public List<IDynamicVariant> getVariables() {
		return this.cache.getVariantList();
	}

	public IVariant eval(final String exp) {
		return this.cache.eval(exp);
	}

	public List<String> getNaughtyList() {
		return this.cache.getNaughtyList();
	}

	/**
	 * Compiles a script once, for a caller that evaluates it often to keep (a sound's or block effect's conditions)
	 */
	public Script compile(final String exp) {
		return this.cache.compile(exp);
	}

	/**
	 * Reports a script that doesn't compile, naming where it came from, once: config errors show as the config loads,
	 * rather than when a sound or effect is first checked
	 *
	 * @param where where it came from: "biome rule 'Desert wind'", "block [minecraft:lava]"
	 */
	public void reportIfBroken(final Script script, final String where) {
		if (!script.isCompiled() && this.reported.add(script.getExpression()))
			ModBase.log().warn("Conditions [%s] in %s don't work, so they are never true: %s", script.getExpression(),
					where, script.getError());
	}

	public boolean check(final String exp) {
		return this.cache.check(exp);
	}

}
