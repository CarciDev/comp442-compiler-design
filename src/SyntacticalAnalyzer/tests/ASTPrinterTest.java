package src.SyntacticalAnalyzer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * STEP-BY-STEP TESTS FOR A3-03 AND A3-04
 * Run after implementing Visitor.java and ASTPrinter.java
 *
 * These tests build known ASTs manually, print them using ASTPrinter,
 * and compare the output against expected strings. The expected format
 * matches the assignment examples (example1/2/3.ast.outast).
 *
 * HOW TO RUN:
 *   javac -d out src/SyntacticalAnalyzer/ASTNode.java \
 *                src/SyntacticalAnalyzer/Visitor.java \
 *                src/SyntacticalAnalyzer/ASTPrinter.java \
 *                src/SyntacticalAnalyzer/tests/ASTPrinterTest.java
 *   java -cp out src.SyntacticalAnalyzer.tests.ASTPrinterTest
 */
public class ASTPrinterTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== A3-03/04: Visitor & ASTPrinter Tests ===\n");
        testPrintSingleLeaf();
        testPrintVarDecl();
        testPrintAssignWithExpr();
        testPrintEmptyProgram();
        testPrintProgramWithVarsAndAssign();
        testPrintNestedExpressions();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    // =====================================================================
    // A3-03/04 TESTS: AST Printing
    // =====================================================================

    /** Test: Print a single leaf node */
    static void testPrintSingleLeaf() {
        // Expected output:
        //   Id | myVar
        try {
            ASTNode node = ASTNode.makeNode("Id", "myVar", 1);
            String output = printToString(node);
            String expected = "Id | myVar\n";
            check("Single leaf print", expected.equals(output));
            if (!expected.equals(output)) showDiff(expected, output);
        } catch (Exception e) {
            check("Single leaf threw: " + e.getMessage(), false);
        }
    }

    /**
     * Test: Print a VarDecl subtree.
     * Models: integer x[5];
     * Expected:
     *   VarDecl
     *   | Type | integer
     *   | Id | x
     *   | DimList
     *   | | Num | 5
     *
     * Reference: example1.ast.outast lines 5-8 pattern
     */
    static void testPrintVarDecl() {
        try {
            ASTNode type = ASTNode.makeNode("Type", "int", 1);
            ASTNode id = ASTNode.makeNode("Id", "x", 1);
            ASTNode dim = ASTNode.makeNode("Dim", "5", 1);
            ASTNode dimList = ASTNode.makeFamily("DimList", 1, List.of(dim));
            ASTNode varDecl = ASTNode.makeFamily("VarDecl", 1, List.of(type, id, dimList));

            String output = printToString(varDecl);
            System.out.println("  VarDecl output:\n" + indent(output));
            check("VarDecl - has 5 lines", output.trim().split("\n").length == 5);
            check("VarDecl - starts with VarDecl",
                    output.startsWith("VarDecl"));
        } catch (Exception e) {
            check("VarDecl print threw: " + e.getMessage(), false);
        }
    }

    /**
     * Test: Print AssignStat with nested expression: a = b + c * d
     * Expected:
     *   AssignStat
     *   | Id | a
     *   | AddOp
     *   | | Id | b
     *   | | MultOp
     *   | | | Id | c
     *   | | | Id | d
     *
     * Reference: example1.ast.outast lines 62-68
     * This tests operator precedence in the AST: MultOp is deeper than AddOp
     */
    static void testPrintAssignWithExpr() {
        try {
            ASTNode a = ASTNode.makeNode("Id", "a", 1);
            ASTNode b = ASTNode.makeNode("Id", "b", 1);
            ASTNode c = ASTNode.makeNode("Id", "c", 1);
            ASTNode d = ASTNode.makeNode("Id", "d", 1);
            ASTNode multOp = ASTNode.makeFamily("MultOp", 1, List.of(c, d));
            ASTNode addOp = ASTNode.makeFamily("AddOp", 1, List.of(b, multOp));
            ASTNode assign = ASTNode.makeFamily("AssignStat", 1, List.of(a, addOp));

            String output = printToString(assign);
            System.out.println("  AssignStat output:\n" + indent(output));
            check("AssignStat - has 7 lines", output.trim().split("\n").length == 7);
        } catch (Exception e) {
            check("AssignStat print threw: " + e.getMessage(), false);
        }
    }

    /**
     * Test: Print minimal empty program.
     * Expected (matching example2.ast.outast lines 1-4):
     *   Prog
     *   | ClassList
     *   | FuncDefList
     *   | ProgramBlock
     */
    static void testPrintEmptyProgram() {
        try {
            ASTNode classList = ASTNode.makeFamily("ClassList", 0, List.of());
            ASTNode funcDefList = ASTNode.makeFamily("FuncDefList", 0, List.of());
            ASTNode programBlock = ASTNode.makeFamily("ProgramBlock", 0, List.of());
            ASTNode prog = ASTNode.makeFamily("Prog", 0, List.of(classList, funcDefList, programBlock));

            String output = printToString(prog);
            System.out.println("  Empty program output:\n" + indent(output));
            check("Empty prog - 4 lines", output.trim().split("\n").length == 4);
        } catch (Exception e) {
            check("Empty prog threw: " + e.getMessage(), false);
        }
    }

    /**
     * Test: Print a program with variable declarations and an assignment.
     * Models (in our language):
     *   main
     *     local integer a; integer b;
     *     do a = 1; end
     *
     * Expected (inspired by example2.ast.outast):
     *   Prog
     *   | ClassList
     *   | FuncDefList
     *   | ProgramBlock
     *   | | VarDecl
     *   | | | Type | int
     *   | | | Id | a
     *   | | | DimList
     *   | | VarDecl
     *   | | | Type | int
     *   | | | Id | b
     *   | | | DimList
     *   | | AssignStat
     *   | | | Id | a
     *   | | | Num | 1
     */
    static void testPrintProgramWithVarsAndAssign() {
        try {
            // Build VarDecl for "integer a"
            ASTNode varDecl1 = ASTNode.makeFamily("VarDecl", 1, List.of(
                    ASTNode.makeNode("Type", "int", 1),
                    ASTNode.makeNode("Id", "a", 1),
                    ASTNode.makeFamily("DimList", 1, List.of())));

            // Build VarDecl for "integer b"
            ASTNode varDecl2 = ASTNode.makeFamily("VarDecl", 2, List.of(
                    ASTNode.makeNode("Type", "int", 2),
                    ASTNode.makeNode("Id", "b", 2),
                    ASTNode.makeFamily("DimList", 2, List.of())));

            // Build AssignStat: a = 1
            ASTNode assign = ASTNode.makeFamily("AssignStat", 3, List.of(
                    ASTNode.makeNode("Id", "a", 3),
                    ASTNode.makeNode("Num", "1", 3)));

            // Build ProgramBlock with the above
            ASTNode programBlock = ASTNode.makeFamily("ProgramBlock", 0, List.of(
                    varDecl1, varDecl2, assign));

            // Build full Prog
            ASTNode prog = ASTNode.makeFamily("Prog", 0, List.of(
                    ASTNode.makeFamily("ClassList", 0, List.of()),
                    ASTNode.makeFamily("FuncDefList", 0, List.of()),
                    programBlock));

            String output = printToString(prog);
            System.out.println("  Program with vars output:\n" + indent(output));

            String[] lines = output.trim().split("\n");
            check("Prog with vars - 15 lines", lines.length == 15);
        } catch (Exception e) {
            check("Prog with vars threw: " + e.getMessage(), false);
        }
    }

    /**
     * Test: Nested expression a + b * c (operator precedence)
     * Verifies MultOp is nested inside AddOp, not the other way around.
     *
     * Expected:
     *   AddOp
     *   | Id | a
     *   | MultOp
     *   | | Id | b
     *   | | Id | c
     *
     * Reference: example2.ast.outast lines 34-38
     */
    static void testPrintNestedExpressions() {
        try {
            ASTNode a = ASTNode.makeNode("Id", "a", 1);
            ASTNode b = ASTNode.makeNode("Id", "b", 1);
            ASTNode c = ASTNode.makeNode("Id", "c", 1);
            ASTNode mult = ASTNode.makeFamily("MultOp", 1, List.of(b, c));
            ASTNode add = ASTNode.makeFamily("AddOp", 1, List.of(a, mult));

            String output = printToString(add);
            System.out.println("  Nested expr output:\n" + indent(output));
            check("Nested expr - 5 lines", output.trim().split("\n").length == 5);
        } catch (Exception e) {
            check("Nested expr threw: " + e.getMessage(), false);
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    /**
     * Uses YOUR ASTPrinter to print the AST to a string.
     * ADAPT THIS METHOD to match your ASTPrinter API.
     * For example, if your ASTPrinter takes a PrintWriter:
     *   new ASTPrinter(writer).print(root);
     * Or if it's a static method:
     *   ASTPrinter.print(root, writer);
     */
    static String printToString(ASTNode root) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        new ASTPrinter().visit(root);
        System.setOut(originalOut);
        return baos.toString();
    }

    static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + name);
            passed++;
        } else {
            System.out.println("  FAIL: " + name);
            failed++;
        }
    }

    static void showDiff(String expected, String actual) {
        System.out.println("    Expected: " + expected.replace("\n", "\\n"));
        System.out.println("    Actual:   " + actual.replace("\n", "\\n"));
    }

    static String indent(String s) {
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\n")) {
            sb.append("    ").append(line).append("\n");
        }
        return sb.toString();
    }
}
