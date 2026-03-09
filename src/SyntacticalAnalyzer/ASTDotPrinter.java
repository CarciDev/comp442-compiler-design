package src.SyntacticalAnalyzer;

import java.io.PrintWriter;

/**
 * Visitor that outputs the AST in Graphviz DOT format.
 */
public class ASTDotPrinter implements Visitor {

    private final PrintWriter out;
    private int nodeCounter = 0;

    public ASTDotPrinter(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void visit(ASTNode node) {
        out.println("digraph AST {");
        out.println("  node [shape=box, fontname=\"Courier\"];");
        printNode(node);
        out.println("}");
        out.flush();
    }

    private int printNode(ASTNode node) {
        int id = nodeCounter++;

        // Build label
        String label = node.getType();
        if (node.getValue() != null) {
            label += "\\n" + escape(node.getValue());
        }
        out.println("  n" + id + " [label=\"" + label + "\"];");

        // Recurse into children
        if (node.getChildren() != null) {
            for (ASTNode child : node.getChildren()) {
                int childId = printNode(child);
                out.println("  n" + id + " -> n" + childId + ";");
            }
        }

        return id;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
