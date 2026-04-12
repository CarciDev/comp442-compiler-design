package src.SemanticAnalyzer;

public class SymbolTableEntry {
    private final String kind; // "class", "function", "data", "param", "local", "inherit"
    private final String name;
    private final String type;
    private final String visibility; // "public", "private", or null
    private SymbolTable link;
    private final int line;
    private int size;    // size in bytes (computed by ComputeMemSizeVisitor)
    private int offset;  // offset from stack frame base (computed by ComputeMemSizeVisitor)

    public SymbolTableEntry(String kind, String name, String type, String visibility, int line) {
        this.kind = kind;
        this.name = name;
        this.type = type;
        this.visibility = visibility;
        this.line = line;
        this.size = 0;
        this.offset = 0;
    }

    public String getKind() { return kind; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getVisibility() { return visibility; }
    public SymbolTable getLink() { return link; }
    public void setLink(SymbolTable link) { this.link = link; }
    public int getLine() { return line; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }
}
