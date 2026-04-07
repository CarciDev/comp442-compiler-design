package src.SemanticAnalyzer;

import src.SyntacticalAnalyzer.ASTNode;
import src.SyntacticalAnalyzer.Visitor;
import java.util.*;

public class SemanticChecker implements Visitor {
    private final SymbolTable globalTable;
    private final Map<ASTNode, SymbolTable> funcTableMap;
    private final List<String[]> errors = new ArrayList<>();
    private SymbolTable currentScope;
    private String currentReturnType;

    public SemanticChecker(SymbolTable globalTable, Map<ASTNode, SymbolTable> funcTableMap) {
        this.globalTable = globalTable;
        this.funcTableMap = funcTableMap;
    }

    @Override
    public void visit(ASTNode node) {
        // Entry point -- only runs on the root "Prog" node
        if (!"Prog".equals(node.getType())) return;
        ASTNode funcDefList = node.getChildren().get(1);
        ASTNode progBlock = node.getChildren().get(2);
        // Visit all function definitions, then the main block
        if (funcDefList.getChildren() != null)
            for (ASTNode fd : funcDefList.getChildren()) visitFuncDef(fd);
        visitMainBlock(progBlock);
    }

    private void visitFuncDef(ASTNode node) {
        // Look up this function's symbol table via the funcTableMap bridge
        SymbolTable ft = funcTableMap.get(node);
        if (ft == null) return;
        // Set the current scope and return type for type checking within this function
        ASTNode funcHead = node.getChildren().get(0);
        ASTNode retTypeNode = funcHead.getChildren().get(3);
        currentScope = ft;
        currentReturnType = "Void".equals(retTypeNode.getType()) ? "void" : normalizeType(retTypeNode.getValue());
        // Visit the function body's statements
        ASTNode funcBody = node.getChildren().get(1);
        ASTNode statList = funcBody.getChildren().get(1);
        visitStatements(statList);
    }

    private void visitMainBlock(ASTNode progBlock) {
        // Look up main's symbol table via funcTableMap, fallback to searching global table
        SymbolTable mt = funcTableMap.get(progBlock);
        if (mt == null) {
            for (SymbolTableEntry e : globalTable.getEntries())
                if ("function".equals(e.getKind()) && "main".equals(e.getName())) { mt = e.getLink(); break; }
        }
        if (mt == null) return;
        // Set scope to main, no return type for main
        currentScope = mt;
        currentReturnType = null;
        if (progBlock.getChildren() == null) return;
        // Visit all statements in main, skip VarDecls (already handled by SymbolTableBuilder)
        for (ASTNode child : progBlock.getChildren())
            if (!"VarDecl".equals(child.getType())) visitStatement(child);
    }

    private void visitStatements(ASTNode node) {
        if (node == null || node.getChildren() == null) return;
        for (ASTNode s : node.getChildren()) visitStatement(s);
    }

    private void visitStatement(ASTNode node) {
        if (node == null) return;
        switch (node.getType()) {
            // Check assignment: LHS and RHS types must match (10.2)
            case "AssignStat": {
                String lhs = inferType(node.getChildren().get(0));
                String rhs = inferType(node.getChildren().get(1));
                if (lhs != null && rhs != null && !lhs.equals(rhs))
                    addError(node.getLineNumber(), "10.2", "error", "Type error in assignment statement");
                break;
            }
            // Check return: expression type must match function's declared return type (10.3)
            case "ReturnStat": {
                String et = inferType(node.getChildren().get(0));
                if (currentReturnType != null && et != null && !currentReturnType.equals(et))
                    addError(node.getLineNumber(), "10.3", "error", "Type error in return statement");
                break;
            }
            // If/while: check condition expression, then recurse into branches/body
            case "IfStat":
                inferType(node.getChildren().get(0));
                visitStatement(node.getChildren().get(1));
                visitStatement(node.getChildren().get(2));
                break;
            case "WhileStat":
                inferType(node.getChildren().get(0));
                visitStatement(node.getChildren().get(1));
                break;
            // Read/write: check the expression type
            case "WriteStat": case "ReadStat":
                inferType(node.getChildren().get(0));
                break;
            // Recurse into statement blocks
            case "StatBlock": case "StatList":
                visitStatements(node);
                break;
            // Standalone function call or dot expression -- infer to trigger checks
            case "FuncCall":
                inferType(node);
                break;
            case "Dot":
                inferType(node);
                break;
            default: break;
        }
    }

    // Infers and returns the type of an AST expression node, dispatching to specific handlers
    private String inferType(ASTNode node) {
        if (node == null) return null;
        switch (node.getType()) {
            case "IntNum": return "int";                          // literal integer
            case "FloatNum": return "float";                      // literal float
            case "DataMember": return inferDataMember(node);      // variable or array access
            case "FuncCall": return inferFuncCall(node, null);    // free function call
            case "Dot": return inferDot(node);                    // member access (obj.member)
            case "AddOp": case "MultOp": return inferBinOp(node); // arithmetic operators
            case "RelExpr": return inferRelExpr(node);            // relational operators
            case "Sign": return node.getChildren() != null && !node.getChildren().isEmpty() ? inferType(node.getChildren().get(0)) : null;
            case "Not": return node.getChildren() != null && !node.getChildren().isEmpty() ? inferType(node.getChildren().get(0)) : null;
            case "Epsilon": return null;
            default: return null;
        }
    }

