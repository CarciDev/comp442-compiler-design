package src.SemanticAnalyzer;

import src.SyntacticalAnalyzer.ASTNode;
import src.SyntacticalAnalyzer.Visitor;
import java.util.*;

public class SymbolTableBuilder implements Visitor {
    private SymbolTable globalTable;
    private final List<String[]> errors = new ArrayList<>();
    private final Map<ASTNode, SymbolTable> funcTableMap = new HashMap<>();

    @Override
    public void visit(ASTNode node) {
        if (!"Prog".equals(node.getType())) return;
        globalTable = new SymbolTable("global", null);
        ASTNode classList = node.getChildren().get(0);
        ASTNode funcDefList = node.getChildren().get(1);
        ASTNode progBlock = node.getChildren().get(2);
        if (classList.getChildren() != null)
            for (ASTNode c : classList.getChildren()) processClassDecl(c);
        if (funcDefList.getChildren() != null)
            for (ASTNode f : funcDefList.getChildren()) processFuncDef(f);
        processMain(progBlock);
        checkMemberFuncConsistency();
        checkInheritance();
        checkCircularDeps();
    }

    private void processClassDecl(ASTNode node) {
        // 1. Get class name, check if already declared (8.1)
        String className = node.getChildren().get(0).getValue();
        int line = node.getChildren().get(0).getLineNumber();
        if (globalTable.lookupLocal(className, "class") != null) {
            addError(line, "8.1", "error", "multiply declared class '" + className + "'");
        }
        // 2. Create class entry + class table, link them, add to global table
        SymbolTableEntry classEntry = new SymbolTableEntry("class", className, "", null, line);
        SymbolTable classTable = new SymbolTable(className, globalTable);
        classEntry.setLink(classTable);
        globalTable.addEntry(classEntry);

        // 3. Process inheritance -- add "inherit" entries and connect parent tables to inheritedTables
        ASTNode inherList = node.getChildren().get(1);
        if (inherList.getChildren() != null && !inherList.getChildren().isEmpty()) {
            for (ASTNode inh : inherList.getChildren()) {
                String parentName = inh.getValue();
                classTable.addEntry(new SymbolTableEntry("inherit", parentName, "", null, inh.getLineNumber()));
                SymbolTableEntry parentEntry = globalTable.lookupLocal(parentName, "class");
                if (parentEntry != null && parentEntry.getLink() != null)
                    classTable.addInheritedTable(parentEntry.getLink());
            }
        } else {
            // No inheritance -- add a "none" entry
            classTable.addEntry(new SymbolTableEntry("inherit", "none", "", null, line));
        }

        // 4. Process members -- VarDecl -> "data" entry, FuncDecl -> "function" entry
        // Note: only processes declarations. Function bodies come later in processFuncDef.
        ASTNode memberList = node.getChildren().get(2);
        if (memberList.getChildren() != null) {
            for (ASTNode md : memberList.getChildren()) {
                String vis = md.getChildren().get(0).getValue();
                ASTNode decl = md.getChildren().get(1);
                if ("VarDecl".equals(decl.getType())) {
                    // Add data member, check for duplicates (8.3)
                    String vn = decl.getChildren().get(1).getValue();
                    String vt = buildTypeStr(decl.getChildren().get(0), decl.getChildren().get(2));
                    int vl = decl.getChildren().get(1).getLineNumber();
                    if (classTable.lookupLocal(vn, "data") != null)
                        addError(vl, "8.3", "error", "multiply declared data member '" + vn + "' in class '" + className + "'");
                    else
                        classTable.addEntry(new SymbolTableEntry("data", vn, vt, vis, vl));
                } else if ("FuncDecl".equals(decl.getType())) {
                    // Add function declaration, check for overloads (9.2)
                    String fn = decl.getChildren().get(0).getValue();
                    String sig = buildSig(decl.getChildren().get(1), decl.getChildren().get(2));
                    int fl = decl.getChildren().get(0).getLineNumber();
                    List<SymbolTableEntry> existing = new ArrayList<>();
                    for (SymbolTableEntry e : classTable.getEntries())
                        if ("function".equals(e.getKind()) && e.getName().equalsIgnoreCase(fn)) existing.add(e);
                    for (SymbolTableEntry e : existing) {
                        if (e.getType().equals(sig))
                            addError(fl, "9.2", "warning", "Overloaded member function '" + fn + "'");
                        else
                            addError(fl, "9.2", "warning", "Overloaded member function '" + fn + "'");
                    }
                    classTable.addEntry(new SymbolTableEntry("function", fn, sig, vis, fl));
                }
            }
        }
    }

