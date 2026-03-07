# Assignment 3: AST Generation via Syntax-Directed Translation

**Course:** COMP 442/6421 — Compiler Design, Winter 2026
**Deadline:** Sunday March 8th, 2026
**Weight:** 8% of final grade (50 points)

---

## The Big Picture

In A1 you built a **lexer** (characters → tokens). In A2 you built a **parser** (tokens → derivation). Now in A3 you build the bridge to semantic analysis: an **Abstract Syntax Tree** (AST).

The AST is the intermediate representation that strips away all syntactic noise (semicolons, parentheses, commas, keywords like `class`, `if`, `do`, `end`) and keeps only the meaningful structure. It's what A4 (symbol tables) and A5 (code generation) will traverse.

**How it works:** You augment your existing table-driven LL(1) parser with **semantic actions** — special markers embedded in the grammar rules. When the parser encounters these markers during parsing, it builds AST nodes on a **semantic stack**. When parsing finishes, the stack holds one node: the complete program AST.

### Key Lecture References

| Topic | Primary Source | Slides |
|-------|---------------|--------|
| What is SDT and why | `6.SDT.pdf` | 2–3 |
| Semantic stack architecture | `7.SDTII.pdf` | 3 |
| Table-driven parser + semantic actions algorithm | `7.SDTII.pdf` | 4 |
| Worked parsing trace with semantic stack | `7.SDTII.pdf` | 8–9 |
| AST node design (makeNode, makeSiblings, etc.) | `8.SDTAST.pdf` | 5–10 |
| makeFamily function | `8.SDTAST.pdf` | 10 |
| General AST generation procedure | `8.SDTAST.pdf` | 16 |
| Stack-based AST generation pseudocode | `8.SDTAST.pdf` | 26–31 |
| **Complete AST structural elements (THE reference)** | `8.SDTAST.pdf` | **34–41** |
| createLeaf / createSubtree / popUntilEpsilon | `8.5.ASTgeneration.pdf` | 4–5 |
| VarDecl AST example step-by-step | `8.5.ASTgeneration.pdf` | 3–5 |
| Dot access (a.b[c].d) AST example | `8.5.ASTgeneration.pdf` | 7–12 |

All notes are in `/Downloads/COMPILER/A3/`.

---

## Grading Breakdown

| Component | Points |
|-----------|--------|
| Semantic concepts list + attribute grammar (report Section 1) | 5 |
| Design description (report Section 2) | 5 |
| Correct AST generation via syntax-directed translation | 20 |
| AST output in `.outast` file | 5 |
| Completeness of test cases | 10 |
| Tools description/justification (report Section 3) | 2 |
| Correct use of tools | 3 |
| **Total** | **50** |

---

## Implementation TODOs (in order)

### Phase 1: AST Data Structure — `ASTNode.java` (new file)

#### TODO(A3-01): Create the AST Node Class

**What:** Build the data structure for AST nodes. Each node has a type (e.g., `"varDecl"`, `"addOp"`, `"id"`), an optional value (for leaves), and a list of children.

**Why:** Every AST node — from the root `Prog` down to leaf `Id` and `Num` nodes — is an instance of this class. It's the foundation everything else builds on.

**Read:** `8.SDTAST.pdf` slides 5–7 for data structure requirements and node diagram.

**Node types needed** (from `8.SDTAST.pdf` slides 34–41):

- **Composite (internal):** `prog`, `classList`, `funcDefList`, `classDecl`, `funcDef`, `inherList`, `membList`, `fParamList`, `varDecl`, `funcDecl`, `dimList`, `statBlock`, `ifStat`, `whileStat`, `assignStat`, `getStat`, `putStat`, `returnStat`, `relExpr`, `addOp`, `multOp`, `not`, `sign`, `dot`, `dataMember`, `fCall`, `indexList`, `aParams`
- **Atomic (leaf):** `id`, `intNum`, `floatNum`, `type`, `visibility`, `relOp`, `addOp` (operator leaf), `multOp` (operator leaf), `sign` (leaf), `void`, `epsilon`

**Fields:** `String type`, `String value`, `List<ASTNode> children`, `ASTNode parent` (optional), `int lineNumber`

