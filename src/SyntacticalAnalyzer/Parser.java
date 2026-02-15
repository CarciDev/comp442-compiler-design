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

    // Derivation tracking - maintains the current sentential form
    private final List<String> sententialForm;

    // Error count
    private int errorCount;

    public Parser(LexicalAnalyzer lexer, PrintWriter derivationWriter, PrintWriter errorWriter) {
        this.lexer = lexer;
        this.derivationWriter = derivationWriter;
        this.errorWriter = errorWriter;
        this.stack = new ArrayDeque<>();
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

    /**
     * Check if a symbol is a terminal (not a non-terminal).
     * Non-terminals are UPPERCASE entries in the parsing table.
     */
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
}