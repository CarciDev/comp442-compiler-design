package src.SyntacticalAnalyzer;

// A3: Universal AST node class with factory methods (makeNode, makeFamily) for
// syntax-directed tree construction. Supports leaf, composite, and operator nodes.
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
