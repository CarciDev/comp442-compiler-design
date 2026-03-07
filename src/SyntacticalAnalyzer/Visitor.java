package src.SyntacticalAnalyzer;

// =============================================================================
// TODO(A3-03): CREATE THE VISITOR INTERFACE
// =============================================================================
//
// WHAT: Define a Visitor interface that can traverse the AST tree.
//       Each "visit" method handles one type of AST node.
//
// WHY:  The Visitor pattern separates the tree structure (ASTNode) from the
//       operations you perform on it (printing, type checking, code gen).
//       In A3 you only need printing, but A4 (symbol tables) and A5 (code gen)
//       will add new visitors without changing ASTNode at all.
//       This is a classic design pattern in compiler construction.
//
// HOW IT CONNECTS:
//   - ASTNode (A3-01) will have an accept(Visitor v) method
//   - ASTPrinter (A3-04) will implement this interface
//   - Future assignments add more implementations (SymbolTableVisitor, etc.)
//
// DESIGN:
//   If you used a single ASTNode class with a "type" string (recommended in
//   A3-01), you can simplify this to just:
//     void visit(ASTNode node)
//   The visitor checks node.getType() internally to decide what to do.
//
//   Alternatively, if you used subclasses, you'd have one visit method per
//   subclass (visitProg, visitClassDecl, visitVarDecl, ...).
//
//   Either approach works. The simpler one-method version is fine for A3.
//
// =============================================================================

public interface Visitor {
    // Implement here
    public void visit(ASTNode node);
}
