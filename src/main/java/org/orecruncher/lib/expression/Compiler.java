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
package org.orecruncher.lib.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Compiler {

	public static class Result {
		public final LazyVariant expression;
		public final List<String> rpn;

		Result(@Nonnull final LazyVariant exp, @Nonnull final List<String> rpn) {
			this.expression = exp;
			this.rpn = rpn;
		}
	}

	/**
	 * The Float representation of the left parenthesis, used for parsing varying
	 * numbers of function parameters.
	 */
	private static final LazyVariant PARAMS_START = () -> null;

	private final OperatorTable operators;
	private final FunctionTable functions;
	private final VariableTable variables;

	public Compiler(@Nullable final OperatorTable operators, @Nullable final FunctionTable functions,
			@Nullable final VariableTable variables) {
		this.operators = operators != null ? operators : new OperatorTable();
		this.functions = functions != null ? functions : new FunctionTable();
		this.variables = variables != null ? variables : new VariableTable();
	}

	public Result compile(@Nonnull final String expression) {
		final Stack<LazyVariant> stack = new Stack<>();
		final List<String> rpn = getRPN(expression);
		for (final String token : rpn) {
			if (this.operators.containsKey(token)) {
				final Operator op = this.operators.get(token);
				final LazyVariant result;
				if (op.isUnary()) {
					final LazyVariant v1 = stack.pop();
					result = () -> op.eval(v1);
				} else {
					final LazyVariant v1 = stack.pop();
					final LazyVariant v2 = stack.pop();
					result = () -> op.eval(v2, v1);
				}
				stack.push(result);
			} else if (this.variables.containsKey(token)) {
				stack.push(this.variables.get(token));
			} else if (this.functions.containsKey(token.toUpperCase(Locale.ROOT))) {
				final LazyFunction f = this.functions.get(token.toUpperCase(Locale.ROOT));
				final ArrayList<LazyVariant> p = new ArrayList<>(
						!f.numParamsVaries() ? f.getNumParams() : 0);
				// pop parameters off the stack until we hit the start of
				// this function's parameter list
				while (!stack.isEmpty() && stack.peek() != PARAMS_START) {
					p.add(0, stack.pop());
				}
				if (stack.peek() == PARAMS_START) {
					stack.pop();
				}
				final LazyVariant[] parms = new LazyVariant[p.size()];
				p.toArray(parms);
				final LazyVariant fResult = () -> f.lazyEval(parms).eval();
				stack.push(fResult);
			} else if ("(".equals(token)) {
				stack.push(PARAMS_START);
			} else if (token.charAt(0) == Tokenizer.quote) {
				final String s = token.substring(1, token.length() - 1);
				stack.push(new StringValue(s));
			} else {
				final float val = Float.parseFloat(token);
				stack.push(new NumberValue(val));
			}
		}
		return new Result(stack.pop(), rpn);
	}

	/**
	 * Is the string a number?
	 *
	 * @param st
	 *            The string.
	 * @return <code>true</code>, if the input string is a number.
	 */
	private static boolean isNumber(final String st) {
		if (st.charAt(0) == Tokenizer.minusSign && st.length() == 1)
			return false;
		if (st.charAt(0) == '+' && st.length() == 1)
			return false;
		if (st.charAt(0) == 'e' || st.charAt(0) == 'E')
			return false;
		for (final char ch : st.toCharArray()) {
			if (!Character.isDigit(ch) && ch != Tokenizer.minusSign && ch != Tokenizer.decimalSeparator && ch != 'e'
					&& ch != 'E' && ch != '+')
				return false;
		}
		return true;
	}

	/**
	 * Implementation of the <i>Shunting Yard</i> algorithm to transform an infix
	 * expression to a RPN expression.
	 *
	 * @param expression
	 *            The input expression in infx.
	 * @return A RPN representation of the expression, with each token as a list
	 *         member.
	 */
	private List<String> shuntingYard(final String expression) {
		final List<String> outputQueue = new ArrayList<>();
		final Stack<String> stack = new Stack<>();
		final Tokenizer tokenizer = new Tokenizer(expression, this.operators.keySet());

		String lastFunction = null;
		String previousToken = null;
		while (tokenizer.hasNext()) {
			final String token = tokenizer.next();
			if (isNumber(token)) {
				outputQueue.add(token);
			} else if (token.charAt(0) == Tokenizer.quote) {
				outputQueue.add(token);
			} else if (this.variables.containsKey(token)) {
				outputQueue.add(token);
			} else if (this.functions.containsKey(token.toUpperCase(Locale.ROOT))) {
				stack.push(token);
				lastFunction = token;
			} else if (Character.isLetter(token.charAt(0))) {
				stack.push(token);
			} else if (",".equals(token)) {
				if (this.operators.containsKey(previousToken)) {
					throw new ExpressionException("Missing parameter(s) for operator " + previousToken
							+ " at character position " + (tokenizer.getPos() - 1 - previousToken.length()));
				}
				while (!stack.isEmpty() && !"(".equals(stack.peek())) {
					outputQueue.add(stack.pop());
				}
				if (stack.isEmpty()) {
					throw new ExpressionException("Parse error for function '" + lastFunction + "'");
				}
			} else if (this.operators.containsKey(token)) {
				if (",".equals(previousToken) || "(".equals(previousToken)) {
					throw new ExpressionException("Missing parameter(s) for operator " + token
							+ " at character position " + (tokenizer.getPos() - token.length()));
				}
				final Operator o1 = this.operators.get(token);
				String token2 = stack.isEmpty() ? null : stack.peek();
				while (token2 != null && this.operators.containsKey(token2)
						&& ((o1.isLeftAssoc() && o1.getPrecedence() <= this.operators.get(token2).getPrecedence())
								|| (o1.getPrecedence() < this.operators.get(token2).getPrecedence()))) {
					outputQueue.add(stack.pop());
					token2 = stack.isEmpty() ? null : stack.peek();
				}
				stack.push(token);
			} else if ("(".equals(token)) {
				if (previousToken != null) {
					if (isNumber(previousToken)) {
						throw new ExpressionException("Missing operator at character position " + tokenizer.getPos());
					}
					// if the ( is preceded by a valid function, then it
					// denotes the start of a parameter list
					if (this.functions.containsKey(previousToken.toUpperCase(Locale.ROOT))) {
						outputQueue.add(token);
					}
				}
				stack.push(token);
			} else if (")".equals(token)) {
				if (this.operators.containsKey(previousToken)) {
					throw new ExpressionException("Missing parameter(s) for operator " + previousToken
							+ " at character position " + (tokenizer.getPos() - 1 - previousToken.length()));
				}
				while (!stack.isEmpty() && !"(".equals(stack.peek())) {
					outputQueue.add(stack.pop());
				}
				if (stack.isEmpty()) {
					throw new ExpressionException("Mismatched parentheses");
				}
				stack.pop();
				if (!stack.isEmpty() && this.functions.containsKey(stack.peek().toUpperCase(Locale.ROOT))) {
					outputQueue.add(stack.pop());
				}
			}
			previousToken = token;
		}
		while (!stack.isEmpty()) {
			final String element = stack.pop();
			if ("(".equals(element) || ")".equals(element)) {
				throw new ExpressionException("Mismatched parentheses");
			}
			if (!this.operators.containsKey(element)) {
				throw new ExpressionException("Unknown operator or function: " + element);
			}
			outputQueue.add(element);
		}
		return outputQueue;
	}

	/**
	 * Cached access to the RPN notation of this expression, ensures only one
	 * calculation of the RPN per expression instance. If no cached instance exists,
	 * a new one will be created and put to the cache.
	 *
	 * @return The cached RPN instance.
	 */
	private List<String> getRPN(@Nonnull final String expression) {
		final List<String> rpn = shuntingYard(expression);
		validate(rpn);
		return rpn;
	}

	/**
	 * Check that the expression has enough numbers and variables to fit the
	 * requirements of the operators and functions, also check for only 1 result
	 * stored at the end of the evaluation.
	 */
	private void validate(final List<String> rpn) {
		/*-
		* Thanks to Norman Ramsey:
		* http://http://stackoverflow.com/questions/789847/postfix-notation-validation
		*/
		// each push on to this stack is a new function scope, with the value of
		// each
		// layer on the stack being the count of the number of parameters in
		// that scope
		final Stack<Integer> stack = new Stack<>();

		// push the 'global' scope
		stack.push(0);

		for (final String token : rpn) {
			if (this.operators.containsKey(token)) {
				if (this.operators.get(token).isUnary()) {
					if (stack.peek() < 1) {
						throw new ExpressionException("Missing parameter(s) for operator " + token);
					}
					// pop the operator's 1 parameters and add the result
					stack.set(stack.size() - 1, stack.peek() - 1 + 1);
				} else {
					if (stack.peek() < 2) {
						throw new ExpressionException("Missing parameter(s) for operator " + token);
					}
					// pop the operator's 2 parameters and add the result
					stack.set(stack.size() - 1, stack.peek() - 2 + 1);
				}
			} else if (this.variables.containsKey(token)) {
				stack.set(stack.size() - 1, stack.peek() + 1);
			} else if (this.functions.containsKey(token.toUpperCase(Locale.ROOT))) {
				final LazyFunction f = this.functions.get(token.toUpperCase(Locale.ROOT));
				final int numParams = stack.pop();
				if (!f.numParamsVaries() && numParams != f.getNumParams()) {
					throw new ExpressionException(
							"Function " + token + " expected " + f.getNumParams() + " parameters, got " + numParams);
				}
				if (stack.size() <= 0) {
					throw new ExpressionException("Too many function calls, maximum scope exceeded");
				}
				// push the result of the function
				stack.set(stack.size() - 1, stack.peek() + 1);
			} else if ("(".equals(token)) {
				stack.push(0);
			} else {
				stack.set(stack.size() - 1, stack.peek() + 1);
			}
		}

		if (stack.size() > 1) {
			throw new ExpressionException("Too many unhandled function parameter lists");
		} else if (stack.peek() > 1) {
			throw new ExpressionException("Too many numbers or variables");
		} else if (stack.peek() < 1) {
			throw new ExpressionException("Empty expression");
		}
	}
}
