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

package org.blockartistry.lib.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.blockartistry.lib.MathStuff;
import org.blockartistry.lib.random.XorShiftRandom;

public final class Expression {

	public static final Variant PI = new NumberValue("PI", MathStuff.PI_F);
	public static final Variant e = new NumberValue("e", MathStuff.E_F);
	public static final Variant TRUE = new BooleanValue("TRUE", true);
	public static final Variant FALSE = new BooleanValue("FALSE", false);

	// Built-in operators, functions, and variables. Allows for
	// the application to predefine items that will be used over and
	// over for multiple expressions.
	private static final Map<String, Operator> builtInOperators = new TreeMap<String, Operator>(
			String.CASE_INSENSITIVE_ORDER);
	private static final Map<String, LazyFunction> builtInFunctions = new TreeMap<String, LazyFunction>(
			String.CASE_INSENSITIVE_ORDER);
	private static final Map<String, LazyVariant> builtInVariables = new TreeMap<String, LazyVariant>(
			String.CASE_INSENSITIVE_ORDER);

	public static void addBuiltInOperator(final Operator op) {
		builtInOperators.put(op.getOper(), op);
	}

	public static void addBuiltInFunction(final LazyFunction func) {
		builtInFunctions.put(func.getName(), func);
	}

	public static void addBuiltInVariable(final String name, final LazyVariant number) {
		builtInVariables.put(name, number);
	}
	
	public static void addBuiltInVariable(@Nonnull final Variant v) {
		builtInVariables.put(v.getName(), v);
	}