    private void processFuncDef(ASTNode node) {
        // 1. Extract scope specifier, function name, params, return type from the AST
        ASTNode funcHead = node.getChildren().get(0);
        ASTNode funcBody = node.getChildren().get(1);
        ASTNode scopeSpec = funcHead.getChildren().get(0);
        ASTNode funcNameNode = funcHead.getChildren().get(1);
        ASTNode fParams = funcHead.getChildren().get(2);
        ASTNode retType = funcHead.getChildren().get(3);
        String funcName = funcNameNode.getValue();
        String sig = buildSig(fParams, retType);
        int line = funcNameNode.getLineNumber();

        // 2. No scope specifier (Epsilon) -> free function
        if ("Epsilon".equals(scopeSpec.getType()) || scopeSpec.getChildren() != null && scopeSpec.getChildren().isEmpty() && scopeSpec.getValue() == null) {
            // Check for duplicate (8.2) or overloaded (9.1) free functions
            List<SymbolTableEntry> existing = new ArrayList<>();
            for (SymbolTableEntry e : globalTable.getEntries())
                if ("function".equals(e.getKind()) && e.getName().equalsIgnoreCase(funcName)) existing.add(e);
            for (SymbolTableEntry e : existing) {
                if (e.getType().equals(sig))
                    addError(line, "8.2", "error", "multiply declared free function '" + funcName + "'");
                else
                    addError(line, "9.1", "warning", "Overloaded free function '" + funcName + "'");
            }
            // Create function entry + table, add to global table, process params and locals
            SymbolTableEntry fe = new SymbolTableEntry("function", funcName, sig, null, line);
            SymbolTable ft = new SymbolTable("::" + funcName, globalTable);
            fe.setLink(ft);
            globalTable.addEntry(fe);
            processParams(fParams, ft);
            processLocals(funcBody, ft, null);
            funcTableMap.put(node, ft);
        } else {
            // 3. Has scope specifier -> member function (e.g. MyClass::funcName)
            String className = scopeSpec.getValue();
            if (className == null && "Id".equals(scopeSpec.getType())) className = scopeSpec.getValue();
            // Find the class in global table (6.1 if not found)
            SymbolTableEntry classEntry = globalTable.lookupLocal(className, "class");
            if (classEntry == null) {
                addError(line, "6.1", "error", "undeclared member function definition '" + className + "::" + funcName + "'");
                SymbolTable ft = new SymbolTable(className + "::" + funcName, globalTable);
                processParams(fParams, ft);
                processLocals(funcBody, ft, null);
                funcTableMap.put(node, ft);
                return;
            }
            // Find the matching declaration (link == null means not yet defined) and attach the body
            SymbolTable classTable = classEntry.getLink();
            boolean found = false;
            for (SymbolTableEntry e : classTable.getEntries()) {
                if ("function".equals(e.getKind()) && e.getName().equalsIgnoreCase(funcName) && e.getLink() == null) {
                    SymbolTable ft = new SymbolTable(className + "::" + funcName, classTable);
                    e.setLink(ft); // attach the body to the declaration
                    processParams(fParams, ft);
                    processLocals(funcBody, ft, classTable);
                    funcTableMap.put(node, ft);
                    found = true;
                    break;
                }
            }
            // No matching declaration found → error 6.1, but still add it to recover
            if (!found) {
                addError(line, "6.1", "error", "undeclared member function definition '" + className + "::" + funcName + "'");
                SymbolTable ft = new SymbolTable(className + "::" + funcName, classTable);
                SymbolTableEntry fe = new SymbolTableEntry("function", funcName, sig, null, line);
                fe.setLink(ft);
                classTable.addEntry(fe);
                processParams(fParams, ft);
                processLocals(funcBody, ft, classTable);
                funcTableMap.put(node, ft);
            }
        }
    }

    private void processMain(ASTNode progBlock) {
        // 1. Create "main" function entry + table, add to global table
        SymbolTableEntry me = new SymbolTableEntry("function", "main", "():", null, 0);
        SymbolTable mt = new SymbolTable("::main", globalTable);
        me.setLink(mt);
        globalTable.addEntry(me);
        if (progBlock.getChildren() == null) return;
        // 2. Loop through program block children
        for (ASTNode child : progBlock.getChildren()) {
            if ("VarDecl".equals(child.getType())) {
                String vn = child.getChildren().get(1).getValue();
                String vt = buildTypeStr(child.getChildren().get(0), child.getChildren().get(2));
                int vl = child.getChildren().get(1).getLineNumber();
                // 3a. Check type exists -- strip array dims and verify it's a known type (11.5)
                String baseType = vt.replaceAll("\\[.*", "");
                if (!baseType.equals("int") && !baseType.equals("float") && globalTable.lookupLocal(baseType, "class") == null)
                    addError(vl, "11.5", "error", "Undeclared class '" + baseType + "'");
                // 3b. Check for duplicate local variable (8.4), then add as "local" entry
                if (mt.lookupLocal(vn, "local") != null)
                    addError(vl, "8.4", "error", "multiply declared variable '" + vn + "' in function 'main'");
                else
                    mt.addEntry(new SymbolTableEntry("local", vn, vt, null, vl));
            }
        }
        funcTableMap.put(progBlock, mt);
    }

