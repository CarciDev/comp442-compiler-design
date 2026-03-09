package src.SyntacticalAnalyzer;

import src.LexicalAnalyzer.LexicalAnalyzer;

import java.io.*;

/**
 * INTEGRATION TESTS FOR A3-05 through A3-09
 * Run after augmenting the grammar and modifying the parser.
 *
 * These tests parse actual .src files through the full pipeline
 * (lexer -> parser with semantic actions -> AST) and verify the
 * AST output has the expected structure.
 *
 * HOW TO RUN:
 *   javac -d out src/LexicalAnalyzer/*.java src/SyntacticalAnalyzer/*.java \
 *                src/SyntacticalAnalyzer/tests/ASTIntegrationTest.java
 *   java -cp out src.SyntacticalAnalyzer.tests.ASTIntegrationTest
 *
 * Run these tests incrementally as you implement:
 *   1. After A3-05/06/07/08 basic wiring: testMinimalProgram
 *   2. After VarDecl actions: testVarDeclarations
 *   3. After expression actions: testSimpleAssignment, testExpressions
 *   4. After statement actions: testWriteReturn, testIfWhile
 *   5. After function actions: testFunctions
 *   6. After class actions: testClasses
 *   7. After all actions: testBubblesort, testPolynomial (full programs)
 */
public class ASTIntegrationTest {

    static int passed = 0;
    static int failed = 0;
    static final String TEST_DIR = "src/SyntacticalAnalyzer/tests/";

