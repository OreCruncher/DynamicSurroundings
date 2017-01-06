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

/**
 * Abstract definition of a supported operator. An operator is defined by its
 * name (pattern), precedence and if it is left- or right associative.
 */
public abstract class Operator {
	/**
	 * This operators name (pattern).
	 */
	private String oper;

	/**
	 * Operators precedence.
	 */
	private int precedence;

	/**
	 * Operator is left associative.
	 */
	private boolean leftAssoc;

	/**
	 * Operator is unary
	 */
	private boolean unary;

	public Operator(final String oper, final int precedence, final boolean leftAssoc) {
		this(oper, precedence, leftAssoc, false);
	}

	/**
	 * Creates a new operator.
	 * 
	 * @param oper
	 *            The operator name (pattern).
	 * @param precedence
	 *            The operators precedence.
	 * @param leftAssoc
	 *            <code>true</code> if the operator is left associative, else
	 *            <code>false</code>.
	 */
	public Operator(final String oper, final int precedence, final boolean leftAssoc, final boolean unary) {
		this.oper = oper;
		this.precedence = precedence;
		this.leftAssoc = leftAssoc;
		this.unary = unary;
	}

	public String getOper() {
		return this.oper;
	}

	public int getPrecedence() {
		return this.precedence;
	}

	public boolean isLeftAssoc() {
		return this.leftAssoc;
	}

	public boolean isUnary() {
		return this.unary;
	}

	/**
	 * Implementation for this operator.
	 * 
	 * @param v1
	 *            Operand 1.
	 * @param v2
	 *            Operand 2.
	 * @return The result of the operation.
	 */
	public abstract Variant eval(final Variant... operands);
}
