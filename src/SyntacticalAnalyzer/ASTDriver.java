package src.SyntacticalAnalyzer;

import src.LexicalAnalyzer.LexicalAnalyzer;

import java.io.*;

// =============================================================================
// TODO(A3-09): CREATE THE AST DRIVER
// =============================================================================
//
// WHAT: A driver program (named "ASTdriver" in the assignment spec) that:
//       1. Takes .src file(s) as input
//       2. Runs the lexer (producing .outlextokens, .outlexerrors from A1)
//       3. Runs the parser (producing .outderivation, .outsyntaxerrors from A2)
//       4. Retrieves the AST from the parser and writes it to .outast (NEW)
//
// WHY:  This is a graded deliverable. The marker will run your ASTdriver on
//       test files and check that all output files are produced correctly.
//       The driver must produce ALL previous output files too (A1 + A2).
//
// HOW IT CONNECTS:
//   - Reuses LexicalAnalyzer from A1 (no changes needed)
//   - Uses the augmented Parser from A3-06/07/08 (which now builds an AST)
//   - Calls ASTPrinter (A3-04) to write the .outast file
//   - You can base this on the existing ParserDriver.java and extend it
//
// IMPLEMENTATION:
//   1. Copy the structure of ParserDriver.processFile()
//   2. After parser.parse(), call parser.getAST() to get the root ASTNode
//   3. Create a PrintWriter for baseName + ".outast"
//   4. Use ASTPrinter to traverse the AST and write to the .outast file
//   5. Print success/failure info to console
//
// OUTPUT FILES PER INPUT (e.g., for "test.src"):
//   output/test.outlextokens       (from A1)
//   output/test.outlexerrors        (from A1)
//   output/test.outderivation      (from A2)
//   output/test.outsyntaxerrors     (from A2)
//   output/test.outast             (NEW in A3)
//
// NOTE: You can either create this as a new driver class, or modify
//   ParserDriver to add AST output. Either approach works. A new class
//   keeps A2 untouched, which is cleaner.
//
// =============================================================================

public class ASTDriver {

    public static void main(String[] args) {
        // Implement here - similar structure to ParserDriver
    }

    public static void processFile(String filename) {
        // Implement here
    }
}