    private void processParams(ASTNode fParams, SymbolTable ft) {
        if (fParams.getChildren() == null) return;
        // Loop through each parameter and add it as a "param" entry to the function's table
        for (ASTNode p : fParams.getChildren()) {
            String pn = p.getChildren().get(1).getValue();
            String pt = buildTypeStr(p.getChildren().get(0), p.getChildren().get(2));
            int pl = p.getChildren().get(1).getLineNumber();
            ft.addEntry(new SymbolTableEntry("param", pn, pt, null, pl));
        }
    }

    private void processLocals(ASTNode funcBody, SymbolTable ft, SymbolTable classTable) {
        // Get the variable declaration list from the function body
        ASTNode varDeclList = funcBody.getChildren().get(0);
        if (varDeclList.getChildren() == null) return;
        for (ASTNode vd : varDeclList.getChildren()) {
            String vn = vd.getChildren().get(1).getValue();
            String vt = buildTypeStr(vd.getChildren().get(0), vd.getChildren().get(2));
            int vl = vd.getChildren().get(1).getLineNumber();
            // Check for duplicate local or param with same name (8.4)
            if (ft.lookupLocal(vn, "local") != null || ft.lookupLocal(vn, "param") != null)
                addError(vl, "8.4", "error", "multiply declared variable '" + vn + "' in function");
            else
                ft.addEntry(new SymbolTableEntry("local", vn, vt, null, vl));
            // If inside a member function, check if local shadows a class data member (8.6)
            if (classTable != null && classTable.lookupLocal(vn, "data") != null)
                addError(vl, "8.6", "warning", "local variable '" + vn + "' in a member function shadows a data member of its class");
        }
    }

    private void checkMemberFuncConsistency() {
        // For each class in the global table...
        for (SymbolTableEntry ce : globalTable.getEntries()) {
            if (!"class".equals(ce.getKind()) || ce.getLink() == null) continue;
            // Check each function entry -- if link is null, it was declared but never defined (6.2)
            for (SymbolTableEntry fe : ce.getLink().getEntries()) {
                if ("function".equals(fe.getKind()) && fe.getLink() == null)
                    addError(fe.getLine(), "6.2", "error", "undefined member function declaration '" + ce.getName() + "::" + fe.getName() + "'");
            }
        }
    }

    private void checkInheritance() {
        // For each class in the global table...
        for (SymbolTableEntry ce : globalTable.getEntries()) {
            if (!"class".equals(ce.getKind()) || ce.getLink() == null) continue;
            SymbolTable ct = ce.getLink();
            // Gather ALL inherited entries (parents, grandparents, etc.) with cycle protection
            List<SymbolTableEntry> allInherited = getAllInheritedEntries(ct, new HashSet<>());
            Set<String> reportedData = new HashSet<>();
            Set<String> reportedFunc = new HashSet<>();
            for (SymbolTableEntry ie : allInherited) {
                // If ancestor has a data member, check if child also has one with same name (8.5)
                if ("data".equals(ie.getKind()) && !reportedData.contains(ie.getName())) {
                    SymbolTableEntry local = ct.lookupLocal(ie.getName(), "data");
                    if (local != null) {
                        addError(local.getLine(), "8.5", "warning", "shadowed inherited data member '" + ie.getName() + "'");
                        reportedData.add(ie.getName());
                    }
                }
                // If ancestor has a function, check if child has one with same name+sig (9.3)
                if ("function".equals(ie.getKind())) {
                    for (SymbolTableEntry lf : ct.getEntries()) {
                        String key = lf.getName() + ":" + lf.getType();
                        if ("function".equals(lf.getKind()) && lf.getName().equalsIgnoreCase(ie.getName())
                                && lf.getType().equals(ie.getType()) && !reportedFunc.contains(key)) {
                            addError(lf.getLine(), "9.3", "warning", "Overridden member function '" + lf.getName() + "'");
                            reportedFunc.add(key);
                        }
                    }
                }
            }
        }
    }

