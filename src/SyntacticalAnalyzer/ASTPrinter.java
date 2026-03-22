package src.SyntacticalAnalyzer;

// A3: Visitor that outputs the AST in pipe-indented text format to .outast files.

public class ASTPrinter implements Visitor{
    public void printNode(ASTNode node,int depth){
        System.out.print("| ".repeat(depth)+node.getType()); //Printing depth.
        if(node.getValue() != null){ //leaf node - print value
            System.out.print(" | "+node.getValue());
            System.out.println();
        }else if(node.getChildren() != null){ //composite node - print children
            System.out.println();
            for(ASTNode child: node.getChildren()){
                printNode(child,depth+1);
            }
        }else{ //no children and no value (e.g., empty node or error recovery)
            System.out.println();
        }

    }

    @Override
    public void visit(ASTNode node) {
        printNode(node,0);
    }
}
