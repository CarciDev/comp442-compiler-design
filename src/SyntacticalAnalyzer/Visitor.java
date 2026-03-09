package src.SyntacticalAnalyzer;

// A3: Visitor interface for AST traversal (decouples tree structure from operations).

public interface Visitor {
    public void visit(ASTNode node);
}