#### TODO(A3-02): Implement Tree-Building Methods

**What:** Factory and helper methods that semantic actions call to assemble the tree.

**Why:** During parsing, partial AST nodes live on the semantic stack. These methods are the glue that pops children, creates parents, and pushes results back.

**Read:** `8.SDTAST.pdf` slides 8–10, `8.5.ASTgeneration.pdf` slide 4.

**Methods:**

| Method | Purpose | Reference |
|--------|---------|-----------|
| `makeNode(type, value, line)` | Create a leaf node | `8.SDTAST.pdf` slide 8 |
| `makeNode(type)` | Create an empty composite node | `8.SDTAST.pdf` slide 8 |
| `adoptChildren(parent, child)` | Add child to parent | `8.SDTAST.pdf` slide 9 |
| `makeSiblings(node1, node2)` | Link as siblings | `8.SDTAST.pdf` slide 9 |
| `makeFamily(type, children...)` | Create parent + adopt all children | `8.SDTAST.pdf` slide 10 |

**Key pattern — `popUntilEpsilon`:** For variable-length lists (params, dims, statements), push an epsilon sentinel first, then items, then pop everything until epsilon to build the list. See `8.5.ASTgeneration.pdf` slide 4.

---

### Phase 2: AST Output — `Visitor.java` + `ASTPrinter.java` (new files)

#### TODO(A3-03): Create the Visitor Interface

**What:** A simple interface with a `visit(ASTNode node)` method.

**Why:** Separates tree structure from operations. A3 needs printing; A4/A5 add symbol tables and code gen as new visitors without changing ASTNode.

#### TODO(A3-04): Implement ASTPrinter (text format → `.outast`)

**What:** Depth-first traversal that prints the tree in indented text format.

**Why:** Graded deliverable (5 pts). The marker compares your `.outast` against expected output.

**Output format** (from assignment examples):
```
Prog
| ClassList
| FuncDefList
| ProgramBlock
| | VarDecl
| | | Type | integer
| | | Id | a
| | | DimList
| | AssignStat
| | | Id | a
| | | Num | 1
```

Each level indented with `| `. Leaf values shown after ` | `. Empty list nodes still appear (no children below them).

---

### Phase 3: Grammar Augmentation — `ParsingTable.java` (modify)

#### TODO(A3-05): Augment Grammar Rules with Semantic Action Markers

**What:** Insert `@ACTION_NAME` strings into the RHS of grammar rules in the `rules` array. These markers get pushed onto the parsing stack alongside grammar symbols.

**Why:** This is what makes it "syntax-**directed**" — the grammar itself specifies when to build AST nodes.

**Read:** `7.SDTII.pdf` slides 3–4, `8.SDTAST.pdf` slide 12, `8.5.ASTgeneration.pdf` slides 4, 8.

**Three patterns:**

| Pattern | Example | When |
|---------|---------|------|
| Leaf creation | `@MAKE_ID` after `id` | Terminal carries semantic meaning |
| Subtree creation | `@MAKE_VARDECL` at end of VARDECL rule | All children are on the stack |
| Epsilon sentinel | `@PUSH_EPSILON` before a repeating list | Variable-length construct starting |

**Example augmentation:**
```
// Before (rule 114):
{"VARDECL", "TYPE id REPTVARDECL2 semi"}

// After:
{"VARDECL", "@PUSH_EPSILON TYPE id @MAKE_ID REPTVARDECL2 semi @MAKE_DIMLIST @MAKE_VARDECL"}
```

**Important:** Markers must NOT appear in parsing table lookups or FIRST/FOLLOW sets. They only exist in RHS strings. Start small — augment VARDECL and FACTOR terminals first, test, then expand.

**Rules to augment by category:**

- **Program:** 52, 78–81, 96
- **Classes:** 11, 48–49, 62–63
- **Variables:** 114, 84–85, 7–9
- **Functions:** 30–35, 23–25, 26
- **Statements:** 100–105, 124, 97–99, 106–109, 123–128
- **Expressions:** 6, 90–93, 110, 15–20
- **Types:** 111–113

---

### Phase 4: Parser Modifications — `Parser.java` (modify)