    private String inferDataMember(ASTNode node) {
        String name = node.getChildren().get(0).getValue();
        ASTNode indiceList = node.getChildren().get(1);
        // Look up variable in current scope, inherited tables, and parent scopes
        SymbolTableEntry entry = lookupVar(name, currentScope);
        if (entry == null) {
            // 11.1: variable not found anywhere
            addError(node.getLineNumber(), "11.1", "error", "Undeclared local variable '" + name + "'");
            return null;
        }
        String type = entry.getType();
        int declDims = countDims(type);
        int usedDims = (indiceList != null && indiceList.getChildren() != null) ? indiceList.getChildren().size() : 0;
        if (usedDims > 0) {
            // 13.1: number of indices used must match declared dimensions
            if (usedDims != declDims)
                addError(node.getLineNumber(), "13.1", "error", "Use of array with wrong number of dimensions");
            // 13.2: each index must be an integer
            for (ASTNode idx : indiceList.getChildren()) {
                String idxType = inferType(idx);
                if (idxType != null && !"int".equals(idxType))
                    addError(idx.getLineNumber(), "13.2", "error", "Array index is not an integer");
            }
            // After indexing, the type is the base type (e.g. int[5] indexed -> int)
            return getBaseType(type);
        }
        return type;
    }

    private String inferFuncCall(ASTNode node, SymbolTable searchScope) {
        String fname = node.getChildren().get(0).getValue();
        ASTNode aparams = node.getChildren().get(1);
        int argCount = (aparams != null && aparams.getChildren() != null) ? aparams.getChildren().size() : 0;
        SymbolTableEntry fe = null;
        if (searchScope != null) {
            // Member function call (via dot) -- search the class table and inherited tables
            // First try to find an overload with matching param count
            for (SymbolTableEntry e : searchScope.getEntries())
                if ("function".equals(e.getKind()) && e.getName().equalsIgnoreCase(fname)
                        && getParamTypesFromSig(e.getType()).size() == argCount) { fe = e; break; }
            if (fe == null) fe = searchScope.lookupLocal(fname, "function");
            if (fe == null) fe = searchScope.lookupInherited(fname, "function");
            if (fe == null) {
                // 11.3: member function not found in class or inherited classes
                addError(node.getLineNumber(), "11.3", "error", "Undeclared member function '" + fname + "'");
                return null;
            }
        } else {
            // Free function call -- search the global table
            // First try to find an overload with matching param count
            for (SymbolTableEntry e : globalTable.getEntries())
                if ("function".equals(e.getKind()) && e.getName().equalsIgnoreCase(fname)
                        && getParamTypesFromSig(e.getType()).size() == argCount) { fe = e; break; }
            // Fallback: any function with that name
            if (fe == null)
                for (SymbolTableEntry e : globalTable.getEntries())
                    if ("function".equals(e.getKind()) && e.getName().equalsIgnoreCase(fname)) { fe = e; break; }
            if (fe == null) {
                // 11.4: free function not found in global scope
                addError(node.getLineNumber(), "11.4", "error", "Undeclared/undefined free function '" + fname + "'");
                return null;
            }
        }
        // Validate parameter count and types
        checkCallParams(node, fe, aparams);
        return getReturnFromSig(fe.getType());
    }

    private String inferDot(ASTNode node) {
        ASTNode left = node.getChildren().get(0);
        ASTNode right = node.getChildren().get(1);
        // Infer the type of the left side (the object)
        String leftType = inferType(left);
        if (leftType == null) return null;
        // 15.1: dot operator can't be used on primitives
        if ("int".equals(leftType) || "float".equals(leftType)) {
            addError(node.getLineNumber(), "15.1", "error", "\".\" operator used on non-class type");
            return null;
        }
        // 11.5: the left side's type must be a declared class
        SymbolTableEntry classEntry = findClass(leftType);
        if (classEntry == null || classEntry.getLink() == null) {
            addError(node.getLineNumber(), "11.5", "error", "Undeclared class '" + leftType + "'");
            return null;
        }
        SymbolTable classTable = classEntry.getLink();
        // Right side is either a data member access or a member function call
        if ("DataMember".equals(right.getType())) {
            String mn = right.getChildren().get(0).getValue();
            // Look up in class table, then inherited tables
            SymbolTableEntry me = classTable.lookupLocal(mn, "data");
            if (me == null) me = classTable.lookupInherited(mn, "data");
            if (me == null) {
                // 11.2: member variable not found
                addError(right.getLineNumber(), "11.2", "error", "Undeclared member variable '" + mn + "'");
                return null;
            }
            return me.getType();
        } else if ("FuncCall".equals(right.getType())) {
            // Delegate to inferFuncCall with the class table as search scope
            return inferFuncCall(right, classTable);
        }
        return null;
    }

