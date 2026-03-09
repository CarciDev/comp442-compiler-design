<!-- TODO(A3-10): CREATE COMPREHENSIVE TEST FILES (10 pts) -->

# A3 Test Files Checklist

The assignment awards **10 points** for completeness of test cases.
You need `.src` files that cover ALL syntactic structures in the language.
Each test file should produce a correct `.outast` output file.

## Structures to Test

Check off each structure as you create a test file covering it:

### Program Structure
- [ ] Empty program (just `main` with empty body)
- [ ] Program with classes, functions, AND main block together

### Class Declarations
- [ ] Simple class with member variables
- [ ] Class with member functions (funcDecl)
- [ ] Class with inheritance (`inherits`)
- [ ] Class with multiple inheritance (`inherits A, B, C`)
- [ ] Class with public/private visibility on members
- [ ] Multiple class declarations

### Function Definitions
- [ ] Free function (no class scope): `funcName(params) : type`
- [ ] Member function (class scope): `ClassName::funcName(params) : type`
- [ ] Function with `void` return type
- [ ] Function with parameters (integer, float, class type)
- [ ] Function with array parameters (`integer arr[]`, `float m[2][3]`)
- [ ] Function with no parameters
- [ ] Function with local variable declarations

### Variable Declarations
- [ ] Simple variable: `integer x;`
- [ ] Float variable: `float y;`
- [ ] Class-typed variable: `MyClass obj;`
- [ ] Array variable with dimensions: `integer arr[5][10];`
- [ ] Array variable with empty brackets: `integer arr[];`

### Statements
- [ ] Assignment: `x = expr;`
- [ ] If-then-else: `if (relexpr) then statblock else statblock;`
- [ ] While loop: `while (relexpr) statblock;`
- [ ] Read: `read(variable);`
- [ ] Write: `write(expr);`
- [ ] Return: `return(expr);`
- [ ] Function call as statement: `func(args);`
- [ ] Method call as statement: `obj.method(args);`
- [ ] Statement block with `do ... end`
- [ ] Nested if/while statements

### Expressions
- [ ] Addition: `a + b`
- [ ] Subtraction: `a - b`
- [ ] Multiplication: `a * b`
- [ ] Division: `a / b`
- [ ] Logical or: `a or b`
- [ ] Logical and: `a and b`
- [ ] Chained arithmetic: `a + b * c - d` (operator precedence)
- [ ] Parenthesized: `(a + b) * c`
- [ ] Negation: `not x`
- [ ] Signed: `-x`, `+x`
- [ ] Relational: `a == b`, `a <> b`, `a < b`, `a > b`, `a <= b`, `a >= b`

### Factors / Variables
- [ ] Integer literal: `42`
- [ ] Float literal: `3.14`
- [ ] Identifier: `x`
- [ ] Array access: `arr[i]`, `matrix[i][j]`
- [ ] Dot notation: `obj.member`
- [ ] Chained dot: `obj.member.field`
- [ ] Function call in expression: `func(a, b)`
- [ ] Method call in expression: `obj.method(a)`
- [ ] Complex expression: `a + obj.method(x) * arr[i+1]`

## Existing Tests to Reuse

Your A2 test files already cover many structures. For A3, they should now
also produce correct `.outast` files:

- `bubblesort.src` - functions, arrays, while, if, write, assignment
- `polynomial.src` - classes, inheritance, member functions, dot notation
- `simple_expressions.src` - arithmetic expressions
- `array_operations.src` - array indexing
- `class_hierarchy.src` - class with inheritance

## Tips

1. Start with simple test files (single variable declaration, single expression)
   so you can verify basic AST construction works before testing complex programs
2. Compare your .outast output against what you'd expect by hand-tracing
3. The assignment examples use a different syntax, but the AST NODE TYPES
   and TREE SHAPES are the reference (see 8.SDTAST.pdf slides 34-41)
