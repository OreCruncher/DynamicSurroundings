/*
 * Copyright 2012 Udo Klimaschewski
 * Copyright 2016 OreCruncher
 * 
 * http://UdoJava.com/
 * http://about.me/udo.klimaschewski
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

// Sourced from: https://github.com/uklimaschewski/EvalEx

package org.blockartistry.mod.DynSurround.util.script;

import org.blockartistry.mod.DynSurround.util.script.Expression.LazyFunction;
import org.blockartistry.mod.DynSurround.util.script.Expression.LazyVariant;

/**
 * Abstract definition of a supported expression function. A function is defined
 * by a name, the number of parameters and the actual processing implementation.
 */
public abstract class Function extends LazyFunction {

	public Function(final String name, final int numParams) {
		super(name, numParams);
	}

	public LazyVariant lazyEval(final LazyVariant... lazyParams) {
		final Variant[] params = new Variant[lazyParams.length];
		for (int i = 0; i < lazyParams.length; i++)
			params[i] = lazyParams[i].eval();
		return new LazyVariant() {
			public Variant eval() {
				return Function.this.eval(params);
			}
		};
	}

	/**
	 * Implementation for this function.
	 *
	 * @param params
	 *            Parameters will be passed by the expression evaluator as an
	 *            array of {@link Variant} values.
	 * @return The function must return a new {@link Variant} value as a
	 *         computing result.
	 */
	public abstract Variant eval(final Variant... params);
}
