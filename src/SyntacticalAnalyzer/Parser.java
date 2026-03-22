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

    // A3: Semantic stack and AST root for syntax-directed translation.
    private final Deque<ASTNode> semanticStack;
    private ASTNode astRoot;
    private Token lastMatchedToken;

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

            // A3: Semantic action markers trigger AST construction on the semantic stack.
            if (top.startsWith("@")) {
                stack.pop();
                executeSemanticAction(top);
            } else if (isTerminal(top)) {
                // Top is a terminal - try to match with lookahead
                String lookaheadStr = tokenToGrammarTerminal(lookahead);
                if (top.equals(lookaheadStr)) {
                    // Match - pop and advance
                    lastMatchedToken = lookahead;
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

    // Epsilon marker object for @PUSH_EPSILON — identity-compared in popUntilEpsilon.
    // Must be distinct from @MAKE_NODE_EPSILON (real AST leaf for unsized dims).
    private static final ASTNode EPSILON_MARKER = ASTNode.makeNode("Epsilon", null, -1);

    private void executeSemanticAction(String action) {
        switch (action) {
            // === Leaf actions ===
            case "@MAKE_ID":
                semanticStack.push(ASTNode.makeNode("Id",
                        lastMatchedToken.getLexeme(), lastMatchedToken.getLine()));
                break;
            case "@MAKE_INTNUM":
                semanticStack.push(ASTNode.makeNode("IntNum",
                        lastMatchedToken.getLexeme(), lastMatchedToken.getLine()));
                break;
            case "@MAKE_FLOATNUM":
                semanticStack.push(ASTNode.makeNode("FloatNum",
                        lastMatchedToken.getLexeme(), lastMatchedToken.getLine()));
                break;
            case "@MAKE_TYPE":
                semanticStack.push(ASTNode.makeNode("Type",
                        lastMatchedToken.getLexeme(), lastMatchedToken.getLine()));
                break;
            case "@MAKE_VOID":
                semanticStack.push(ASTNode.makeNode("Void",
                        "void", lastMatchedToken.getLine()));
                break;
            case "@MAKE_VISIBILITY":
                semanticStack.push(ASTNode.makeNode("Visibility",
                        lastMatchedToken.getLexeme(), lastMatchedToken.getLine()));
                break;
            case "@MAKE_ADDOP":
                semanticStack.push(ASTNode.makeNode("AddOp",
                        lastMatchedToken.getLexeme(), lastMatchedToken.getLine()));
                break;
            case "@MAKE_MULTOP":
                semanticStack.push(ASTNode.makeNode("MultOp",
                        lastMatchedToken.getLexeme(), lastMatchedToken.getLine()));
                break;
            case "@MAKE_RELOP":
                semanticStack.push(ASTNode.makeNode("RelOp",
                        lastMatchedToken.getLexeme(), lastMatchedToken.getLine()));
                break;
            case "@MAKE_SIGN":
                semanticStack.push(ASTNode.makeNode("Sign",
                        lastMatchedToken.getLexeme(), lastMatchedToken.getLine()));
                break;
            case "@MAKE_NODE_EPSILON":
                // Composite (not leaf) so ASTPrinter won't NPE on null children
                semanticStack.push(ASTNode.makeNode("Epsilon",
                        lastMatchedToken != null ? lastMatchedToken.getLine() : 0));
                break;

            // === Epsilon Marker ===
            case "@PUSH_EPSILON":
                semanticStack.push(EPSILON_MARKER);
                break;

            // === List constructors (popUntilEpsilon) ===
            case "@MAKE_DIMLIST":     pushListNode("DimList"); break;
            case "@MAKE_INDICELIST":  pushListNode("IndiceList"); break;
            case "@MAKE_FPARAMSLIST": pushListNode("FParamsList"); break;
            case "@MAKE_APARAMSLIST": pushListNode("AParamsList"); break;
            case "@MAKE_CLASSLIST":   pushListNode("ClassList"); break;
            case "@MAKE_FUNCDEFLIST": pushListNode("FuncDefList"); break;
            case "@MAKE_INHERITLIST": pushListNode("InherList"); break;
            case "@MAKE_MEMBERLIST":  pushListNode("MemberList"); break;
            case "@MAKE_VARDECLLIST": pushListNode("VarDeclList"); break;
            case "@MAKE_STATLIST":    pushListNode("StatList"); break;
            case "@MAKE_STATBLOCK":   pushListNode("StatBlock"); break;

            // === Composite constructors ===
            case "@MAKE_PROG": {
                ASTNode funcBody = safePop();
                ASTNode funcDefList = safePop();
                ASTNode classList = safePop();
                // Flatten FuncBody(VarDeclList, StatList) into flat ProgramBlock
                ASTNode progBlock = ASTNode.makeNode("ProgramBlock", 0);
                if (funcBody.getChildren() != null) {
                    for (ASTNode subList : funcBody.getChildren()) {
                        if (subList.getChildren() != null) {
                            for (ASTNode child : subList.getChildren()) {
                                progBlock.adoptChildren(child);
                            }
                        }
                    }
                }
                semanticStack.push(ASTNode.makeFamily("Prog", 0,
                        Arrays.asList(classList, funcDefList, progBlock)));
                break;
            }
            case "@MAKE_CLASSDECL": {
                ASTNode memberList = safePop();
                ASTNode inheritList = safePop();
                ASTNode id = safePop();
                semanticStack.push(ASTNode.makeFamily("ClassDecl", id.getLineNumber(),
                        Arrays.asList(id, inheritList, memberList)));
                break;
            }
            case "@MAKE_MEMBERDECL": {
                ASTNode decl = safePop();
                ASTNode visibility = safePop();
                semanticStack.push(ASTNode.makeFamily("MemberDecl", visibility.getLineNumber(),
                        Arrays.asList(visibility, decl)));
                break;
            }
            case "@MAKE_FUNCDECL": {
                ASTNode returnType = safePop();
                ASTNode fParamsList = safePop();
                ASTNode id = safePop();
                semanticStack.push(ASTNode.makeFamily("FuncDecl", id.getLineNumber(),
                        Arrays.asList(id, fParamsList, returnType)));
                break;
            }
            case "@MAKE_FUNCDEF": {
                ASTNode funcBody = safePop();
                ASTNode funcHead = safePop();
                semanticStack.push(ASTNode.makeFamily("FuncDef", funcHead.getLineNumber(),
                        Arrays.asList(funcHead, funcBody)));
                break;
            }
            case "@MAKE_FUNCHEAD": {
                ASTNode returnType = safePop();
                ASTNode fParamsList = safePop();
                ASTNode funcName = safePop();
                ASTNode scopeSpec = safePop();
                semanticStack.push(ASTNode.makeFamily("FuncHead", scopeSpec.getLineNumber(),
                        Arrays.asList(scopeSpec, funcName, fParamsList, returnType)));
                break;
            }
            case "@MAKE_FUNCBODY": {
                ASTNode statList = safePop();
                ASTNode varDeclList = safePop();
                semanticStack.push(ASTNode.makeFamily("FuncBody", 0,
                        Arrays.asList(varDeclList, statList)));
                break;
            }
            case "@MAKE_VARDECL": {
                ASTNode dimList = safePop();
                ASTNode id = safePop();
                ASTNode type = safePop();
                semanticStack.push(ASTNode.makeFamily("VarDecl", type.getLineNumber(),
                        Arrays.asList(type, id, dimList)));
                break;
            }
            case "@MAKE_FPARAM": {
                ASTNode dimList = safePop();
                ASTNode id = safePop();
                ASTNode type = safePop();
                semanticStack.push(ASTNode.makeFamily("FParam", type.getLineNumber(),
                        Arrays.asList(type, id, dimList)));
                break;
            }
            case "@MAKE_ASSIGNSTAT": {
                ASTNode expr = safePop();
                ASTNode variable = safePop();
                semanticStack.push(ASTNode.makeFamily("AssignStat", variable.getLineNumber(),
                        Arrays.asList(variable, expr)));
                break;
            }
            case "@MAKE_IFSTAT": {
                ASTNode elseBlock = safePop();
                ASTNode thenBlock = safePop();
                ASTNode relExpr = safePop();
                semanticStack.push(ASTNode.makeFamily("IfStat", relExpr.getLineNumber(),
                        Arrays.asList(relExpr, thenBlock, elseBlock)));
                break;
            }
            case "@MAKE_WHILESTAT": {
                ASTNode statBlock = safePop();
                ASTNode relExpr = safePop();
                semanticStack.push(ASTNode.makeFamily("WhileStat", relExpr.getLineNumber(),
                        Arrays.asList(relExpr, statBlock)));
                break;
            }
            case "@MAKE_READSTAT": {
                ASTNode variable = safePop();
                semanticStack.push(ASTNode.makeFamily("ReadStat", variable.getLineNumber(),
                        Arrays.asList(variable)));
                break;
            }
            case "@MAKE_WRITESTAT": {
                ASTNode expr = safePop();
                semanticStack.push(ASTNode.makeFamily("WriteStat", expr.getLineNumber(),
                        Arrays.asList(expr)));
                break;
            }
            case "@MAKE_RETURNSTAT": {
                ASTNode expr = safePop();
                semanticStack.push(ASTNode.makeFamily("ReturnStat", expr.getLineNumber(),
                        Arrays.asList(expr)));
                break;
            }
            case "@MAKE_RELEXPR": {
                ASTNode right = safePop();
                ASTNode relOp = safePop();
                ASTNode left = safePop();
                semanticStack.push(ASTNode.makeFamily("RelExpr", left.getLineNumber(),
                        Arrays.asList(left, relOp, right)));
                break;
            }
            case "@MAKE_ADDNODE": {
                ASTNode right = safePop();
                ASTNode op = safePop();    // AddOp leaf (+/-/or)
                ASTNode left = safePop();
                ASTNode node = ASTNode.makeFamily("AddOp", op.getLineNumber(),
                        Arrays.asList(left, right));
                node.setValue(op.getValue());         // operator stored in value
                semanticStack.push(node);
                break;
            }
            case "@MAKE_MULTNODE": {
                ASTNode right = safePop();
                ASTNode op = safePop();    // MultOp leaf (*|/|and)
                ASTNode left = safePop();
                ASTNode node = ASTNode.makeFamily("MultOp", op.getLineNumber(),
                        Arrays.asList(left, right));
                node.setValue(op.getValue());
                semanticStack.push(node);
                break;
            }
            case "@MAKE_NOT": {
                ASTNode factor = safePop();
                semanticStack.push(ASTNode.makeFamily("Not", factor.getLineNumber(),
                        Arrays.asList(factor)));
                break;
            }
            case "@MAKE_SIGNFACTOR": {
                ASTNode factor = safePop();
                ASTNode sign = safePop();  // Sign leaf (+/-)
                ASTNode node = ASTNode.makeFamily("Sign", sign.getLineNumber(),
                        Arrays.asList(factor));
                node.setValue(sign.getValue());
                semanticStack.push(node);
                break;
            }
            case "@MAKE_VAR": {
                ASTNode indiceList = safePop();
                ASTNode id = safePop();
                semanticStack.push(ASTNode.makeFamily("DataMember", id.getLineNumber(),
                        Arrays.asList(id, indiceList)));
                break;
            }
            case "@MAKE_DOT": {
                ASTNode right = safePop();
                ASTNode left = safePop();
                semanticStack.push(ASTNode.makeFamily("Dot", left.getLineNumber(),
                        Arrays.asList(left, right)));
                break;
            }
            case "@MAKE_FUNCCALL": {
                ASTNode aParamsList = safePop();
                ASTNode id = safePop();
                semanticStack.push(ASTNode.makeFamily("FuncCall", id.getLineNumber(),
                        Arrays.asList(id, aParamsList)));
                break;
            }

            // === Special actions ===
            case "@PUSH_NULLSCOPE": {
                ASTNode funcName = safePop();
                // Push epsilon scope (composite, not epsilon marker) then funcName back
                semanticStack.push(ASTNode.makeNode("Epsilon", funcName.getLineNumber()));
                semanticStack.push(funcName);
                break;
            }
            case "@RETYPE_TOP_TO_TYPE": {
                if (!semanticStack.isEmpty()) {
                    semanticStack.peek().setType("Type");
                }
                break;
            }
            default:
                reportError("Unknown semantic action: " + action);
                break;
        }
    }

    /**
     * Safe pop from the semantic stack. Returns an error placeholder node
     * instead of crashing when the stack is empty (e.g., during error recovery).
     */
    private ASTNode safePop() {
        if (semanticStack.isEmpty()) {
            return ASTNode.makeNode("ErrorNode", 0);
        }
        return semanticStack.pop();
    }

    private List<ASTNode> popUntilEpsilon() {
        List<ASTNode> nodes = new ArrayList<>();
        while (!semanticStack.isEmpty() && semanticStack.peek() != EPSILON_MARKER) {
            nodes.add(semanticStack.pop());
        }
        if (!semanticStack.isEmpty()) {
            semanticStack.pop(); // pop the epsilon marker
        }
        Collections.reverse(nodes);
        return nodes;
    }

    private void pushListNode(String type) {
        List<ASTNode> children = popUntilEpsilon();
        int line = children.isEmpty() ? 0 : children.get(0).getLineNumber();
        semanticStack.push(ASTNode.makeFamily(type, line, children));
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

    private boolean isTerminal(String symbol) {
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
                        if (!symbols[j].startsWith("@")) {
                            sententialForm.add(i, symbols[j]);
                        }
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