    // Recursively collects all data/function entries from inherited tables (parents, grandparents, etc.)
    private List<SymbolTableEntry> getAllInheritedEntries(SymbolTable table, Set<SymbolTable> visited) {
        List<SymbolTableEntry> result = new ArrayList<>();
        for (SymbolTable it : table.getInheritedTables()) {
            if (visited.contains(it)) continue; // cycle protection
            visited.add(it);
            for (SymbolTableEntry e : it.getEntries())
                if ("data".equals(e.getKind()) || "function".equals(e.getKind()))
                    result.add(e);
            result.addAll(getAllInheritedEntries(it, visited));
        }
        return result;
    }

    private void checkCircularDeps() {
        // Build a dependency graph: class name -> set of classes it inherits from
        Map<String, Set<String>> graph = new HashMap<>();
        for (SymbolTableEntry e : globalTable.getEntries()) {
            if (!"class".equals(e.getKind()) || e.getLink() == null) continue;
            Set<String> deps = new HashSet<>();
            for (SymbolTableEntry ie : e.getLink().getEntries()) {
                if ("inherit".equals(ie.getKind()) && !"none".equals(ie.getName()))
                    deps.add(ie.getName());
            }
            graph.put(e.getName(), deps);
        }
        // Run DFS cycle detection on the graph (14.1)
        Set<String> visited = new HashSet<>(), inStack = new HashSet<>();
        Set<String> reported = new HashSet<>();
        for (String cn : graph.keySet()) {
            if (!visited.contains(cn) && hasCycle(graph, cn, visited, inStack, reported)) {
                // cycle found and reported in hasCycle
            }
        }
    }

    private boolean hasCycle(Map<String, Set<String>> g, String n, Set<String> vis, Set<String> stk, Set<String> reported) {
        // If n is already on the recursion stack, we found a cycle
        if (stk.contains(n)) {
            if (!reported.contains(n)) {
                SymbolTableEntry e = globalTable.lookupLocal(n, "class");
                if (e != null) addError(e.getLine(), "14.1", "error", "Circular class dependency involving '" + n + "'");
                reported.add(n);
            }
            return true;
        }
        // If already fully visited, no cycle through this node
        if (vis.contains(n)) return false;
        // Mark as visiting and recurse into dependencies
        vis.add(n); stk.add(n);
        for (String dep : g.getOrDefault(n, Collections.emptySet())) {
            if (g.containsKey(dep) && hasCycle(g, dep, vis, stk, reported)) {
                stk.remove(n);
                return true;
            }
        }
        // Done visiting, remove from recursion stack
        stk.remove(n);
        return false;
    }

    // Builds a type string like "int", "float[5]", or "MyClass[][3]"
    String buildTypeStr(ASTNode typeNode, ASTNode dimList) {
        // Start with the base type (e.g. "integer" -> "int")
        String base = normalizeType(typeNode.getValue());
        // Append array dimensions -- known size like [5] or unknown like []
        if (dimList != null && dimList.getChildren() != null)
            for (ASTNode d : dimList.getChildren())
                base += "IntNum".equals(d.getType()) ? "[" + d.getValue() + "]" : "[]";
        return base;
    }

    // Builds a function signature string like "(int,float):void" or "(int):int"
    String buildSig(ASTNode fParams, ASTNode retType) {
        StringBuilder sb = new StringBuilder("(");
        // Append comma-separated parameter types
        if (fParams.getChildren() != null) {
            for (int i = 0; i < fParams.getChildren().size(); i++) {
                if (i > 0) sb.append(",");
                ASTNode p = fParams.getChildren().get(i);
                sb.append(buildTypeStr(p.getChildren().get(0), p.getChildren().get(2)));
            }
        }
        // Append return type after ":"
        sb.append("):");
        if ("Void".equals(retType.getType())) sb.append("void");
        else sb.append(normalizeType(retType.getValue()));
        return sb.toString();
    }

    // Normalizes type names: "integer" -> "int", class names -> canonical casing
    String normalizeType(String raw) {
        if (raw == null) return "";
        if ("integer".equals(raw)) return "int";
        if ("float".equals(raw) || "void".equals(raw)) return raw;
        // If it matches a declared class name, return the canonical (declared) casing
        for (SymbolTableEntry e : globalTable.getEntries())
            if ("class".equals(e.getKind()) && e.getName().equalsIgnoreCase(raw)) return e.getName();
        return raw;
    }

    private void addError(int line, String code, String level, String msg) {
        errors.add(new String[]{String.valueOf(line), code, level, msg});
    }

    public SymbolTable getGlobalTable() { return globalTable; }
    public List<String[]> getErrors() { return errors; }
    public Map<ASTNode, SymbolTable> getFuncTableMap() { return funcTableMap; }
}
