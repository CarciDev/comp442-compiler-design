package src.SyntacticalAnalyzer;

import src.LexicalAnalyzer.LexicalAnalyzer;
import src.LexicalAnalyzer.Token;

import java.io.*;
import java.util.*;

/**
 * Table-driven LL(1) predictive parser with panic-mode error recovery.
 *
 * Uses the parsing table generated from the transformed LL(1) grammar,
 * verified by UCalgary CFG Checker and grammartool FIRST/FOLLOW sets.
 *
 * Algorithm: Slide set 5, slide 23 (Prof. Paquet)
 * Error recovery: Slide set 5, slide 20 (panic mode with FIRST/FOLLOW)
 * Derivation output: Leftmost derivation as sentential forms
 */
public class Parser {

    private final LexicalAnalyzer lexer;
    private Token lookahead;
    private boolean success;

    // Output writers
    private final PrintWriter derivationWriter;
    private final PrintWriter errorWriter;

    // Parsing stack
    private final Deque<String> stack;

    // =========================================================================
    // TODO(A3-06): ADD A SEMANTIC STACK
    // =========================================================================
    //
    // WHAT: Add a second stack (Deque<ASTNode>) called "semanticStack" that
    //       operates alongside the existing parsing stack. This stack holds
    //       partially-built AST nodes during parsing.
    //
    // WHY:  The parsing stack drives syntax analysis (terminals & non-terminals).
    //       The semantic stack drives AST construction (ASTNode objects).
    //       They work in tandem: when the parser matches a meaningful terminal
    //       or processes a semantic action marker, it pushes/pops ASTNodes on
    //       the semantic stack.
    //       See: 7.SDTII.pdf slide 3 (architecture diagram with both stacks)
    //            8.SDTAST.pdf slides 26-31 (stack-based AST generation)
    //
    // HOW IT CONNECTS:
    //   - Semantic action markers in the grammar (A3-05) trigger operations
    //     on this stack
    //   - When parsing finishes, the semantic stack should contain exactly
    //     ONE node: the root "Prog" node = your complete AST
    //     See: 8.SDTAST.pdf slide 16
    //
    // ALSO ADD:
    //   - A field to store the AST root after parsing (e.g., ASTNode astRoot)
    //   - A getter method getAST() so the driver can retrieve it
    //   - Store the most recently matched Token so semantic actions can access
    //     its lexeme and line number (e.g., Token lastMatchedToken)
    //
    // =========================================================================

    private final Deque<ASTNode> semanticStack; //semantic stack
    private ASTNode astRoot; //root of AST-Tree
    private Token lastMatchedToken; //to keep track of line number.

    // Derivation tracking - maintains the current sentential form
    private final List<String> sententialForm;

    // Error count
    private int errorCount;

    public Parser(LexicalAnalyzer lexer, PrintWriter derivationWriter, PrintWriter errorWriter) {
        this.lexer = lexer;
        this.derivationWriter = derivationWriter;
        this.errorWriter = errorWriter;
        this.stack = new ArrayDeque<>();
        this.semanticStack = new ArrayDeque<>();
        this.sententialForm = new ArrayList<>();
        this.success = true;
        this.errorCount = 0;
    }