    // Infer type of binary arithmetic operation (AddOp, MultOp)
    private String inferBinOp(ASTNode node) {
        String lt = inferType(node.getChildren().get(0));
        String rt = inferType(node.getChildren().get(1));
        // 10.1: both operands must have the same type
        if (lt != null && rt != null && !lt.equals(rt))
            addError(node.getLineNumber(), "10.1", "error", "Type error in expression");
        return lt != null ? lt : rt;
    }

    // Infer type of relational expression (e.g. a < b)
    private String inferRelExpr(ASTNode node) {
        // Left is child 0, operator is child 1, right is child 2
        String lt = inferType(node.getChildren().get(0));
        String rt = inferType(node.getChildren().get(2));
        // 10.1: both operands must have the same type
        if (lt != null && rt != null && !lt.equals(rt))
            addError(node.getLineNumber(), "10.1", "error", "Type error in expression");
        return lt != null ? lt : rt;
    }

    private void checkCallParams(ASTNode node, SymbolTableEntry fe, ASTNode aparams) {
        // Parse declared param types from signature and get actual arguments
        List<String> declTypes = getParamTypesFromSig(fe.getType());
        List<ASTNode> args = (aparams != null && aparams.getChildren() != null) ? aparams.getChildren() : Collections.emptyList();
        // 12.1: argument count must match parameter count
        if (declTypes.size() != args.size()) {
            addError(node.getLineNumber(), "12.1", "error", "Function call with wrong number of parameters");
            return;
        }
        // Check each argument against the corresponding parameter
        for (int i = 0; i < declTypes.size(); i++) {
            String at = inferType(args.get(i));
            String pt = declTypes.get(i);
            if (at != null && pt != null) {
                String ab = getBaseType(at), pb = getBaseType(pt);
                // 12.2: base types must match
                if (!ab.equalsIgnoreCase(pb))
                    addError(node.getLineNumber(), "12.2", "error", "Function call with wrong type of parameters");
                // 13.3: if it's an array param, dimensions must match
                else if (countDims(pt) > 0 && countDims(at) != countDims(pt))
                    addError(node.getLineNumber(), "13.3", "error", "Array parameter using wrong number of dimensions");
            }
        }
    }

    // --- Helpers ---

    // Searches for a variable (data, local, or param) by walking: current scope -> inherited -> parent
    private SymbolTableEntry lookupVar(String name, SymbolTable scope) {
        if (scope == null) return null;
        // Check current scope's entries
        for (SymbolTableEntry e : scope.getEntries())
            if (e.getName().equalsIgnoreCase(name) && ("data".equals(e.getKind()) || "local".equals(e.getKind()) || "param".equals(e.getKind())))
                return e;
        // Check inherited tables (for class members)
        for (SymbolTable it : scope.getInheritedTables()) {
            SymbolTableEntry e = lookupVar(name, it);
            if (e != null) return e;
        }
        // Walk up to parent scope (e.g. function -> class -> global)
        return lookupVar(name, scope.getParent());
    }

    // Finds a class entry in the global table by name
    private SymbolTableEntry findClass(String name) {
        for (SymbolTableEntry e : globalTable.getEntries())
            if ("class".equals(e.getKind()) && e.getName().equalsIgnoreCase(name)) return e;
        return null;
    }

    // Extracts the return type from a signature string, e.g. "(int,float):void" -> "void"
    private String getReturnFromSig(String sig) {
        if (sig == null) return null;
        int c = sig.lastIndexOf(':');
        return c >= 0 && c + 1 < sig.length() ? sig.substring(c + 1) : "";
    }

    // Extracts the parameter types from a signature string, e.g. "(int,float):void" -> ["int", "float"]
    private List<String> getParamTypesFromSig(String sig) {
        if (sig == null) return Collections.emptyList();
        int lp = sig.indexOf('('), rp = sig.indexOf(')');
        if (lp < 0 || rp < 0 || rp <= lp + 1) return Collections.emptyList();
        return Arrays.asList(sig.substring(lp + 1, rp).split(","));
    }

    // Counts the number of array dimensions in a type string, e.g. "int[5][3]" -> 2
    private int countDims(String type) {
        int c = 0;
        for (char ch : type.toCharArray()) if (ch == '[') c++;
        return c;
    }

    // Returns the base type without array dimensions, e.g. "int[5][3]" -> "int"
    private String getBaseType(String type) {
        int b = type.indexOf('[');
        return b >= 0 ? type.substring(0, b) : type;
    }

    // Normalizes type names: "integer" -> "int", class names -> canonical casing
    private String normalizeType(String raw) {
        if (raw == null) return "";
        if ("integer".equals(raw)) return "int";
        if ("float".equals(raw) || "void".equals(raw)) return raw;
        SymbolTableEntry ce = findClass(raw);
        return ce != null ? ce.getName() : raw;
    }

    private void addError(int line, String code, String level, String msg) {
        errors.add(new String[]{String.valueOf(line), code, level, msg});
    }

    public List<String[]> getErrors() { return errors; }
}