#### TODO(A3-06): Add a Semantic Stack

**What:** Add a `Deque<ASTNode> semanticStack` field alongside the existing parsing stack. Also add `Token lastMatchedToken` and `ASTNode astRoot` with a `getAST()` getter.

**Why:** The parsing stack drives syntax; the semantic stack drives AST construction. They run in parallel.

**Read:** `7.SDTII.pdf` slide 3 (architecture diagram), `8.SDTAST.pdf` slide 16 (at parse end, one node remains = full AST).

#### TODO(A3-07): Add Semantic Action Detection (Third Branch in Parse Loop)

**What:** In the main `while` loop, add a check **before** the terminal/non-terminal branches: if top of stack starts with `"@"`, pop it and call `executeSemanticAction(top)`.

**Why:** This is the core mechanism from `7.SDTII.pdf` slide 4. Semantic action symbols are neither terminals nor non-terminals — they trigger AST operations when they reach the top of the stack.

**Pseudocode:**
```java
while (!stack.peek().equals("$")) {
    String top = stack.peek();

    if (top.startsWith("@")) {          // <-- NEW: semantic action
        stack.pop();
        executeSemanticAction(top);
    } else if (isTerminal(top)) {
        // ... existing terminal matching ...
        // ADD: lastMatchedToken = lookahead; before advancing
    } else {
        // ... existing non-terminal expansion ...
    }
}
// After loop: astRoot = semanticStack.pop();
```

**Also:** Save `lastMatchedToken = lookahead` before advancing in the terminal match branch, so leaf-creation actions can access the matched token's lexeme and line.

#### TODO(A3-08): Implement Semantic Action Handler Methods

**What:** A big `switch` statement mapping each `@ACTION_NAME` to its AST-building logic.

**Why:** Each action corresponds to recognizing a semantic concept. Leaf actions push one node; composite actions pop children and push a subtree.

**Read:** `8.SDTAST.pdf` slides 15–16, `8.5.ASTgeneration.pdf` slides 4–5 (step-by-step stack traces).

**Actions to implement:**

| Category | Actions |
|----------|---------|
| **Leaf** | `@MAKE_ID`, `@MAKE_INTNUM`, `@MAKE_FLOATNUM`, `@MAKE_TYPE`, `@MAKE_VISIBILITY`, `@MAKE_RELOP`, `@MAKE_ADDOP_LEAF`, `@MAKE_MULTOP_LEAF`, `@MAKE_SIGN`, `@MAKE_VOID` |
| **Sentinel** | `@PUSH_EPSILON` |
| **Lists** (popUntilEpsilon) | `@MAKE_DIMLIST`, `@MAKE_PARAMLIST`, `@MAKE_CLASSLIST`, `@MAKE_FUNCDEFLIST`, `@MAKE_INHERLIST`, `@MAKE_MEMBLIST`, `@MAKE_STATBLOCK`, `@MAKE_INDEXLIST`, `@MAKE_APARAMS` |
| **Composites** (pop fixed children) | `@MAKE_VARDECL`, `@MAKE_FUNCDECL`, `@MAKE_FUNCDEF`, `@MAKE_CLASSDECL`, `@MAKE_PROG`, `@MAKE_ASSIGNSTAT`, `@MAKE_IFSTAT`, `@MAKE_WHILESTAT`, `@MAKE_GETSTAT`, `@MAKE_PUTSTAT`, `@MAKE_RETURNSTAT`, `@MAKE_RELEXPR`, `@MAKE_ADDOP`, `@MAKE_MULTOP`, `@MAKE_NOT`, `@MAKE_SIGN_EXPR`, `@MAKE_DOT`, `@MAKE_DATAMEMBER`, `@MAKE_FCALL` |

**Tip:** Build incrementally. Start with leaf actions + `@PUSH_EPSILON` + `@MAKE_DIMLIST` + `@MAKE_VARDECL`, test with `a3_test_vardecl.src`, then expand.

---

### Phase 5: Driver — `ASTDriver.java` (new file)

#### TODO(A3-09): Create the AST Driver

**What:** A driver program that runs lexer + parser on `.src` files and outputs all files from A1/A2/A3.

