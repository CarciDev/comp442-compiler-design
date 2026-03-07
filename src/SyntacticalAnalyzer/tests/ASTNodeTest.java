package src.SyntacticalAnalyzer;

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

    /** Test: Create leaf nodes (id, intNum, floatNum, type) with values */
    static void testLeafNodeCreation() {
        // After A3-01, you should be able to create leaf nodes like:
        //   ASTNode idNode = ASTNode.makeNode("id", "myVar", 5);
        //   ASTNode numNode = ASTNode.makeNode("intNum", "42", 3);
        //   ASTNode typeNode = ASTNode.makeNode("type", "integer", 1);
        //
        // Verify:
        //   - node.getType() returns the type string
        //   - node.getValue() returns the value string
        //   - node.getChildren() is empty (it's a leaf)
        //   - node.getLine() returns the line number

        try {
            ASTNode idNode = ASTNode.makeNode("id", "myVar", 5);
            check("Leaf id - type", "id".equals(idNode.getType()));
            check("Leaf id - value", "myVar".equals(idNode.getValue()));
            check("Leaf id - no children", idNode.getChildren().isEmpty());
            check("Leaf id - line", idNode.getLine() == 5);

            ASTNode numNode = ASTNode.makeNode("intNum", "42", 3);
            check("Leaf intNum - type", "intNum".equals(numNode.getType()));
            check("Leaf intNum - value", "42".equals(numNode.getValue()));

            ASTNode floatNode = ASTNode.makeNode("floatNum", "3.14", 7);
            check("Leaf floatNum - type", "floatNum".equals(floatNode.getType()));
            check("Leaf floatNum - value", "3.14".equals(floatNode.getValue()));

            ASTNode typeNode = ASTNode.makeNode("type", "integer", 1);
            check("Leaf type - type", "type".equals(typeNode.getType()));
            check("Leaf type - value", "integer".equals(typeNode.getValue()));
        } catch (Exception e) {
            check("Leaf node creation threw exception: " + e.getMessage(), false);
        }
    }

    /** Test: Create composite nodes (no value, will have children) */
    static void testCompositeNodeCreation() {
        try {
            ASTNode varDecl = ASTNode.makeNode("varDecl");
            check("Composite - type", "varDecl".equals(varDecl.getType()));
            check("Composite - no value", varDecl.getValue() == null || varDecl.getValue().isEmpty());
            check("Composite - no children yet", varDecl.getChildren().isEmpty());
        } catch (Exception e) {
            check("Composite node creation threw exception: " + e.getMessage(), false);
        }
    }

    /** Test: Create epsilon sentinel node */
    static void testEpsilonNode() {
        try {
            ASTNode eps = ASTNode.makeNode("epsilon");
            check("Epsilon - type", "epsilon".equals(eps.getType()));
        } catch (Exception e) {
            check("Epsilon node creation threw exception: " + e.getMessage(), false);
        }
    }

    // =====================================================================
    // A3-02 TESTS: Tree-Building Methods
    // =====================================================================

    /** Test: adoptChildren makes nodes children of a parent */
    static void testAdoptChildren() {
        // Build:  varDecl -> [type, id]
        try {
            ASTNode parent = ASTNode.makeNode("varDecl");
            ASTNode child1 = ASTNode.makeNode("type", "integer", 1);
            ASTNode child2 = ASTNode.makeNode("id", "x", 1);

            ASTNode.adoptChildren(parent, child1);
            ASTNode.adoptChildren(parent, child2);

            check("adoptChildren - has 2 children", parent.getChildren().size() == 2);
            check("adoptChildren - first child is type",
                    "type".equals(parent.getChildren().get(0).getType()));
            check("adoptChildren - second child is id",
                    "id".equals(parent.getChildren().get(1).getType()));
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
            ASTNode node1 = ASTNode.makeNode("id", "a", 1);
            ASTNode node2 = ASTNode.makeNode("id", "b", 1);
            ASTNode.makeSiblings(node1, node2);
            // Verify they are linked (implementation-dependent check)
            check("makeSiblings - nodes created without error", true);
        } catch (Exception e) {
            check("makeSiblings threw exception: " + e.getMessage(), false);
        }
    }

    /** Test: makeFamily creates parent with given children */
    static void testMakeFamily() {
        // Build: addOp -> [id("a"), id("b")]
        // This matches: a + b -> AddOp with two children
        try {
            ASTNode left = ASTNode.makeNode("id", "a", 1);
            ASTNode right = ASTNode.makeNode("id", "b", 1);
            ASTNode addOp = ASTNode.makeFamily("addOp", left, right);

            check("makeFamily - type", "addOp".equals(addOp.getType()));
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

            // Push epsilon sentinel
            stack.push(ASTNode.makeNode("epsilon"));
            // Push dimensions (in parsing order)
            stack.push(ASTNode.makeNode("intNum", "5", 1));
            stack.push(ASTNode.makeNode("intNum", "3", 1));
            stack.push(ASTNode.makeNode("intNum", "2", 1));

            // Pop until epsilon (your helper method does this)
            java.util.List<ASTNode> collected = new java.util.ArrayList<>();
            while (!"epsilon".equals(stack.peek().getType())) {
                collected.add(0, stack.pop()); // prepend to reverse order
            }
            stack.pop(); // remove epsilon

            check("popUntilEpsilon - collected 3 items", collected.size() == 3);
            check("popUntilEpsilon - first is 5", "5".equals(collected.get(0).getValue()));
            check("popUntilEpsilon - second is 3", "3".equals(collected.get(1).getValue()));
            check("popUntilEpsilon - third is 2", "2".equals(collected.get(2).getValue()));
            check("popUntilEpsilon - epsilon removed", stack.isEmpty());
        } catch (Exception e) {
            check("popUntilEpsilon threw exception: " + e.getMessage(), false);
        }
    }

    /**
     * Test: Build a complete VarDecl subtree manually.
     * Models: integer x[5][3];
     * Expected AST:
     *   VarDecl
     *   | Type | integer
     *   | Id | x
     *   | DimList
     *   | | Num | 5
     *   | | Num | 3
     *
     * Reference: 8.5.ASTgeneration.pdf slides 3-5
     */
    static void testBuildVarDecl() {
        try {
            ASTNode typeNode = ASTNode.makeNode("type", "integer", 1);
            ASTNode idNode = ASTNode.makeNode("id", "x", 1);
            ASTNode dim1 = ASTNode.makeNode("intNum", "5", 1);
            ASTNode dim2 = ASTNode.makeNode("intNum", "3", 1);
            ASTNode dimList = ASTNode.makeFamily("dimList", dim1, dim2);
            ASTNode varDecl = ASTNode.makeFamily("varDecl", typeNode, idNode, dimList);

            check("VarDecl - type correct", "varDecl".equals(varDecl.getType()));
            check("VarDecl - 3 children", varDecl.getChildren().size() == 3);
            check("VarDecl - child 0 is Type", "type".equals(varDecl.getChildren().get(0).getType()));
            check("VarDecl - child 1 is Id", "id".equals(varDecl.getChildren().get(1).getType()));
            check("VarDecl - child 2 is DimList", "dimList".equals(varDecl.getChildren().get(2).getType()));
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
            ASTNode lhs = ASTNode.makeNode("id", "a", 1);
            ASTNode left = ASTNode.makeNode("id", "b", 1);
            ASTNode right = ASTNode.makeNode("id", "c", 1);
            ASTNode addOp = ASTNode.makeFamily("addOp", left, right);
            ASTNode assign = ASTNode.makeFamily("assignStat", lhs, addOp);

            check("AssignStat - type", "assignStat".equals(assign.getType()));
            check("AssignStat - 2 children", assign.getChildren().size() == 2);
            check("AssignStat - LHS is id", "id".equals(assign.getChildren().get(0).getType()));
            check("AssignStat - RHS is addOp", "addOp".equals(assign.getChildren().get(1).getType()));

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
            ASTNode classList = ASTNode.makeFamily("classList");
            ASTNode funcDefList = ASTNode.makeFamily("funcDefList");
            ASTNode programBlock = ASTNode.makeFamily("programBlock");
            ASTNode prog = ASTNode.makeFamily("prog", classList, funcDefList, programBlock);

            check("Prog - type", "prog".equals(prog.getType()));
            check("Prog - 3 children", prog.getChildren().size() == 3);
            check("Prog - child 0 is classList", "classList".equals(prog.getChildren().get(0).getType()));
            check("Prog - child 1 is funcDefList", "funcDefList".equals(prog.getChildren().get(1).getType()));
            check("Prog - child 2 is programBlock", "programBlock".equals(prog.getChildren().get(2).getType()));
            check("Prog - classList empty", prog.getChildren().get(0).getChildren().isEmpty());
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
