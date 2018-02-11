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

package org.blockartistry.lib.expression;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.blockartistry.lib.math.MathStuff;
import org.blockartistry.lib.random.XorShiftRandom;

public final class Expression {

	public static final Variant PI = new NumberValue("PI", MathStuff.PI_F);
	public static final Variant e = new NumberValue("e", MathStuff.E_F);
	public static final Variant TRUE = new BooleanValue("TRUE", true);
	public static final Variant FALSE = new BooleanValue("FALSE", false);

	// Built-in operators, functions, and variables. Allows for
	// the application to predefine items that will be used over and
	// over for multiple expressions.
	private static final OperatorTable builtInOperators = new OperatorTable();
	private static final FunctionTable builtInFunctions = new FunctionTable();
	private static final VariableTable builtInVariables = new VariableTable();

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
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().asBoolean() ? FALSE : TRUE;
			}
		});
		addBuiltInOperator(new Operator("+", 20, true) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().add(operands[1].eval());
			}
		});
		addBuiltInOperator(new Operator("-", 20, true) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return new NumberValue(operands[0].eval().asNumber() - operands[1].eval().asNumber());
			}
		});
		addBuiltInOperator(new Operator("*", 30, true) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return new NumberValue(operands[0].eval().asNumber() * operands[1].eval().asNumber());
			}
		});
		addBuiltInOperator(new Operator("/", 30, true) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return new NumberValue(operands[0].eval().asNumber() / operands[1].eval().asNumber());
			}
		});
		addBuiltInOperator(new Operator("%", 30, true) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return new NumberValue(operands[0].eval().asNumber() % operands[1].eval().asNumber());
			}
		});
		addBuiltInOperator(new Operator("&&", 4, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().asBoolean() && operands[1].eval().asBoolean() ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("||", 2, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().asBoolean() || operands[1].eval().asBoolean() ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator(">", 10, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().compareTo(operands[1].eval()) > 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator(">=", 10, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().compareTo(operands[1].eval()) >= 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("<", 10, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().compareTo(operands[1].eval()) < 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("<=", 10, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().compareTo(operands[1].eval()) <= 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("=", 7, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().compareTo(operands[1].eval()) == 0 ? TRUE : FALSE;
			}
		});
		addBuiltInOperator(new Operator("==", 7, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().compareTo(operands[1].eval()) == 0 ? TRUE : FALSE;
			}
		});

		addBuiltInOperator(new Operator("!=", 7, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().compareTo(operands[1].eval()) != 0 ? TRUE : FALSE;
			}
		});
		addBuiltInOperator(new Operator("<>", 7, false) {
			@Override
			public Variant eval(final LazyVariant... operands) {
				return operands[0].eval().compareTo(operands[1].eval()) != 0 ? TRUE : FALSE;
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
	 * The current infix expression, with optional variable substitutions.
	 */
	private String expression = null;

	/**
	 * The cached RPN (Reverse Polish Notation) of the expression.
	 */
	private List<String> rpn = null;

	/**
	 * The compiled expression.
	 */
	private LazyVariant program;

	/**
	 * All defined operators with name and implementation.
	 */
	private OperatorTable operators;

	/**
	 * All defined functions with name and implementation.
	 */
	private FunctionTable functions;

	/**
	 * All defined variables with name and value.
	 */
	private VariableTable variables;

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
		this.operators = new OperatorTable(builtInOperators);
		this.functions = new FunctionTable(builtInFunctions);
		this.variables = new VariableTable(builtInVariables);
	}

	/**
	 * Evaluates the expression.
	 * 
	 * @return The result of the expression.
	 */
	public Variant eval() {
		return this.getProgram().eval();
	}

	/**
	 * Returns the compiled program. Useful for when the Expression object itself is
	 * no longer needed but the caller wishes to retain the compiled results.
	 * 
	 * @return The compiled program
	 */
	public LazyVariant getProgram() {
		if (this.program == null) {
			final Compiler.Result result = new Compiler(this.operators, this.functions, this.variables)
					.compile(this.expression);
			this.program = result.expression;
			this.rpn = result.rpn;

		}
		return this.program;
	}

	/**
	 * Cached access to the RPN notation of this expression, ensures only one
	 * calculation of the RPN per expression instance. If no cached instance exists,
	 * a new one will be created and put to the cache.
	 * 
	 * @return The cached RPN instance.
	 */
	public List<String> getRPN() {
		if (this.rpn == null)
			this.getProgram();
		return this.rpn;
	}

	/**
	 * Get a string representation of the RPN (Reverse Polish Notation) for this
	 * expression.
	 * 
	 * @return A string with the RPN representation for this expression.
	 */
	public String toRPN() {
		return String.join(" ", getRPN());
	}

	public Expression addVariable(@Nonnull final Variant v) {
		this.variables.put(v.getName(), v);
		return this;
	}

	public Expression addVariables(@Nonnull final List<? extends Variant> list) {
		list.forEach(v -> this.addVariable(v));
		return this;
	}

	public Expression addVariables(@Nonnull final Map<String, ? extends Variant> map) {
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
		return Collections.unmodifiableSet(this.variables.keySet());
	}

	/**
	 * Exposing declared operators in the expression.
	 * 
	 * @return All declared operators.
	 */
	public Set<String> getDeclaredOperators() {
		return Collections.unmodifiableSet(this.operators.keySet());
	}

	/**
	 * Exposing declared functions.
	 * 
	 * @return All declared functions.
	 */
	public Set<String> getDeclaredFunctions() {
		return Collections.unmodifiableSet(this.functions.keySet());
	}

	/**
	 * @return The original expression string
	 */
	public String getExpression() {
		return this.expression;
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