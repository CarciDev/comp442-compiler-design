package src.SyntacticalAnalyzer;

import java.util.List;

/**
 * STEP-BY-STEP TESTS FOR A3-01 AND A3-02
 * Run after implementing ASTNode.java
 *
 * These tests verify you can create leaf nodes, composite nodes,
 * and build tree structures using makeNode, makeFamily, makeSiblings,
 * adoptChildren, and the epsilon/popUntilEpsilon pattern.
 *
 * HOW TO RUN:
 *   javac -d out src/SyntacticalAnalyzer/ASTNode.java src/SyntacticalAnalyzer/tests/ASTNodeTest.java
 *   java -cp out src.SyntacticalAnalyzer.tests.ASTNodeTest
 *
 * Each test prints PASS or FAIL. Fix failures before moving on.
 */
public class ASTNodeTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== A3-01: ASTNode Creation Tests ===\n");
        testLeafNodeCreation();
        testCompositeNodeCreation();
        testEpsilonNode();

        System.out.println("\n=== A3-02: Tree-Building Method Tests ===\n");
        testAdoptChildren();
        testMakeSiblings();
        testMakeFamily();
        testPopUntilEpsilon();
        testBuildVarDecl();
        testBuildAssignStatWithAddOp();
        testBuildProgStructure();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    // =====================================================================
    // A3-01 TESTS: Node Creation
    // =====================================================================

    /** Test: Create leaf nodes (Id, Num, Type) with values */
    static void testLeafNodeCreation() {
        // After A3-01, you should be able to create leaf nodes like:
        //   ASTNode idNode = ASTNode.makeNode("Id", "myVar", 5);
        //   ASTNode numNode = ASTNode.makeNode("Num", "42", 3);
        //   ASTNode typeNode = ASTNode.makeNode("Type", "int", 1);
        //
        // Verify:
        //   - node.getType() returns the type string
        //   - node.getValue() returns the value string
        //   - node.getChildren() is empty (it's a leaf)
        //   - node.getLineNumber() returns the line number

        try {
            ASTNode idNode = ASTNode.makeNode("Id", "myVar", 5);
            check("Leaf Id - type", "Id".equals(idNode.getType()));
            check("Leaf Id - value", "myVar".equals(idNode.getValue()));
            check("Leaf Id - no children", idNode.getChildren() == null);
            check("Leaf Id - line", idNode.getLineNumber() == 5);

            ASTNode numNode = ASTNode.makeNode("Num", "42", 3);
            check("Leaf Num - type", "Num".equals(numNode.getType()));
            check("Leaf Num - value", "42".equals(numNode.getValue()));

            ASTNode floatNode = ASTNode.makeNode("Num", "3.14", 7);
            check("Leaf Num float - type", "Num".equals(floatNode.getType()));
            check("Leaf Num float - value", "3.14".equals(floatNode.getValue()));

            ASTNode typeNode = ASTNode.makeNode("Type", "int", 1);
            check("Leaf Type - type", "Type".equals(typeNode.getType()));
            check("Leaf Type - value", "int".equals(typeNode.getValue()));
        } catch (Exception e) {
            check("Leaf node creation threw exception: " + e.getMessage(), false);
        }
    }

    /** Test: Create composite nodes (no value, will have children) */
    static void testCompositeNodeCreation() {
        try {
            ASTNode varDecl = ASTNode.makeNode("VarDecl", 0);
            check("Composite - type", "VarDecl".equals(varDecl.getType()));
            check("Composite - no value", varDecl.getValue() == null);
            check("Composite - no children yet", varDecl.getChildren().isEmpty());
        } catch (Exception e) {
            check("Composite node creation threw exception: " + e.getMessage(), false);
        }
    }

    /** Test: Create epsilon sentinel node */
    static void testEpsilonNode() {
        try {
            ASTNode eps = ASTNode.makeNode("Epsilon", 0);
            check("Epsilon - type", "Epsilon".equals(eps.getType()));
        } catch (Exception e) {
            check("Epsilon node creation threw exception: " + e.getMessage(), false);
        }
    }

    // =====================================================================
    // A3-02 TESTS: Tree-Building Methods
    // =====================================================================

    /** Test: adoptChildren makes nodes children of a parent */
    static void testAdoptChildren() {
        // Build:  VarDecl -> [Type, Id]
        try {
            ASTNode parent = ASTNode.makeNode("VarDecl", 1);
            ASTNode child1 = ASTNode.makeNode("Type", "int", 1);
            ASTNode child2 = ASTNode.makeNode("Id", "x", 1);

            parent.adoptChildren(child1);
            parent.adoptChildren(child2);

            check("adoptChildren - has 2 children", parent.getChildren().size() == 2);
            check("adoptChildren - first child is Type",
                    "Type".equals(parent.getChildren().get(0).getType()));
            check("adoptChildren - second child is Id",
                    "Id".equals(parent.getChildren().get(1).getType()));
        } catch (Exception e) {
            check("adoptChildren threw exception: " + e.getMessage(), false);
        }
    }

    /** Test: makeSiblings links nodes as siblings */
    static void testMakeSiblings() {
        // This tests that sibling linking works. If your implementation
        // uses a children list on the parent instead of sibling pointers,
        // you may adapt this test accordingly.
        try {
            ASTNode parent = ASTNode.makeNode("VarDecl", 1);
            ASTNode node1 = ASTNode.makeNode("Id", "a", 1);
            ASTNode node2 = ASTNode.makeNode("Id", "b", 1);
            parent.adoptChildren(node1); // node1 needs a parent first
            node1.makeSiblings(node2);
            check("makeSiblings - parent has 2 children", parent.getChildren().size() == 2);
            check("makeSiblings - second child is b", "b".equals(parent.getChildren().get(1).getValue()));
        } catch (Exception e) {
            check("makeSiblings threw exception: " + e.getMessage(), false);
        }
    }

    /** Test: makeFamily creates parent with given children */
    static void testMakeFamily() {
        // Build: AddOp -> [Id("a"), Id("b")]
        // This matches: a + b -> AddOp with two children
        try {
            ASTNode left = ASTNode.makeNode("Id", "a", 1);
            ASTNode right = ASTNode.makeNode("Id", "b", 1);
            ASTNode addOp = ASTNode.makeFamily("AddOp", 1, List.of(left, right));

            check("makeFamily - type", "AddOp".equals(addOp.getType()));
            check("makeFamily - 2 children", addOp.getChildren().size() == 2);
            check("makeFamily - left child", "a".equals(addOp.getChildren().get(0).getValue()));
            check("makeFamily - right child", "b".equals(addOp.getChildren().get(1).getValue()));
        } catch (Exception e) {
            check("makeFamily threw exception: " + e.getMessage(), false);
        }
    }

    /**
     * Test: popUntilEpsilon pattern for variable-length lists.
     * Simulates what happens with DimList construction:
     *   push epsilon, push dim("5"), push dim("3"), push dim("2")
     *   then popUntilEpsilon -> collects [dim("5"), dim("3"), dim("2")]
     *
     * NOTE: This tests the pattern, not the parser. You may need to
     * adapt depending on how you implement popUntilEpsilon in your code.
     * The key thing: it should collect items in the correct order.
     */
    static void testPopUntilEpsilon() {
        try {
            // Simulate semantic stack operations
            java.util.Deque<ASTNode> stack = new java.util.ArrayDeque<>();

            // Push Epsilon sentinel
            stack.push(ASTNode.makeNode("Epsilon", 0));
            // Push dimensions (in parsing order)
            stack.push(ASTNode.makeNode("Dim", "5", 1));
            stack.push(ASTNode.makeNode("Dim", "3", 1));
            stack.push(ASTNode.makeNode("Dim", "2", 1));

            // Pop until Epsilon (your helper method does this)
            java.util.List<ASTNode> collected = new java.util.ArrayList<>();
            while (!"Epsilon".equals(stack.peek().getType())) {
                collected.add(0, stack.pop()); // prepend to reverse order
            }
            stack.pop(); // remove epsilon

            check("popUntilEpsilon - collected 3 items", collected.size() == 3);
            check("popUntilEpsilon - first is 5", "5".equals(collected.get(0).getValue()));
            check("popUntilEpsilon - second is 3", "3".equals(collected.get(1).getValue()));
            check("popUntilEpsilon - third is 2", "2".equals(collected.get(2).getValue()));
            check("popUntilEpsilon - Epsilon removed", stack.isEmpty());
        } catch (Exception e) {
            check("popUntilEpsilon threw exception: " + e.getMessage(), false);
        }
    }

    /**
     * Test: Build a complete VarDecl subtree manually.
     * Models: integer x[5][3];
     * Expected AST:
     *   VarDecl
     *   | Type | int
     *   | Id | x
     *   | DimList
     *   | | Dim | 5
     *   | | Dim | 3
     */
    static void testBuildVarDecl() {
        try {
            ASTNode typeNode = ASTNode.makeNode("Type", "int", 1);
            ASTNode idNode = ASTNode.makeNode("Id", "x", 1);
            ASTNode dim1 = ASTNode.makeNode("Dim", "5", 1);
            ASTNode dim2 = ASTNode.makeNode("Dim", "3", 1);
            ASTNode dimList = ASTNode.makeFamily("DimList", 1, List.of(dim1, dim2));
            ASTNode varDecl = ASTNode.makeFamily("VarDecl", 1, List.of(typeNode, idNode, dimList));

            check("VarDecl - type correct", "VarDecl".equals(varDecl.getType()));
            check("VarDecl - 3 children", varDecl.getChildren().size() == 3);
            check("VarDecl - child 0 is Type", "Type".equals(varDecl.getChildren().get(0).getType()));
            check("VarDecl - child 1 is Id", "Id".equals(varDecl.getChildren().get(1).getType()));
            check("VarDecl - child 2 is DimList", "DimList".equals(varDecl.getChildren().get(2).getType()));
            check("VarDecl - DimList has 2 dims", varDecl.getChildren().get(2).getChildren().size() == 2);
        } catch (Exception e) {
            check("VarDecl build threw exception: " + e.getMessage(), false);
        }
    }

    /**
     * Test: Build AssignStat with expression: a = b + c
     * Expected AST:
     *   AssignStat
     *   | Id | a
     *   | AddOp
     *   | | Id | b
     *   | | Id | c
     *
     * Reference: example2.ast.outast lines 32-38 (a=a+b*c pattern)
     */
    static void testBuildAssignStatWithAddOp() {
        try {
            ASTNode lhs = ASTNode.makeNode("Id", "a", 1);
            ASTNode left = ASTNode.makeNode("Id", "b", 1);
            ASTNode right = ASTNode.makeNode("Id", "c", 1);
            ASTNode addOp = ASTNode.makeFamily("AddOp", 1, List.of(left, right));
            ASTNode assign = ASTNode.makeFamily("AssignStat", 1, List.of(lhs, addOp));

            check("AssignStat - type", "AssignStat".equals(assign.getType()));
            check("AssignStat - 2 children", assign.getChildren().size() == 2);
            check("AssignStat - LHS is Id", "Id".equals(assign.getChildren().get(0).getType()));
            check("AssignStat - RHS is AddOp", "AddOp".equals(assign.getChildren().get(1).getType()));

            ASTNode addChild = assign.getChildren().get(1);
            check("AddOp - left is b", "b".equals(addChild.getChildren().get(0).getValue()));
            check("AddOp - right is c", "c".equals(addChild.getChildren().get(1).getValue()));
        } catch (Exception e) {
            check("AssignStat build threw exception: " + e.getMessage(), false);
        }
    }

    /**
     * Test: Build the top-level Prog structure.
     * Even a minimal program has: Prog -> [ClassList, FuncDefList, ProgramBlock]
     * For an empty program, ClassList and FuncDefList have no children.
     *
     * Reference: example2.ast.outast lines 1-4
     */
    static void testBuildProgStructure() {
        try {
            ASTNode classList = ASTNode.makeFamily("ClassList", 0, List.of());
            ASTNode funcDefList = ASTNode.makeFamily("FuncDefList", 0, List.of());
            ASTNode programBlock = ASTNode.makeFamily("ProgramBlock", 0, List.of());
            ASTNode prog = ASTNode.makeFamily("Prog", 0, List.of(classList, funcDefList, programBlock));

            check("Prog - type", "Prog".equals(prog.getType()));
            check("Prog - 3 children", prog.getChildren().size() == 3);
            check("Prog - child 0 is ClassList", "ClassList".equals(prog.getChildren().get(0).getType()));
            check("Prog - child 1 is FuncDefList", "FuncDefList".equals(prog.getChildren().get(1).getType()));
            check("Prog - child 2 is ProgramBlock", "ProgramBlock".equals(prog.getChildren().get(2).getType()));
            check("Prog - ClassList empty", prog.getChildren().get(0).getChildren().isEmpty());
        } catch (Exception e) {
            check("Prog structure threw exception: " + e.getMessage(), false);
        }
    }

    // =====================================================================
    // Helper
    // =====================================================================

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
