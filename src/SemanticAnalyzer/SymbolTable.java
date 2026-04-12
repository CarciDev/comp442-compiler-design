package src.SemanticAnalyzer;

import java.util.*;

public class SymbolTable {
    private final String name;
    private final List<SymbolTableEntry> entries = new ArrayList<>();
    private final SymbolTable parent;
    private final List<SymbolTable> inheritedTables = new ArrayList<>();
    private int scopeSize;  // total size of this scope in bytes (computed by ComputeMemSizeVisitor)

    public SymbolTable(String name, SymbolTable parent) {
        this.name = name;
        this.parent = parent;
        this.scopeSize = 0;
    }

    public void addEntry(SymbolTableEntry e) { entries.add(e); }

    public SymbolTableEntry lookupLocal(String name, String kind) {
        for (SymbolTableEntry e : entries)
            if (e.getName().equalsIgnoreCase(name) && e.getKind().equals(kind)) return e;
        return null;
    }

    public SymbolTableEntry lookupLocal(String name) {
        for (SymbolTableEntry e : entries)
            if (e.getName().equalsIgnoreCase(name) && !e.getKind().equals("inherit")) return e;
        return null;
    }

    public List<SymbolTableEntry> lookupAllLocal(String name) {
        List<SymbolTableEntry> result = new ArrayList<>();
        for (SymbolTableEntry e : entries)
            if (e.getName().equalsIgnoreCase(name) && !e.getKind().equals("inherit")) result.add(e);
        return result;
    }

    public SymbolTableEntry lookupInherited(String name) {
        for (SymbolTable t : inheritedTables) {
            SymbolTableEntry e = t.lookupLocal(name);
            if (e != null) return e;
            e = t.lookupInherited(name);
            if (e != null) return e;
        }
        return null;
    }

    public SymbolTableEntry lookupInherited(String name, String kind) {
        for (SymbolTable t : inheritedTables) {
            SymbolTableEntry e = t.lookupLocal(name, kind);
            if (e != null) return e;
            e = t.lookupInherited(name, kind);
            if (e != null) return e;
        }
        return null;
    }

    public void addInheritedTable(SymbolTable t) { inheritedTables.add(t); }
    public String getName() { return name; }
    public SymbolTable getParent() { return parent; }
    public List<SymbolTableEntry> getEntries() { return entries; }
    public List<SymbolTable> getInheritedTables() { return inheritedTables; }
    public int getScopeSize() { return scopeSize; }
    public void setScopeSize(int scopeSize) { this.scopeSize = scopeSize; }
}
