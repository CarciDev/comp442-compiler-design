package src.SyntacticalAnalyzer;

// =============================================================================
// TODO(A3-01): CREATE THE AST NODE CLASS
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
// NODE TYPES YOU NEED (see 8.SDTAST.pdf slides 34-41 for the full reference):
//
//   COMPOSITE (internal nodes - have children):
//     prog         -> children: classList, funcDefList, statBlock (always 3)
//     classList     -> children: zero or more classDecl
//     funcDefList   -> children: zero or more funcDef
//     classDecl     -> children: id, inherList, membList
//     funcDef       -> children: type|void, scopeSpec?, id, fParamList, statBlock
//     inherList     -> children: zero or more id
//     membList      -> children: zero or more membDecl (varDecl or funcDecl)
//     fParamList    -> children: zero or more varDecl
//     varDecl       -> children: type, id, dimList
//     funcDecl      -> children: type, id, fParamList
//     dimList       -> children: zero or more num
//     statBlock     -> children: zero or more statement/varDecl
//     ifStat        -> children: relExpr, statBlock (then), statBlock (else)
//     whileStat     -> children: relExpr, statBlock
//     assignStat    -> children: variable, expr
//     getStat       -> children: variable
//     putStat       -> children: expr
//     returnStat    -> children: expr
//     relExpr       -> children: arithExpr, relOp, arithExpr
//     addOp         -> children: left arithExpr, right term (binary, always 2 + operator)
//     multOp        -> children: left term, right factor (binary, always 2 + operator)
//     not           -> children: factor (unary)
//     sign          -> children: factor (unary)
//     dot           -> children: left, right (member access chain)
//     dataMember    -> children: id, indexList
//     fCall         -> children: id, aParams
//     indexList     -> children: zero or more arithExpr
//     aParams       -> children: zero or more expr
//
//   ATOMIC (leaf nodes - no children, carry a value):
//     id            -> value = identifier string (e.g., "myVar")
//     intNum        -> value = integer literal (e.g., "42")
//     floatNum      -> value = float literal (e.g., "3.14")
//     type          -> value = "integer", "float", or class name
//     visibility    -> value = "public" or "private"
//     relOp         -> value = "eq","neq","lt","gt","leq","geq"
//     addOp (leaf)  -> value = "plus","minus","or" (operator token)
//     multOp (leaf) -> value = "mult","div","and" (operator token)
//     sign (leaf)   -> value = "plus" or "minus"
//     void          -> value = "void"
//     epsilon       -> marker for empty lists (used during stack operations)
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
// TODO(A3-02): IMPLEMENT TREE-BUILDING METHODS
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
//      Creates a LEAF node (atomic concept). Used for id, intNum, type, etc.
//      Example: makeNode("id", "myVar", 5) -> leaf node for identifier "myVar"
//      See: 8.SDTAST.pdf slide 8
//
//   2. makeNode(String type)
//      Creates an INTERNAL node (composite concept) with no value yet.
//      Example: makeNode("varDecl") -> empty varDecl waiting for children
//
//   3. adoptChildren(ASTNode parent, ASTNode child)
//      Makes 'child' (and all its siblings) children of 'parent'.
//      See: 8.SDTAST.pdf slide 9
//
//   4. makeSiblings(ASTNode node1, ASTNode node2)
//      Links node1 and node2 as siblings (they share a parent).
//      Appends node2 to the end of node1's sibling list.
//      See: 8.SDTAST.pdf slide 9
//
//   5. makeFamily(String parentType, ASTNode... children)
//      Shorthand: creates a parent node and adopts all children.
//      Example: makeFamily("addOp", leftExpr, rightTerm)
//      See: 8.SDTAST.pdf slide 10
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

public class ASTNode {
    // Implement here
}