**Why:** Graded deliverable. The marker runs this on test files.

**Output files per input (e.g., `test.src`):**
- `output/test.outlextokens` (A1)
- `output/test.outlexerrors` (A1)
- `output/test.outderivation` (A2)
- `output/test.outsyntaxerrors` (A2)
- `output/test.outast` **(A3 — NEW)**

Base this on `ParserDriver.java` and add the AST output step.

---

### Phase 6: Testing — `tests/` directory

#### TODO(A3-10): Create Comprehensive Test Files (10 pts)

See `tests/A3_TESTING_TODO.md` for the full checklist of structures to cover.

---

## Tests

### Test 1: `ASTNodeTest.java` — After A3-01, A3-02

**Run:**
```bash
javac -d out src/SyntacticalAnalyzer/ASTNode.java src/SyntacticalAnalyzer/tests/ASTNodeTest.java
java -cp out src.SyntacticalAnalyzer.tests.ASTNodeTest
```

**Verifies:**

| Test | What it checks |
|------|----------------|
| `testLeafNodeCreation` | Create id, intNum, floatNum, type leaves with correct type/value/line |
| `testCompositeNodeCreation` | Create varDecl node with no value, no children |
| `testEpsilonNode` | Create epsilon sentinel node |
| `testAdoptChildren` | Parent gets children in correct order |
| `testMakeSiblings` | Sibling linking works |
| `testMakeFamily` | Create addOp with 2 children; verify type and child values |
| `testPopUntilEpsilon` | Simulates stack: push epsilon, push 3 items, pop until epsilon collects all 3 in order |
| `testBuildVarDecl` | Manually build `integer x[5][3]` → VarDecl(Type, Id, DimList(Num, Num)) |
| `testBuildAssignStatWithAddOp` | Manually build `a = b + c` → AssignStat(Id, AddOp(Id, Id)) |
| `testBuildProgStructure` | Build Prog(ClassList, FuncDefList, ProgramBlock) — all empty |

### Test 2: `ASTPrinterTest.java` — After A3-03, A3-04

**Run:**
```bash
javac -d out src/SyntacticalAnalyzer/ASTNode.java \
             src/SyntacticalAnalyzer/Visitor.java \
             src/SyntacticalAnalyzer/ASTPrinter.java \
             src/SyntacticalAnalyzer/tests/ASTPrinterTest.java
java -cp out src.SyntacticalAnalyzer.tests.ASTPrinterTest
```

**Note:** You need to wire up the `printToString()` helper to call your ASTPrinter. The file has TODO comments showing the options.

**Verifies:**

| Test | What it checks |
|------|----------------|
| `testPrintSingleLeaf` | Prints `Id \| myVar` |
| `testPrintVarDecl` | Prints 5-line tree for `integer x[5]` |
| `testPrintAssignWithExpr` | Prints 7-line tree for `a = b + c * d` with correct nesting |
| `testPrintEmptyProgram` | Prints 4-line Prog with empty lists (matches example2 lines 1–4) |
| `testPrintProgramWithVarsAndAssign` | Prints 15-line tree (2 VarDecls + 1 AssignStat) |
| `testPrintNestedExpressions` | MultOp nested inside AddOp (operator precedence) |

### Test 3: `ASTIntegrationTest.java` — After A3-05 through A3-09

**Run:**
```bash
javac -d out src/LexicalAnalyzer/*.java src/SyntacticalAnalyzer/*.java \
             src/SyntacticalAnalyzer/tests/ASTIntegrationTest.java
java -cp out src.SyntacticalAnalyzer.tests.ASTIntegrationTest
```

**Note:** You need to uncomment `parser.getAST()` and ASTPrinter calls in the helpers.

**Runs in 8 incremental phases:**