    /**
     * Main parse method. Implements the table-driven parsing algorithm.
     * Reference: Slide set 5, slide 23
     *
     * @return true if the input is syntactically valid
     */
    public boolean parse() {
        // Initialize stack with $ and start symbol
        stack.push("$");
        stack.push("START");

        // Initialize sentential form
        sententialForm.add("START");

        // Get first token (skip comments)
        lookahead = nextValidToken();

        while (!stack.peek().equals("$")) {
            String top = stack.peek();

            // =================================================================
            // TODO(A3-07): ADD SEMANTIC ACTION DETECTION (THIRD BRANCH)
            // =================================================================
            //
            // WHAT: Add a check BEFORE the terminal/non-terminal branches:
            //       if the top of the parsing stack starts with "@", it's a
            //       semantic action marker — pop it and execute the action.
            //
            // WHY:  This is the core mechanism of syntax-directed translation
            //       in a table-driven parser. Semantic action symbols are
            //       pushed onto the parsing stack alongside grammar symbols
            //       (from the augmented rules in A3-05). When they reach the
            //       top, they trigger AST construction on the semantic stack.
            //       See: 7.SDTII.pdf slide 4 (the complete algorithm pseudocode)
            //
            // PSEUDOCODE (from 7.SDTII.pdf slide 4):
            //   if top starts with "@":
            //       stack.pop()
            //       executeSemanticAction(top)  // see A3-08
            //   else if isTerminal(top):
            //       ... existing terminal matching code ...
            //   else:
            //       ... existing non-terminal expansion code ...
            //
            // IMPORTANT: This branch goes FIRST because "@" symbols are
            //   neither terminals nor non-terminals. If you don't check for
            //   them, isTerminal() would return true (since "@MAKE_VARDECL"
            //   isn't in the parsing table), and the parser would try to
            //   match it against the input token and fail.
            //
            // ALSO: When matching terminals below (the "Match - pop and
            //   advance" section), save the matched token before advancing:
            //     lastMatchedToken = lookahead;  // save for semantic actions
            //   Semantic actions for leaf nodes (like @MAKE_ID) need to know
            //   WHICH token was just matched to create the correct AST leaf.
            //
            // =================================================================

            if (isTerminal(top)) {
                // Top is a terminal - try to match with lookahead
                String lookaheadStr = tokenToGrammarTerminal(lookahead);
                if (top.equals(lookaheadStr)) {
                    // Match - pop and advance
                    stack.pop();
                    lookahead = nextValidToken();
                } else {
                    // Terminal mismatch error
                    reportError("Syntax error at line " + lookahead.getLine()
                            + ": expected '" + top + "', found '" + lookahead.getLexeme() + "'");
                    // Pop the expected terminal (the grammar expected it but it's missing)
                    // Don't advance the input - the current lookahead may be valid for the next stack symbol
                    stack.pop();
                    success = false;
                }
            } else {
                // Top is a non-terminal - look up parsing table
                String lookaheadStr = tokenToGrammarTerminal(lookahead);
                int ruleIndex = ParsingTable.lookup(top, lookaheadStr);

                if (ruleIndex != -1) {
                    // Found a rule - apply it
                    stack.pop();
                    String lhs = ParsingTable.rules[ruleIndex][0];
                    String rhs = ParsingTable.rules[ruleIndex][1];

                    // Push RHS in reverse order (skip for EPSILON)
                    if (!rhs.equals("EPSILON")) {
                        String[] symbols = rhs.split(" ");
                        for (int i = symbols.length - 1; i >= 0; i--) {
                            stack.push(symbols[i]);
                        }
                    }

                    // Update and output derivation
                    updateDerivation(lhs, rhs);
                    outputDerivation();
                } else {
                    // Error - empty cell in parsing table
                    skipError();
                    success = false;
                }
            }
        }

        // Check if input is fully consumed
        String lookaheadStr = tokenToGrammarTerminal(lookahead);
        if (!lookaheadStr.equals("$")) {
            reportError("Syntax error at line " + lookahead.getLine()
                    + ": unexpected tokens after end of program");
            success = false;
        }
        if (!semanticStack.isEmpty()) {
            astRoot = semanticStack.pop();
        }
        return success;
    }

    /**
     * Panic-mode error recovery for table-driven parser.
     * Reference: Slide set 5, slide 20
     *
     * When an error occurs (empty cell in TT[A, t]):
     * - If lookahead is $ or in FOLLOW(top): pop (treat as A -> epsilon)
     * - Otherwise: scan tokens until we find one in FIRST(top) or
     *   (epsilon in FIRST(top) and lookahead in FOLLOW(top))
     */
    private void skipError() {
        String top = stack.peek();
        String lookaheadStr = tokenToGrammarTerminal(lookahead);

        reportError("Syntax error at line " + lookahead.getLine()
                + ": unexpected '" + lookahead.getLexeme() + "' while parsing " + top);

        Set<String> followSet = ParsingTable.followSets.getOrDefault(top, Collections.emptySet());
        Set<String> firstSet = ParsingTable.firstSets.getOrDefault(top, Collections.emptySet());

        if (lookaheadStr.equals("$") || followSet.contains(lookaheadStr)) {
            // Pop - treat as A -> epsilon
            stack.pop();
        } else {
            // Scan - skip tokens until we can resume
            while (!lookaheadStr.equals("$")
                    && !firstSet.contains(lookaheadStr)
                    && !(firstSet.contains("EPSILON") && followSet.contains(lookaheadStr))) {
                lookahead = nextValidToken();
                lookaheadStr = tokenToGrammarTerminal(lookahead);
            }
            // If we landed on a FOLLOW token and epsilon is in FIRST, pop the non-terminal
            if (firstSet.contains("EPSILON") && followSet.contains(lookaheadStr)) {
                stack.pop();
            } else if (lookaheadStr.equals("$")) {
                // Reached end of input during scan - pop the non-terminal to avoid infinite loop
                stack.pop();
            }
            // Otherwise, we landed on a FIRST token - leave it and let the main loop retry
        }
    }

