package src.SyntacticalAnalyzer;

// =============================================================================
// (A3-04): IMPLEMENT THE AST PRINTER (TEXT FORMAT -> .outast)
// =============================================================================
//
// WHAT: A Visitor implementation that traverses the AST and outputs it in
//       indented text format to a .outast file. This is one of the two
//       acceptable output formats (the other is Graphviz DOT).
//
// WHY:  The .outast file is a graded deliverable (5 pts). It proves your
//       AST was constructed correctly. The marker will compare your tree
//       structure against expected output.
//
// HOW IT CONNECTS:
//   - Implements Visitor (A3-03)
//   - Called by ASTDriver (A3-09) after parsing is complete
//   - Takes the root ASTNode from Parser and prints the full tree
//
// OUTPUT FORMAT (from assignment examples):
//   Each level is indented with "| " (pipe + space). Leaf nodes show their
//   value after " | ". Example:
//
//     Prog
//     | ClassList
//     | | ClassDecl
//     | | | Id | myClass
//     | | | InherList
//     | | | MembList
//     | | | | VarDecl
//     | | | | | Type | integer
//     | | | | | Id | x
//     | | | | | DimList
//     | FuncDefList
//     | ProgramBlock
//     | | VarDecl
//     | | | Type | float
//     | | | Id | a
//     | | | DimList
//     | | AssignStat
//     | | | Id | a
//     | | | Num | 5
//
// ALGORITHM:
//   Recursive depth-first traversal:
//     printNode(node, depth):
//       1. Print "| " repeated 'depth' times, then node.type
//       2. If node is a leaf with a value, append " | " + node.value
//       3. Print newline
//       4. For each child: printNode(child, depth + 1)
//
// NOTE: Empty list nodes (e.g., ClassList with no classes) should still
//       appear as a line. They just won't have any children printed below them.
//
// =============================================================================

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