| Phase | Test | Source file | What it checks |
|-------|------|-------------|----------------|
| 1 | `testMinimalProgram` | `a3_test_minimal.src` | Prog with 3 empty children |
| 2 | `testVarDeclarations` | `a3_test_vardecl.src` | 4 VarDecls with correct Type/Id/DimList; array dims count |
| 3 | `testSimpleAssignment` | `a3_test_assign_simple.src` | AssignStat LHS=id, RHS=num |
| 3 | `testExpressions` | `a3_test_expressions.src` | Operator precedence: MultOp nested inside AddOp |
| 4 | `testWriteReturn` | `a3_test_write_return.src` | PutStat and ReturnStat nodes exist; FuncDef present |
| 4 | `testIfWhile` | `a3_test_if_while.src` | IfStat and WhileStat nodes found in tree |
| 5 | `testFunctions` | `a3_test_functions.src` | 2 FuncDefs in FuncDefList |
| 6 | `testClasses` | `a3_test_classes.src` | 2 ClassDecls in ClassList |
| 6 | `testDotAccess` | `a3_test_dot_access.src` | Dot/DataMember/FCall nodes in tree |
| 7 | `testArrays` | `a3_test_arrays.src` | Parses without error |
| 7 | `testNotSign` | `a3_test_not_sign.src` | Sign or Not nodes in tree |
| 8 | `testBubblesort` | `bubblesort.src` (assignment) | Full program: funcDefs, programBlock non-empty |
| 8 | `testPolynomial` | `polynomial.src` (assignment) | Full program: classes, funcDefs, programBlock |

Each test also writes the `.outast` file to `tests/output/` for visual inspection.

---

## Source Test Files Summary

| File | Language Features Tested |
|------|------------------------|
| `a3_test_minimal.src` | Empty program (`main do end`) |
| `a3_test_vardecl.src` | `integer`, `float`, arrays with dimensions |
| `a3_test_assign_simple.src` | `a = 1; b = 2;` |
| `a3_test_expressions.src` | `+`, `*`, precedence, parenthesized |
| `a3_test_write_return.src` | `write(expr)`, `return(expr)`, function def |
| `a3_test_if_while.src` | `if/then/else`, `while`, relational operators |
| `a3_test_functions.src` | Free functions, params, function calls |
| `a3_test_classes.src` | Class decl, inheritance, member functions, member vars |
| `a3_test_arrays.src` | Array decl, indexing `arr[i]`, multi-dim |
| `a3_test_dot_access.src` | `obj.field`, `obj.method()` |
| `a3_test_not_sign.src` | `not(expr)`, `-x`, `+x` |

---

## File Map

```
src/SyntacticalAnalyzer/
├── ASTNode.java          ← NEW (A3-01, A3-02)
├── Visitor.java           ← NEW (A3-03)
├── ASTPrinter.java        ← NEW (A3-04)
├── ParsingTable.java      ← MODIFY (A3-05)
├── Parser.java            ← MODIFY (A3-06, A3-07, A3-08)
├── ASTDriver.java         ← NEW (A3-09)
├── ParserDriver.java      (unchanged)
└── tests/
    ├── A3_TESTING_TODO.md     ← checklist (A3-10)
    ├── ASTNodeTest.java       ← unit tests for Phase 1
    ├── ASTPrinterTest.java    ← unit tests for Phase 2
    ├── ASTIntegrationTest.java ← integration tests for Phases 3–8
    ├── a3_test_minimal.src
    ├── a3_test_vardecl.src
    ├── a3_test_assign_simple.src
    ├── a3_test_expressions.src
    ├── a3_test_write_return.src
    ├── a3_test_if_while.src
    ├── a3_test_functions.src
    ├── a3_test_classes.src
    ├── a3_test_arrays.src
    ├── a3_test_dot_access.src
    └── a3_test_not_sign.src

src/LexicalAnalyzer/          (unchanged from A1)
docs/assignment3/              (this guide)
```

---

## Deliverables Checklist

- [ ] **PDF Report** with 3 sections (semantic concepts, design, tools)
- [ ] **ASTNode.java** — node class with tree-building methods
- [ ] **Visitor.java** + **ASTPrinter.java** — tree printing
- [ ] **ParsingTable.java** — augmented grammar rules
- [ ] **Parser.java** — semantic stack + action processing
- [ ] **ASTDriver.java** — produces all output files
- [ ] **Test `.src` files** — covering all syntactic structures
- [ ] **`.outast` output files** — generated for each test
- [ ] All A1/A2 outputs still generated correctly
- [ ] Submission: `A3_studentid.zip`