    public static void main(String[] args) {
        System.out.println("=== A3 Integration Tests: Parse .src -> AST ===\n");

        // Phase 1: Basic structure
        System.out.println("--- Phase 1: Basic Structure ---");
        testMinimalProgram();

        // Phase 2: Variable declarations
        System.out.println("\n--- Phase 2: Variable Declarations ---");
        testVarDeclarations();

        // Phase 3: Assignments and expressions
        System.out.println("\n--- Phase 3: Assignments & Expressions ---");
        testSimpleAssignment();
        testExpressions();

        // Phase 4: Statements
        System.out.println("\n--- Phase 4: Statements ---");
        testWriteReturn();
        testIfWhile();

        // Phase 5: Functions
        System.out.println("\n--- Phase 5: Functions ---");
        testFunctions();

        // Phase 6: Classes and dot access
        System.out.println("\n--- Phase 6: Classes & Dot Access ---");
        testClasses();
        testDotAccess();

        // Phase 7: Arrays and unary
        System.out.println("\n--- Phase 7: Arrays & Unary ---");
        testArrays();
        testNotSign();

        // Phase 8: Full programs from assignment
        System.out.println("\n--- Phase 8: Full Programs (Assignment Examples) ---");
        testBubblesort();
        testPolynomial();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    // =====================================================================
    // Phase 1: Minimal program
    // =====================================================================

    /**
     * Parse: main do end
     * Expected AST root: Prog with 3 children (ClassList, FuncDefList, ProgramBlock)
     * All three should be empty lists.
     */
    static void testMinimalProgram() {
        ASTNode root = parseFile("a3_test_minimal.src");
        if (root == null) return;

        check("Minimal - root is Prog", isType(root, "Prog"));
        check("Minimal - 3 children", root.getChildren().size() == 3);
        check("Minimal - ClassList empty", root.getChildren().get(0).getChildren().isEmpty());
        check("Minimal - FuncDefList empty", root.getChildren().get(1).getChildren().isEmpty());

        printAST(root, "a3_test_minimal");
    }

    // =====================================================================
    // Phase 2: Variable declarations
    // =====================================================================

    /**
     * Parse: main with local integer x; float y; integer arr[5]; float matrix[3][4];
     * Check that VarDecl nodes are created with correct Type, Id, DimList children.
     */
    static void testVarDeclarations() {
        ASTNode root = parseFile("a3_test_vardecl.src");
        if (root == null) return;

        ASTNode progBlock = root.getChildren().get(2); // ProgramBlock
        check("VarDecl - programBlock has 4 children", progBlock.getChildren().size() == 4);

        // First vardecl: integer x (no dims)
        ASTNode vd1 = progBlock.getChildren().get(0);
        check("VarDecl1 - is VarDecl", isType(vd1, "VarDecl"));
        check("VarDecl1 - has 3 children", vd1.getChildren().size() == 3);
        check("VarDecl1 - type is integer", hasValue(vd1.getChildren().get(0), "integer"));
        check("VarDecl1 - id is x", hasValue(vd1.getChildren().get(1), "x"));
        check("VarDecl1 - dimList empty", vd1.getChildren().get(2).getChildren().isEmpty());

        // Third vardecl: integer arr[5] (one dim)
        ASTNode vd3 = progBlock.getChildren().get(2);
        check("VarDecl3 - id is arr", hasValue(vd3.getChildren().get(1), "arr"));
        check("VarDecl3 - dimList has 1 dim", vd3.getChildren().get(2).getChildren().size() == 1);

        // Fourth vardecl: float matrix[3][4] (two dims)
        ASTNode vd4 = progBlock.getChildren().get(3);
        check("VarDecl4 - dimList has 2 dims", vd4.getChildren().get(2).getChildren().size() == 2);

        printAST(root, "a3_test_vardecl");
    }

    // =====================================================================
    // Phase 3: Assignments and expressions
    // =====================================================================

    static void testSimpleAssignment() {
        ASTNode root = parseFile("a3_test_assign_simple.src");
        if (root == null) return;

        ASTNode progBlock = root.getChildren().get(2);
        // Should have: 2 VarDecls + 2 AssignStats = 4 children
        check("SimpleAssign - 4 children in progBlock", progBlock.getChildren().size() == 4);

        // First assign: a = 1
        ASTNode assign1 = progBlock.getChildren().get(2);
        check("Assign1 - is AssignStat", isType(assign1, "AssignStat"));
        // LHS is DataMember(Id("a"), IndiceList()) — check the Id inside it
        ASTNode lhs = assign1.getChildren().get(0);
        check("Assign1 - LHS is DataMember", isType(lhs, "DataMember"));
        check("Assign1 - LHS id is a", hasValue(lhs.getChildren().get(0), "a"));
        check("Assign1 - RHS is 1", hasValue(assign1.getChildren().get(1), "1"));

        printAST(root, "a3_test_assign_simple");
    }

    /**
     * Tests operator precedence: a = b + c * d
     * MultOp should be a child of AddOp (deeper in tree).
     */
    static void testExpressions() {
        ASTNode root = parseFile("a3_test_expressions.src");
        if (root == null) return;

        ASTNode progBlock = root.getChildren().get(2);
        // 4 vardecls + 4 assignments = 8 children
        check("Expr - 8 children", progBlock.getChildren().size() == 8);

        // Third assign: a = b + c * d (index 6, after 4 vardecls + 2 assigns)
        ASTNode assign3 = progBlock.getChildren().get(6);
        check("Expr3 - is AssignStat", isType(assign3, "AssignStat"));

        ASTNode rhs = assign3.getChildren().get(1);
        check("Expr3 - RHS is AddOp", isType(rhs, "AddOp"));
        // AddOp's right child should be MultOp (b + (c*d))
        // At minimum, verify AddOp exists with nested MultOp
        boolean hasNestedMult = containsType(rhs, "MultOp");
        check("Expr3 - contains nested MultOp (precedence)", hasNestedMult);

        printAST(root, "a3_test_expressions");
    }

    // =====================================================================
    // Phase 4: Statements
    // =====================================================================

    static void testWriteReturn() {
        ASTNode root = parseFile("a3_test_write_return.src");
        if (root == null) return;

        check("WriteReturn - root is Prog", isType(root, "Prog"));
        // Should have 1 funcDef + main block
        check("WriteReturn - has funcDef", root.getChildren().get(1).getChildren().size() >= 1);

        printAST(root, "a3_test_write_return");
    }

    static void testIfWhile() {
        ASTNode root = parseFile("a3_test_if_while.src");
        if (root == null) return;

        ASTNode progBlock = root.getChildren().get(2);
        // Check for ifStat and whileStat nodes somewhere in the tree
        check("IfWhile - contains IfStat", containsType(progBlock, "IfStat"));
        check("IfWhile - contains WhileStat", containsType(progBlock, "WhileStat"));

        printAST(root, "a3_test_if_while");
    }

    // =====================================================================
    // Phase 5: Functions
    // =====================================================================

    static void testFunctions() {
        ASTNode root = parseFile("a3_test_functions.src");
        if (root == null) return;

        ASTNode funcDefList = root.getChildren().get(1);
        check("Functions - 2 FuncDefs", funcDefList.getChildren().size() == 2);

        // First function: add(integer a, integer b) : integer
        ASTNode funcDef1 = funcDefList.getChildren().get(0);
        check("FuncDef1 - is FuncDef", isType(funcDef1, "FuncDef"));

        printAST(root, "a3_test_functions");
    }

    // =====================================================================
    // Phase 6: Classes and dot access
    // =====================================================================

    static void testClasses() {
        ASTNode root = parseFile("a3_test_classes.src");
        if (root == null) return;

        ASTNode classList = root.getChildren().get(0);
        check("Classes - 2 Class nodes", classList.getChildren().size() == 2);

        printAST(root, "a3_test_classes");
    }

    static void testDotAccess() {
        ASTNode root = parseFile("a3_test_dot_access.src");
        if (root == null) return;

        ASTNode progBlock = root.getChildren().get(2);
        // Should contain dot access nodes
        check("DotAccess - contains Dot/DataMember", containsType(progBlock, "Dot")
                || containsType(progBlock, "DataMember")
                || containsType(progBlock, "FuncCall"));

        printAST(root, "a3_test_dot_access");
    }

    // =====================================================================
    // Phase 7: Arrays and unary
    // =====================================================================

    static void testArrays() {
        ASTNode root = parseFile("a3_test_arrays.src");
        if (root == null) return;

        check("Arrays - parsed successfully", root != null);
        printAST(root, "a3_test_arrays");
    }

    static void testNotSign() {
        ASTNode root = parseFile("a3_test_not_sign.src");
        if (root == null) return;

        ASTNode progBlock = root.getChildren().get(2);
        check("NotSign - contains Sign or Not",
                containsType(progBlock, "Sign") || containsType(progBlock, "Not"));

        printAST(root, "a3_test_not_sign");
    }

    // =====================================================================
    // Phase 8: Full assignment programs
    // =====================================================================

    static void testBubblesort() {
        ASTNode root = parseFile(
                "../docs_a3/assignment3.COMP442-6421.paquet.2026.4/source files/bubblesort.src");
        if (root == null) return;

        check("Bubblesort - root is Prog", isType(root, "Prog"));
        check("Bubblesort - has funcDefs",
                root.getChildren().get(1).getChildren().size() >= 2);
        check("Bubblesort - programBlock not empty",
                !root.getChildren().get(2).getChildren().isEmpty());

        printAST(root, "bubblesort");
    }

    static void testPolynomial() {
        ASTNode root = parseFile(
                "../docs_a3/assignment3.COMP442-6421.paquet.2026.4/source files/polynomial.src");
        if (root == null) return;

        check("Polynomial - root is Prog", isType(root, "Prog"));
        check("Polynomial - has classes",
                !root.getChildren().get(0).getChildren().isEmpty());
        check("Polynomial - has funcDefs",
                !root.getChildren().get(1).getChildren().isEmpty());

        printAST(root, "polynomial");
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    /**
     * Parse a .src file and return the AST root.
     * ADAPT THIS to match your Parser/ASTDriver API.
     */
    static ASTNode parseFile(String filename) {
        String path = TEST_DIR + filename;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            // Null writers to suppress derivation/error output during tests
            PrintWriter nullWriter = new PrintWriter(new StringWriter());

            LexicalAnalyzer lexer = new LexicalAnalyzer(reader);
            Parser parser = new Parser(lexer, nullWriter, nullWriter);
            parser.parse();

            return parser.getAstRoot();

        } catch (UnsupportedOperationException e) {
            check("Parse " + filename + " - NOT YET IMPLEMENTED (" + e.getMessage() + ")", false);
            return null;
        } catch (FileNotFoundException e) {
            check("Parse " + filename + " - FILE NOT FOUND", false);
            return null;
        } catch (Exception e) {
            check("Parse " + filename + " - EXCEPTION: " + e.getMessage(), false);
            e.printStackTrace();
            return null;
        }
    }

    /** Print AST to output/ directory for visual inspection */
    static void printAST(ASTNode root, String testName) {
        try {
            File outputDir = new File(TEST_DIR + "output");
            if (!outputDir.exists()) outputDir.mkdirs();

            String outPath = new File(outputDir, testName + ".outast").getPath();
            PrintWriter pw = new PrintWriter(new FileWriter(outPath));

            ASTPrinter printer = new ASTPrinter();
            // Redirect System.out to the file writer
            PrintStream oldOut = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) throws IOException { pw.write(b); }
                public void write(byte[] b, int off, int len) throws IOException {
                    pw.write(new String(b, off, len));
                }
            }));
            root.accept(printer);
            System.setOut(oldOut);

            pw.close();
            System.out.println("  -> Wrote: " + outPath);
        } catch (Exception e) {
            System.out.println("  -> Could not write AST: " + e.getMessage());
        }
    }

    static boolean isType(ASTNode node, String type) {
        return node != null && type.equalsIgnoreCase(node.getType());
    }

    static boolean hasValue(ASTNode node, String value) {
        return node != null && value.equals(node.getValue());
    }

    /** Recursively check if any node in the subtree has the given type */
    static boolean containsType(ASTNode node, String type) {
        if (node == null) return false;
        if (type.equalsIgnoreCase(node.getType())) return true;
        if (node.getChildren() == null) return false;
        for (ASTNode child : node.getChildren()) {
            if (containsType(child, type)) return true;
        }
        return false;
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
}
