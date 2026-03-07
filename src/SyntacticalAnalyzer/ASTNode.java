package src.SyntacticalAnalyzer;

// =============================================================================
// CREATE THE AST NODE CLASS
// =============================================================================
//
// WHAT: Build the data structure that represents each node in the Abstract
//       Syntax Tree. Every node needs: a type (what kind of node), an optional
//       value (for leaves like identifiers/numbers), and links to children.
//
// WHY:  The AST is the intermediate representation of your program. Unlike
//       the parse tree (which has nodes for every grammar symbol), the AST
//       only keeps the semantically meaningful parts. No semicolons, no
//       parentheses, no commas - just the structure that matters.
//       See: 8.SDTAST.pdf slides 2-3 (AST definition and goals)
//
// HOW IT CONNECTS: The Parser (A3-06/07/08) will build ASTNodes during
//       parsing using a semantic stack. The Visitor (A3-03/04) will
//       traverse the finished tree to produce output.
//
// DESIGN CHOICES (read 8.SDTAST.pdf slides 5-7):
//   Option A: Single class with a "type" string/enum + children list
//             Simpler, fewer files. Recommended for this assignment.
//   Option B: Class hierarchy (abstract ASTNode, subclass per type)
//             More OOP, but 20+ classes to maintain.
//
// NODE TYPES
// Sources: assignment 3 example .outast files (ground truth) + assignment grammar
//
//   COMPOSITE (internal nodes - have children):
//
//     --- Confirmed by example .outast files ---
//     Prog          -> children: ClassList, FuncDefList, ProgramBlock (always 3)
//     ClassList     -> children: zero or more Class
//     FuncDefList   -> children: zero or more FuncDef
//     Class         -> children: Id, [InherList], {Visibility, VarDecl/FuncDecl}...
//     FuncDef       -> children: [Id (scope)], Type/Void, Id, ParamList, StatBlock
//     ParamList     -> children: zero or more VarDecl (also used for aParams in FuncCall)
//     VarDecl       -> children: Type, Id, DimList
//     DimList       -> children: zero or more Dim
//     ProgramBlock  -> children: zero or more VarDecl/statement (main program body)
//     StatBlock     -> children: zero or more VarDecl/statement (function body)
//     AssignStat    -> children: variable, expr
//     PutStat       -> children: expr                     (from: write '(' expr ')')
//     ReturnStat    -> children: expr                     (from: return '(' expr ')')
//     AddOp         -> children: left expr, right expr    (binary; operator stored in value)
//     MultOp        -> children: left expr, right expr    (binary; operator stored in value)
//     FuncCall      -> children: Id, ParamList            (both as expr and as standalone statement)
//
//     --- From grammar, not shown in provided examples ---
//     IfStat        -> children: RelExpr, StatBlock (then), StatBlock (else)
//     WhileStat     -> children: RelExpr, StatBlock
//     GetStat       -> children: variable                 (from: read '(' variable ')')
//     RelExpr       -> children: arithExpr, RelOp, arithExpr
//     Not           -> children: factor                   (unary)
//     Sign          -> children: factor                   (unary; sign stored in value)
//     Dot           -> children: left, right              (member access: a.b)
//     DataMember    -> children: Id, IndexList            (variable with optional indices)
//     IndexList     -> children: zero or more arithExpr   (from: {{indice}})
//     InherList     -> children: zero or more Id          (from: inherits id {',' id})
//     FuncDecl      -> children: Id, ParamList, Type/Void (function declaration in class)
//     Visibility    -> leaf-like wrapper, value = "public" or "private"
//
//   ATOMIC (leaf nodes - no children, carry a value):
//     Id            -> value = identifier string          (e.g., "myVar")
//     Num           -> value = integer or float literal   (e.g., "1", "3.14")
//     Type          -> value = "int", "float", or class name
//     Dim           -> value = array dimension size       (e.g., "5"; empty for [])
//     Void          -> value = "void"                     (return type for functions)
//     RelOp         -> value = "eq","neq","lt","gt","leq","geq"
//     Sign (leaf)   -> value = "+" or "-"                 (operator token)
//     Epsilon       -> marker for empty lists (used during semantic stack operations)
//
// REQUIRED FIELDS:
//   - String type        (node type from list above)
//   - String value       (for leaf nodes; null for composite)
//   - List<ASTNode> children  (for composite nodes; empty for leaves)
//   - ASTNode parent     (optional but helpful for traversal)
//   - int lineNumber     (from Token, for error reporting in A4/A5)
//
// =============================================================================

