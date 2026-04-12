package src.CodeGenerator;

import src.SyntacticalAnalyzer.ASTNode;
import src.SyntacticalAnalyzer.Visitor;
import src.SemanticAnalyzer.SymbolTable;
import src.SemanticAnalyzer.SymbolTableEntry;

/**
 * Pre-computation phase that calculates memory sizes and offsets for all
 * symbol table entries. This runs as a separate visitor pass before code
 * generation, as recommended in the course slides (CodeGenerationII, slides 34-35).
 *
 * For each scope (class, function, program):
 *   - Computes the size of each variable/parameter entry
 *   - Computes the offset of each entry within its scope
 *   - Computes the total scope size (stack frame size for functions)
 *
 * Sizes: sizeof(int) = 4, sizeof(float) = 8, sizeof(class) = sum of member sizes,
 *        sizeof(array) = elementSize * product(dimensions)
 *
 * Offsets: negative from r14 (stack grows downward). Layout per function frame:
 *   -4(r14)          : return address
 *   -8(r14)          : self pointer (member functions only)
 *   -(8+paramSize)   : parameters (in order)
 *   -(8+paramSize+localSize) : local variables
 */
public class ComputeMemSizeVisitor implements Visitor {

    private final SymbolTable globalTable;

    public ComputeMemSizeVisitor(SymbolTable globalTable) {
        this.globalTable = globalTable;
    }

    @Override
    public void visit(ASTNode node) {
        if (!"Prog".equals(node.getType())) return;

        // Phase 1: Compute class sizes (needed before function frame sizes)
        for (SymbolTableEntry e : globalTable.getEntries()) {
            if ("class".equals(e.getKind()) && e.getLink() != null) {
                computeClassSize(e);
            }
        }

        // Phase 2: Compute function/main frame sizes and offsets
        for (SymbolTableEntry e : globalTable.getEntries()) {
            if ("function".equals(e.getKind()) && e.getLink() != null) {
                computeFuncFrameSize(e.getLink());
            }
            if ("class".equals(e.getKind()) && e.getLink() != null) {
                for (SymbolTableEntry me : e.getLink().getEntries()) {
                    if ("function".equals(me.getKind()) && me.getLink() != null) {
                        computeFuncFrameSize(me.getLink());
                    }
                }
            }
        }
    }

    // ==================== Class Sizes ====================

    private int computeClassSize(SymbolTableEntry classEntry) {
        SymbolTable ct = classEntry.getLink();
        int totalSize = 0;

        // Include inherited class sizes
        for (SymbolTable inh : ct.getInheritedTables()) {
            SymbolTableEntry inhClass = globalTable.lookupLocal(inh.getName(), "class");
            if (inhClass != null) {
                totalSize += computeClassSize(inhClass);
            }
        }

        // Compute size and offset for each data member
        for (SymbolTableEntry e : ct.getEntries()) {
            if ("data".equals(e.getKind())) {
                int memberSize = typeSize(e.getType());
                e.setSize(memberSize);
                e.setOffset(totalSize);
                totalSize += memberSize;
            }
        }

        // Store class total size in the class entry and scope
        classEntry.setSize(totalSize);
        ct.setScopeSize(totalSize);
        return totalSize;
    }

    // ==================== Function Frame Sizes ====================

    private void computeFuncFrameSize(SymbolTable funcTable) {
        int offset = 0;

        // Return address at -4(r14)
        offset += 4;

        // For member functions, reserve -8(r14) for hidden 'self' pointer
        boolean isMember = funcTable.getName().contains("::") && !funcTable.getName().startsWith("::");
        if (isMember) {
            offset += 4;
        }

        // Parameters
        for (SymbolTableEntry e : funcTable.getEntries()) {
            if ("param".equals(e.getKind())) {
                int size = typeSize(e.getType());
                e.setSize(size);
                offset += size;
                e.setOffset(-offset);
            }
        }

        // Local variables
        for (SymbolTableEntry e : funcTable.getEntries()) {
            if ("local".equals(e.getKind())) {
                int size = typeSize(e.getType());
                e.setSize(size);
                offset += size;
                e.setOffset(-offset);
            }
        }

        // Reserve space for temporaries and sub-frame for library calls
        offset += 200;
        funcTable.setScopeSize(offset);
    }

    // ==================== Type Size Computation ====================

    public int typeSize(String type) {
        if (type == null || type.isEmpty()) return 0;
        String baseType = type.replaceAll("\\[.*", "");
        int baseSize;
        switch (baseType) {
            case "int":   baseSize = 4; break;
            case "float": baseSize = 4; break;  // Moon uses integer representation for floats
            default:
                // Class type — look up class size
                SymbolTableEntry ce = globalTable.lookupLocal(baseType, "class");
                baseSize = (ce != null) ? ce.getSize() : 4;
                if (baseSize == 0) baseSize = 4; // fallback
                break;
        }
        // Multiply by array dimensions
        int totalElements = 1;
        int idx = type.indexOf('[');
        while (idx >= 0) {
            int end = type.indexOf(']', idx);
            if (end > idx + 1) {
                try {
                    totalElements *= Integer.parseInt(type.substring(idx + 1, end));
                } catch (NumberFormatException ignored) {
                }
            }
            idx = type.indexOf('[', end + 1);
        }
        return baseSize * totalElements;
    }
}