	static {
		addBuiltInOperator(new Operator("!", 20, false, true) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].asBoolean() ? FALSE : TRUE;
			}
		});
		addBuiltInOperator(new Operator("+", 20, true) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].add(operands[1]);
			}
		});
		addBuiltInOperator(new Operator("-", 20, true) {
			@Override
			public Variant eval(final Variant... operands) {
				return new NumberValue(operands[0].asNumber() - operands[1].asNumber());
			}
		});
		addBuiltInOperator(new Operator("*", 30, true) {
			@Override
			public Variant eval(final Variant... operands) {
				return new NumberValue(operands[0].asNumber() * operands[1].asNumber());
			}
		});
		addBuiltInOperator(new Operator("/", 30, true) {
			@Override
			public Variant eval(final Variant... operands) {
				return new NumberValue(operands[0].asNumber() / operands[1].asNumber());
			}
		});
		addBuiltInOperator(new Operator("%", 30, true) {
			@Override
			public Variant eval(final Variant... operands) {
				return new NumberValue(operands[0].asNumber() % operands[1].asNumber());
			}
		});
		addBuiltInOperator(new Operator("&&", 4, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].asBoolean() && operands[1].asBoolean() ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("||", 2, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].asBoolean() || operands[1].asBoolean() ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator(">", 10, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].compareTo(operands[1]) > 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator(">=", 10, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].compareTo(operands[1]) >= 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("<", 10, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].compareTo(operands[1]) < 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("<=", 10, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].compareTo(operands[1]) <= 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("=", 7, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].compareTo(operands[1]) == 0 ? TRUE : FALSE;
			}
		});
		addBuiltInOperator(new Operator("==", 7, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].compareTo(operands[1]) == 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("!=", 7, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].compareTo(operands[1]) != 0 ? TRUE : FALSE;
			}
		});
		addBuiltInOperator(new Operator("<>", 7, false) {
			@Override
			public Variant eval(final Variant... operands) {
				return operands[0].compareTo(operands[1]) != 0 ? TRUE : FALSE;
			}
		});
		addBuiltInFunction(new Function("MATCH", 2) {
			@Override
			public Variant eval(final Variant... parameters) {
				final String regex = parameters[0].asString();
				final String input = parameters[1].asString();
				return Pattern.matches(regex, input) ? TRUE : FALSE;
			}
		});
		addBuiltInFunction(new Function("NOT", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				return parameters[0].asBoolean() ? FALSE : TRUE;
			}
		});

		// Do lazy function here because we only need to evaluate one of the
		// branches based on the value of the first parameter.
		addBuiltInFunction(new LazyFunction("IF", 3) {
			@Override
			public LazyVariant lazyEval(final LazyVariant... lazyParams) {
				final boolean isTrue = lazyParams[0].eval().asBoolean();
				return isTrue ? lazyParams[1] : lazyParams[2];
			}
		});

		addBuiltInFunction(new Function("RANDOM", 0) {
			@Override
			public Variant eval(final Variant... parameters) {
				return new NumberValue(XorShiftRandom.current().nextFloat());
			}
		});
		addBuiltInFunction(new Function("SIN", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float d = MathStuff.sin(MathStuff.toRadians(parameters[0].asNumber()));
				return new NumberValue(d);
			}
		});
		addBuiltInFunction(new Function("COS", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float d = MathStuff.cos(MathStuff.toRadians(parameters[0].asNumber()));
				return new NumberValue(d);
			}
		});
		addBuiltInFunction(new Function("TAN", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float d = MathStuff.tan(MathStuff.toRadians(parameters[0].asNumber()));
				return new NumberValue(d);
			}
		});
		addBuiltInFunction(new Function("RAD", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float d = MathStuff.toRadians(parameters[0].asNumber());
				return new NumberValue(d);
			}
		});
		addBuiltInFunction(new Function("DEG", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float d = MathStuff.toDegrees(parameters[0].asNumber());
				return new NumberValue(d);
			}
		});
		addBuiltInFunction(new Function("MAX", -1) {
			@Override
			public Variant eval(final Variant... parameters) {
				if (parameters.length == 0) {
					throw new ExpressionException("MAX requires at least one parameter");
				}
				Variant max = null;
				for (Variant parameter : parameters) {
					if (max == null || parameter.compareTo(max) > 0) {
						max = parameter;
					}
				}
				return max;
			}
		});
		addBuiltInFunction(new Function("ONEOF", -1) {
			@Override
			public Variant eval(final Variant... parameters) {
				if (parameters.length < 2) {
					throw new ExpressionException("ONEOF requires at least two parameters");
				}
				final Variant selector = parameters[0];
				for (int i = 1; i < parameters.length; i++) {
					if (selector.compareTo(parameters[i]) == 0)
						return TRUE;
				}
				return FALSE;
			}
		});
		addBuiltInFunction(new Function("MIN", -1) {
			@Override
			public Variant eval(final Variant... parameters) {
				if (parameters.length == 0) {
					throw new ExpressionException("MIN requires at least one parameter");
				}
				Variant min = null;
				for (Variant parameter : parameters) {
					if (min == null || parameter.compareTo(min) < 0) {
						min = parameter;
					}
				}
				return min;
			}
		});
		addBuiltInFunction(new Function("ABS", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				return new NumberValue(MathStuff.abs(parameters[0].asNumber()));
			}
		});
		addBuiltInFunction(new Function("ROUND", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float toRound = parameters[0].asNumber();
				return new NumberValue(Math.round(toRound));
			}
		});
		addBuiltInFunction(new Function("FLOOR", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float toRound = parameters[0].asNumber();
				return new NumberValue(Math.floor(toRound));
			}
		});
		addBuiltInFunction(new Function("CEILING", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float toRound = parameters[0].asNumber();
				return new NumberValue(Math.ceil(toRound));
			}
		});
		addBuiltInFunction(new Function("SQRT", 1) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float x = parameters[0].asNumber();
				return new NumberValue(Math.sqrt(x));
			}
		});
		addBuiltInFunction(new Function("CLAMP", 3) {
			@Override
			public Variant eval(final Variant... parameters) {
				final float val = parameters[0].asNumber();
				final float low = parameters[1].asNumber();
				final float high = parameters[2].asNumber();
				return new NumberValue(MathStuff.clamp(val, low, high));
			}
		});

		addBuiltInVariable(e.getName(), e);
		addBuiltInVariable(PI.getName(), PI);
		addBuiltInVariable(TRUE.getName(), TRUE);
		addBuiltInVariable(FALSE.getName(), FALSE);

	}

	/**
	 * The original infix expression.
	 */
	private final String originalExpression;

	/**
	 * The current infix expression, with optional variable substitutions.
	 */
	private String expression = null;

	/**
	 * The cached RPN (Reverse Polish Notation) of the expression.
	 */
	private List<String> rpn = null;

	/**
	 * All defined operators with name and implementation.
	 */
	private final Map<String, Operator> operators = new TreeMap<String, Operator>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * All defined functions with name and implementation.
	 */
	private final Map<String, LazyFunction> functions = new TreeMap<String, LazyFunction>(
			String.CASE_INSENSITIVE_ORDER);

	/**
	 * All defined variables with name and value.
	 */
	private final Map<String, LazyVariant> variables = new TreeMap<String, LazyVariant>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * The Float representation of the left parenthesis, used for parsing
	 * varying numbers of function parameters.
	 */
	private static final LazyVariant PARAMS_START = new LazyVariant() {
		public Variant eval() {
			return null;
		}
	};

	/**
	 * LazyVariant interface created for lazily evaluated functions
	 */
	public static interface LazyVariant {
		Variant eval();
	}

	public static abstract class LazyFunction {
		/**
		 * Name of this function.
		 */
		private String name;
		/**
		 * NumberValue of parameters expected for this function. <code>-1</code>
		 * denotes a variable number of parameters.
		 */
		private int numParams;

		/**
		 * Creates a new function with given name and parameter count.
		 *
		 * @param name
		 *            The name of the function.
		 * @param numParams
		 *            The number of parameters for this function.
		 *            <code>-1</code> denotes a variable number of parameters.
		 */
		public LazyFunction(final String name, final int numParams) {
			this.name = name.toUpperCase(Locale.ROOT);
			this.numParams = numParams;
		}

		public String getName() {
			return this.name;
		}

		public int getNumParams() {
			return this.numParams;
		}

		public boolean numParamsVaries() {
			return this.numParams < 0;
		}

		public abstract LazyVariant lazyEval(final LazyVariant... lazyParams);
	}

	/**
	 * Creates a new expression instance from an expression string with a given
	 * default match context of {@link MathContext#DECIMAL32}.
	 * 
	 * @param expression
	 *            The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or
	 *            <code>"sin(y)>0 & max(z, 3)>3"</code>
	 */
	public Expression(final String expression) {
		this.expression = expression;
		this.originalExpression = expression;

		this.operators.putAll(builtInOperators);
		this.functions.putAll(builtInFunctions);
		this.variables.putAll(builtInVariables);
	}

	/**
	 * Is the string a number?
	 * 
	 * @param st
	 *            The string.
	 * @return <code>true</code>, if the input string is a number.
	 */
	private boolean isNumber(final String st) {
		if (st.charAt(0) == Tokenizer.minusSign && st.length() == 1)
			return false;
		if (st.charAt(0) == '+' && st.length() == 1)
			return false;
		if (st.charAt(0) == 'e' || st.charAt(0) == 'E')
			return false;
		for (char ch : st.toCharArray()) {
			if (!Character.isDigit(ch) && ch != Tokenizer.minusSign && ch != Tokenizer.decimalSeparator && ch != 'e'
					&& ch != 'E' && ch != '+')
				return false;
		}
		return true;
	}

	/**
	 * Implementation of the <i>Shunting Yard</i> algorithm to transform an
	 * infix expression to a RPN expression.
	 * 
	 * @param expression
	 *            The input expression in infx.
	 * @return A RPN representation of the expression, with each token as a list
	 *         member.
	 */
	private List<String> shuntingYard(final String expression) {
		final List<String> outputQueue = new ArrayList<String>();
		final Stack<String> stack = new Stack<String>();
		final Tokenizer tokenizer = new Tokenizer(expression, this.operators.keySet());

		String lastFunction = null;
		String previousToken = null;
		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
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
				Operator o1 = this.operators.get(token);
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
				if (operators.containsKey(previousToken)) {
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
				if (!stack.isEmpty() && functions.containsKey(stack.peek().toUpperCase(Locale.ROOT))) {
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

	private LazyVariant exp;

	/**
	 * Evaluates the expression.
	 * 
	 * @return The result of the expression.
	 */
	public Variant eval() {

		if (this.exp == null) {
			final Stack<LazyVariant> stack = new Stack<LazyVariant>();
			for (final String token : getRPN()) {
				if (this.operators.containsKey(token)) {
					final Operator op = this.operators.get(token);
					final LazyVariant result;
					if (op.isUnary()) {
						final LazyVariant v1 = stack.pop();
						result = new LazyVariant() {
							public Variant eval() {
								return op.eval(v1.eval());
							}
						};
					} else {
						final LazyVariant v1 = stack.pop();
						final LazyVariant v2 = stack.pop();
						result = new LazyVariant() {
							public Variant eval() {
								return op.eval(v2.eval(), v1.eval());
							}
						};
					}
					stack.push(result);
				} else if (this.variables.containsKey(token)) {
					stack.push(this.variables.get(token));
				} else if (this.functions.containsKey(token.toUpperCase(Locale.ROOT))) {
					final LazyFunction f = this.functions.get(token.toUpperCase(Locale.ROOT));
					final ArrayList<LazyVariant> p = new ArrayList<LazyVariant>(
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
					final LazyVariant fResult = new LazyVariant() {
						public Variant eval() {
							return f.lazyEval(parms).eval();
						}
					};
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
			this.exp = stack.pop();
		}
		return this.exp.eval();
	}

	/**
	 * Cached access to the RPN notation of this expression, ensures only one
	 * calculation of the RPN per expression instance. If no cached instance
	 * exists, a new one will be created and put to the cache.
	 * 
	 * @return The cached RPN instance.
	 */
	public List<String> getRPN() {
		if (this.rpn == null) {
			this.rpn = shuntingYard(this.expression);
			validate(this.rpn);
		}
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
		final Stack<Integer> stack = new Stack<Integer>();

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
				LazyFunction f = this.functions.get(token.toUpperCase(Locale.ROOT));
				int numParams = stack.pop();
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

	/**
	 * Get a string representation of the RPN (Reverse Polish Notation) for this
	 * expression.
	 * 
	 * @return A string with the RPN representation for this expression.
	 */
	public String toRPN() {
		StringBuilder result = new StringBuilder();
		for (String st : getRPN()) {
			if (result.length() != 0)
				result.append(" ");
			result.append(st);
		}
		return result.toString();
	}

	@Nonnull
	public Expression addVariable(@Nonnull final Variant v) {
		this.variables.put(v.getName(), v);
		return this;
	}

	@Nonnull
	public Expression addVariables(@Nonnull final List<Variant> list) {
		for (final Variant v : list)
			this.addVariable(v);
		return this;
	}

	@Nonnull
	public Expression addVariables(@Nonnull final Map<String, Variant> map) {
		this.variables.putAll(map);
		return this;
	}
	
	@Nonnull
	public Expression addFunction(@Nonnull final Function func) {
		this.functions.put(func.getName(), func);
		return this;
	}

	/**
	 * Exposing declared variables in the expression.
	 * 
	 * @return All declared variables.
	 */
	public Set<String> getDeclaredVariables() {
		return Collections.unmodifiableSet(variables.keySet());
	}

	/**
	 * Exposing declared operators in the expression.
	 * 
	 * @return All declared operators.
	 */
	public Set<String> getDeclaredOperators() {
		return Collections.unmodifiableSet(operators.keySet());
	}

	/**
	 * Exposing declared functions.
	 * 
	 * @return All declared functions.
	 */
	public Set<String> getDeclaredFunctions() {
		return Collections.unmodifiableSet(functions.keySet());
	}

	/**
	 * @return The original expression string
	 */
	public String getExpression() {
		return this.expression;
	}

	/**
	 * The original expression used to construct this expression, without
	 * variables substituted.
	 */
	public String getOriginalExpression() {
		return this.originalExpression;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Expression that = (Expression) o;
		if (this.expression == null) {
			return that.expression == null;
		} else {
			return this.expression.equals(that.expression);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.expression == null ? 0 : this.expression.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return toRPN();
	}

}