// =============================================================================
// IMPLEMENT TREE-BUILDING METHODS
// =============================================================================
//
// WHAT: Factory/helper methods that the semantic actions will call to
//       construct the AST during parsing.
//
// WHY:  During parsing, the semantic stack holds partially-built AST nodes.
//       These methods are the "glue" that assembles nodes into subtrees.
//       See: 8.SDTAST.pdf slides 8-10 (makeNode, makeSiblings, adoptChildren,
//       makeFamily) and 8.5.ASTgeneration.pdf slide 4 (createLeaf, createSubtree)
//
// METHODS TO IMPLEMENT:
//
//   1. makeNode(String type, String value, int line)
//      Creates a LEAF node (atomic concept). Used for Id, Num, Type, etc.
//      Example: makeNode("Id", "myVar", 5) -> leaf node for identifier "myVar"
//
//   2. makeNode(String type)
//      Creates an INTERNAL node (composite concept) with no value yet.
//      Example: makeNode("VarDecl") -> empty VarDecl waiting for children
//
//   3. adoptChildren(ASTNode parent, ASTNode child)
//      Makes 'child' (and all its siblings) children of 'parent'.
//
//   4. makeSiblings(ASTNode node1, ASTNode node2)
//      Links node1 and node2 as siblings (they share a parent).
//      Appends node2 to the end of node1's sibling list.
//
//   5. makeFamily(String parentType, ASTNode... children)
//      Shorthand: creates a parent node and adopts all children.
//      Example: makeFamily("AddOp", leftExpr, rightTerm)
//
// KEY INSIGHT from 8.5.ASTgeneration.pdf slide 4:
//   - "createLeaf(type)" = makeNode for a leaf
//   - "createSubtree(type, pop, pop, ...)" = makeFamily by popping from stack
//   - "popuntile" = pop from semantic stack until you hit an epsilon marker.
//     This is how you handle VARIABLE-LENGTH lists (param lists, dim lists,
//     statement blocks). You push an epsilon sentinel first, then push items,
//     then popuntile collects everything into a list node.
//
// =============================================================================
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ASTNode {
    private String type;
    private String value;
    private List<ASTNode> children;
    private ASTNode parent;
    private int lineNumber;

    //constructors

    //Creating Leaf - Private constructor
    private ASTNode(String type, String value, int lineNumber) {
        this.type = type; //leaf type
        this.value = value; //leaf value
        this.children = null; //leafs don't have children
        this.lineNumber = lineNumber; //for traceability
    }

    //Creating Composite - Private constructor
    private ASTNode(String type,int lineNumber){
        this.type = type; //composite type
        this.value = null; //composites don't have values, only children.
        this.children = new ArrayList<>(); //composites have children.
        this.lineNumber = lineNumber; //for traceability
    }

    //Static methods: Supporting Factory Pattern and useful methods found in the notes.

    //Creating Leaf
    public static ASTNode makeNode(String type, String value, int lineNumber){
        return new ASTNode(type,value,lineNumber);
    }

    //Creating Composite
    public static ASTNode makeNode(String type,int lineNumber){
        return new ASTNode(type,lineNumber);
    }

    //Make family takes in two nodes, hence does not need to belong to an instance.
    public static ASTNode makeFamily(String parentType,int lineNumber,List<ASTNode> childrenNodes){
        ASTNode parentNode = makeNode(parentType,lineNumber);
        for(ASTNode child : childrenNodes){
            parentNode.adoptChildren(child); //adopting a child automatically makes it a rightmost sibling of the last child.
        }
        return parentNode;
    }

    public ASTNode makeSiblings(ASTNode rightSibling){
        ASTNode leftSibling = this;
        leftSibling.getParent().adoptChildren(rightSibling); //go to left sibling, reach parent, add its child (adds to list which means rightmost).
        return leftSibling;
    }

    public ASTNode adoptChildren(ASTNode childNode){
        ASTNode parentNode = this; //get target sibling
        parentNode.getChildren().add(childNode); //get the children
        childNode.setParent(parentNode); //set the link to the parent.
        return parentNode;
    }

    //Visitor Accept
    public void accept(Visitor v){
        v.visit(this);
    }

    //Getters and Setters

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public void setChildren(List<ASTNode> children) {
        this.children = children;
    }

    public ASTNode getParent() {
        return parent;
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