    /**
     * Get the next token from the lexer, skipping comments and error tokens.
     */
    private Token nextValidToken() {
        Token token = lexer.nextToken();
        while (token.getType() == Token.TokenType.BLOCKCMT
                || token.getType() == Token.TokenType.INLINECMT
                || token.isError()) {
            if (token.isError()) {
                // Report lexical errors but keep parsing
                reportError("Lexical error at line " + token.getLine()
                        + ": " + token.getTokenTypeName() + " '" + token.getLexeme() + "'");
            }
            token = lexer.nextToken();
        }
        return token;
    }

    /**
     * Map a Token to its grammar terminal name.
     * Uses toFlaciString() which is 1:1 with the parsing table terminal names.
     */
    private String tokenToGrammarTerminal(Token token) {
        return token.toFlaciString();
    }

    // =========================================================================
    // TODO(A3-08): IMPLEMENT SEMANTIC ACTION HANDLER METHODS
    // =========================================================================
    //
    // WHAT: Create a method executeSemanticAction(String action) that maps
    //       each "@ACTION_NAME" to its corresponding AST-building logic.
    //       Each handler pops nodes from the semantic stack, assembles them,
    //       and pushes the result back.
    //
    // WHY:  Each semantic action corresponds to recognizing a "semantic
    //       concept" — either atomic (leaf) or composite (subtree).
    //       See: 8.SDTAST.pdf slides 15-16 (semantic concepts -> AST nodes)
    //            8.5.ASTgeneration.pdf slides 4-5 (concrete createLeaf/
    //            createSubtree examples with step-by-step stack traces)
    //
    // HOW IT CONNECTS:
    //   Called from the "@" branch you added in A3-07. Uses the semantic
    //   stack from A3-06 and the ASTNode methods from A3-01/02.
    //
    // IMPLEMENTATION PATTERN:
    //   void executeSemanticAction(String action) {
    //       switch (action) {
    //           case "@MAKE_ID":
    //               // Leaf: push an Id node using the last matched token
    //               semanticStack.push(ASTNode.makeNode("Id",
    //                   lastMatchedToken.getLexeme(),
    //                   lastMatchedToken.getLine()));
    //               break;
    //
    //           case "@MAKE_INTNUM":
    //               // Leaf: push an IntNum node (e.g., array dim sizes, integer literals)
    //               semanticStack.push(ASTNode.makeNode("IntNum",
    //                   lastMatchedToken.getLexeme(),
    //                   lastMatchedToken.getLine()));
    //               break;
    //
    //           case "@MAKE_FLOATNUM":
    //               // Leaf: push a FloatNum node
    //               semanticStack.push(ASTNode.makeNode("FloatNum",
    //                   lastMatchedToken.getLexeme(),
    //                   lastMatchedToken.getLine()));
    //               break;
    //
    //           case "@MAKE_TYPE":
    //               semanticStack.push(ASTNode.makeNode("Type",
    //                   lastMatchedToken.getLexeme(),
    //                   lastMatchedToken.getLine()));
    //               break;
    //
    //           case "@MAKE_NODE_EPSILON":
    //               // Leaf: push an Epsilon node (e.g., unsized array dim [])
    //               semanticStack.push(ASTNode.makeNode("Epsilon"));
    //               break;
    //
    //           case "@PUSH_EPSILON":
    //               // Push sentinel marker for variable-length lists
    //               semanticStack.push(ASTNode.makeNode("Epsilon"));
    //               break;
    //
    //           case "@MAKE_DIMLIST":
    //               // Pop until Epsilon, collect into a DimList
    //               List<ASTNode> dims = popUntilEpsilon();
    //               semanticStack.push(ASTNode.makeFamily("DimList",
    //                   dims.toArray(new ASTNode[0])));
    //               break;
    //
    //           case "@MAKE_VARDECL":
    //               // Pop: DimList, Id, Type (reverse order from how they were pushed!)
    //               ASTNode dl = semanticStack.pop();    // DimList
    //               ASTNode vid = semanticStack.pop();   // Id
    //               ASTNode vtype = semanticStack.pop(); // Type
    //               semanticStack.push(
    //                   ASTNode.makeFamily("VarDecl", vtype, vid, dl));
    //               break;
    //
    //           case "@MAKE_ADDNODE":
    //               // Ternary: pop right, addOp, left -> addOp becomes parent
    //               ASTNode addRight = semanticStack.pop();  // right TERM
    //               ASTNode addOp = semanticStack.pop();     // AddOp leaf
    //               ASTNode addLeft = semanticStack.pop();   // left TERM
    //               semanticStack.push(
    //                   ASTNode.makeFamily("AddOp", addLeft, addOp, addRight));
    //               break;
    //
    //           // ... similar for all other actions (see full list below)
    //       }
    //   }
    //
    // HELPER METHOD - popUntilEpsilon():
    //   Pops ASTNodes from the semantic stack until an epsilon sentinel is
    //   found. Returns the collected nodes in correct order (reversed from
    //   pop order). This handles variable-length constructs like:
    //     - DimList, IndiceList, FParamsList, AParamsList
    //     - ClassList, FuncDefList, InheritList, MemberList
    //     - VarDeclList, StatList, StatBlock
    //   See: 8.5.ASTgeneration.pdf slide 4 ("popuntile" operation)
    //
    // =====================================================================
    // FULL LIST OF SEMANTIC ACTIONS TO IMPLEMENT
    // (matches the markers in ParsingTable.java A3-05)
    // =====================================================================
    //
    // --- Leaf actions (push one node from lastMatchedToken) ---
    //   @MAKE_ID            -> Id leaf (identifier)
    //   @MAKE_INTNUM        -> IntNum leaf (integer literal)
    //   @MAKE_FLOATNUM      -> FloatNum leaf (float literal)
    //   @MAKE_TYPE          -> Type leaf ("integer", "float", or class id)
    //   @MAKE_VOID          -> Void leaf ("void" return type)
    //   @MAKE_VISIBILITY    -> Visibility leaf ("public" or "private")
    //   @MAKE_ADDOP         -> AddOp leaf (+/-/or) from lastMatchedToken
    //   @MAKE_MULTOP        -> MultOp leaf (*/ / /and) from lastMatchedToken
    //   @MAKE_RELOP         -> RelOp leaf (eq/neq/lt/gt/leq/geq)
    //   @MAKE_SIGN          -> Sign leaf ("+" or "-")
    //   @MAKE_NODE_EPSILON  -> Epsilon leaf (e.g., unsized array dim [])
    //
    // --- Sentinel ---
    //   @PUSH_EPSILON       -> push Epsilon marker for list boundaries
    //
    // --- List constructors (popUntilEpsilon) ---
    //   @MAKE_DIMLIST       -> DimList(intNum|epsilon*)
    //   @MAKE_INDICELIST    -> IndiceList(arithExpr*)
    //   @MAKE_FPARAMSLIST   -> FParamsList(fParam*)
    //   @MAKE_APARAMSLIST   -> AParamsList(expr*)
    //   @MAKE_CLASSLIST     -> ClassList(classDecl*)
    //   @MAKE_FUNCDEFLIST   -> FuncDefList(funcDef*)
    //   @MAKE_INHERITLIST   -> InheritList(id*)
    //   @MAKE_MEMBERLIST    -> MemberList(memberDecl*)
    //   @MAKE_VARDECLLIST   -> VarDeclList(varDecl*)
    //   @MAKE_STATLIST      -> StatList(statement*)
    //   @MAKE_STATBLOCK     -> StatBlock(statement*)
    //
    // --- Composite constructors (pop fixed children) ---
    //   @MAKE_PROG          -> pop 3: FuncBody(main), FuncDefList, ClassList
    //   @MAKE_CLASSDECL     -> pop 3: MemberList, InheritList, Id (className)
    //   @MAKE_MEMBERDECL    -> pop 2: declaration (VarDecl/FuncDecl), Visibility
    //   @MAKE_FUNCDECL      -> pop 3: Type/Void (return), FParamsList, Id (funcName)
    //   @MAKE_FUNCDEF       -> pop 2: FuncBody, FuncHead
    //   @MAKE_FUNCHEAD      -> pop 4: Type/Void (return), FParamsList, Id (funcName), Id/Epsilon (scope)
    //   @MAKE_FUNCBODY      -> pop 2: StatList, VarDeclList
    //   @MAKE_VARDECL       -> pop 3: DimList, Id, Type
    //   @MAKE_FPARAM        -> pop 3: DimList, Id, Type
    //   @MAKE_ASSIGNSTAT    -> pop 2: Expr, Variable
    //   @MAKE_IFSTAT        -> pop 3: StatBlock (else), StatBlock (then), RelExpr
    //   @MAKE_WHILESTAT     -> pop 2: StatBlock, RelExpr
    //   @MAKE_READSTAT      -> pop 1: Variable
    //   @MAKE_WRITESTAT     -> pop 1: Expr
    //   @MAKE_RETURNSTAT    -> pop 1: Expr
    //   @MAKE_RELEXPR       -> pop 3: right ArithExpr, RelOp, left ArithExpr
    //   @MAKE_ADDNODE       -> pop 3: right Term, AddOp leaf, left Term
    //   @MAKE_MULTNODE      -> pop 3: right Factor, MultOp leaf, left Factor
    //   @MAKE_NOT           -> pop 1: Factor -> not(Factor)
    //   @MAKE_SIGNFACTOR    -> pop 2: Factor, Sign -> sign(Factor)
    //   @MAKE_VAR           -> pop 2: IndiceList, Id -> var(Id, IndiceList)
    //   @MAKE_DOT           -> pop 2: right (Var/FuncCall), left -> dot(left, right)
    //   @MAKE_FUNCCALL      -> pop 2: AParamsList, Id -> funcCall(Id, AParamsList)
    //
    // --- Special actions ---
    //   @PUSH_NULLSCOPE     -> for free functions: pop Id (funcName),
    //                          push Epsilon (null scope), push Id back
    //   @RETYPE_TOP_TO_TYPE -> change top-of-stack node type from "Id" to "Type"
    //                          (used in MEMBERDECL when id is actually a class type)
    //
    // TIP: Build and test incrementally! Start with:
    //   1. @MAKE_ID, @MAKE_INTNUM, @MAKE_FLOATNUM, @MAKE_TYPE (leaf nodes)
    //   2. @PUSH_EPSILON, @MAKE_DIMLIST (list handling)
    //   3. @MAKE_VARDECL (first composite)
    //   4. Then add expressions, statements, classes, etc.
    //
    // =========================================================================

