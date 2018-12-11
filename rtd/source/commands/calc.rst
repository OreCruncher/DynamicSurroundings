Command: calc
=============
Dynamic Surroundings has a client side command line calculator that can be used to evaluate
expressions.  The main purpose for me is to test out the internal expression engine, but it can be
useful for players just because it can handle math.

The reason it is an expression evaluator is because it can do things other than just math (i.e. 2+2).
It has the ability to evaluate logical expressions (9 < 10), and to handle strings ('test' + 35).
I am not sure what value this ability has to the regular player, but it is available in case someone
comes up with a decent use.

The calculator uses a subset of the features found in :ref:`condition strings <tutorial-condition-strings>`.
These features are summarized below.

..	list-table:: Built-in Operators
   	:widths: auto
   	:align: center
   	:header-rows: 1

   	*	- Operator
		- Name
		- Example
	*	- \+
		- Plus
		- 5 + 6
	*	- \-
		- Minus
		- 6 - 5
	*	- \*
		- Multiply
		- 4 * 5
	*	- /
		- Divide
		- 9 / 4
	*	- %
		- Modulus
		- 8 % 4
	*	- &&
		- Logical And
		- (9 < 10) && (4/2 == 2)
	*	- ||
		- Logical Or
		- (9 < 10) || (4//2 == 2)
	*	- >
		- Greather Than
		- 9 > 8
	*	- >=
		- Greater Than Equal
		- 10 >= 11
	*	- <
		- Less Than
		- 8 < 9
	*	- <=
		- Less Than Equal
		- 6 <= 6
	*	- =
		- Equal
		- 8 = 9
	*	- ==
		- Equivalent
		- 'this' == 'that'
	*	- !=
		- Not Equal
		- 9 != 10
	*	- <>
		- Not Equal
		- 9 <> 10
	*	- !
		- Not
		- Inverts a logical value (See NOT function below)

The expression engine has a set of built-in functions that can be used to simplify expressions.

..	list-table:: Built-in Functions
   	:widths: auto
   	:align: center
   	:header-rows: 1

   	*	- Function
		- Description
	*	- MATCH(regex, input)
		- Performs a regular expression match
	*	- NOT(expression)
		- Performs a logical not on an expression.  A value of 0 becomes 1, and a value of non-zero becomes 0.
	*	- IF(condition, trueExp, falseExp)
		- Performs one of two evaluations based on a condition
	*	- MAX(exp1,exp2,...)
		- Determines the max value from a selection of expressions
	*	- MIN(exp1,exp2,...)
		- Determines the min value from a selection of expressions
	*	- ABS(exp)
		- Determines the absolute value of an expression
	*	- ROUND(exp)
		- Rounds a number to the closest integer value
	*	- FLOOR(exp)
		- Returns the largest integer less than or equal to exp.
	*	- CEILING(exp)
		- Returns the smallest integer greater than or equal to exp.
	*	- SQRT(exp)
		- Calculates the square root of an expression
	*	- CLAMP(exp,min,max)
		- Ensures that an expression is within the specified bounds
	*	- ONEOF(exp,v1,...)
		- Determines if the result of the expression matches any of the specified values

A few constant variables are provided if they are needed.

..	list-table:: Constant Variables
   	:widths: auto
   	:align: center
   	:header-rows: 1
   	
   	*	- Variable
   		- Type
   		- Description
	*	- TRUE
		- boolean
		- Indicates true.  Has a value of 1.
	*	- FALSE
		- boolean
		- Indicates false.  Has a value of 0.
	*	- PI
		- Float
		- 3.1415927