    /**
     * Check if a symbol is a terminal (not a non-terminal).
     * Non-terminals are UPPERCASE entries in the parsing table.
     */
    private boolean isTerminal(String symbol) {
        // NOTE: After implementing A3-07, you may want to also exclude
        // semantic action markers (starting with "@") here, OR handle
        // them before this method is ever called.
        return !ParsingTable.table.containsKey(symbol);
    }

    /**
     * Update the sentential form by replacing the leftmost occurrence of lhs
     * with the rhs symbols.
     */
    private void updateDerivation(String lhs, String rhs) {
        // Find the leftmost occurrence of lhs in the sentential form
        for (int i = 0; i < sententialForm.size(); i++) {
            if (sententialForm.get(i).equals(lhs)) {
                sententialForm.remove(i);
                if (!rhs.equals("EPSILON")) {
                    String[] symbols = rhs.split(" ");
                    for (int j = symbols.length - 1; j >= 0; j--) {
                        sententialForm.add(i, symbols[j]);
                    }
                }
                return;
            }
        }
    }

    /**
     * Output the current sentential form as one line of the derivation.
     */
    private void outputDerivation() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sententialForm.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(sententialForm.get(i));
        }
        derivationWriter.println(sb.toString());
    }

    /**
     * Report a syntax error.
     */
    private void reportError(String message) {
        errorWriter.println(message);
        errorCount++;
    }

    /**
     * Get the number of errors found during parsing.
     */
    public int getErrorCount() {
        return errorCount;
    }

    public ASTNode getAstRoot() {
        return astRoot;
    }

}