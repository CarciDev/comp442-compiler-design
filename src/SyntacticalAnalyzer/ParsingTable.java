package src.SyntacticalAnalyzer;// LL(1) Parsing Table - Auto-generated
// Usage: table.get("NONTERMINAL").get("terminal") returns int[] of RHS symbols

import java.util.*;

public class ParsingTable {

    // =========================================================================
    // AUGMENTED GRAMMAR — SEMANTIC ACTIONS FOR AST GENERATION
    // =========================================================================
    //
    // =========================================================================

    // =========================================================================
    // SEMANTIC ACTION REFERENCE
    // =========================================================================
    //
    // All semantic action markers start with "@" and are pushed onto the
    // parsing stack as part of the RHS. When popped, they trigger operations
    // on a SEPARATE semantic stack (not the parsing stack).
    //
    // --- LEAF CREATORS (push one AST leaf onto semantic stack) ---
    //   @MAKE_ID         - id leaf from last matched token
    //   @MAKE_INTNUM      - intNum leaf from last matched token
    //   @MAKE_FLOATNUM    - floatNum leaf from last matched token
    //   @MAKE_TYPE        - type leaf (integer/float/id) from last matched token
    //   @MAKE_VISIBILITY  - visibility leaf (public/private) from last matched token
    //   @MAKE_ADDOP       - addOp leaf (+/-/or) from last matched token
    //   @MAKE_MULTOP      - multOp leaf (*/ / /and) from last matched token
    //   @MAKE_RELOP       - relOp leaf (eq/neq/lt/gt/leq/geq)
    //   @MAKE_SIGN        - sign leaf (+/-) from last matched token
    //   @MAKE_VOID        - void leaf (keyword)
    //   @MAKE_NODE_EPSILON - epsilon leaf (e.g. unsized array dimension [])
    //
    // --- EPSILON MARKER (for variable-length list collection) ---
    //   @PUSH_EPSILON    - push epsilon marker onto semantic stack
    //                       (used with pop-until-epsilon in variable-length list collectors)
    //
    // --- FIXED-ARITY SUBTREE CREATORS (pop N children, create parent, push) ---
    //   @MAKE_PROG        - pop 3: classList, funcDefList, mainBody
    //   @MAKE_CLASSDECL   - pop 3: className, inheritList, memberList
    //   @MAKE_MEMBERDECL  - pop 2: visibility, declaration
    //   @MAKE_FUNCDECL    - pop 3: funcName, fParamsList, returnType
    //   @MAKE_FUNCDEF     - pop 2: funcHead, funcBody
    //   @MAKE_FUNCHEAD    - pop 4: scopeSpec, funcName, fParamsList, returnType
    //   @MAKE_FUNCBODY    - pop 2: varDeclList, statList
    //   @MAKE_VARDECL     - pop 3: type, varName, dimList
    //   @MAKE_FPARAM      - pop 3: type, paramName, dimList
    //   @MAKE_ASSIGNSTAT  - pop 2: variable, expr
    //   @MAKE_IFSTAT      - pop 3: condition, thenBlock, elseBlock
    //   @MAKE_WHILESTAT   - pop 2: condition, loopBody
    //   @MAKE_READSTAT    - pop 1: variable
    //   @MAKE_WRITESTAT   - pop 1: expr
    //   @MAKE_RETURNSTAT  - pop 1: expr
    //   @MAKE_RELEXPR     - pop 3: leftArith, relOp, rightArith
    //   @MAKE_ADDNODE     - pop 3: left, addOp, right -> addOp node (left-assoc)
    //   @MAKE_MULTNODE    - pop 3: left, multOp, right -> multOp node (left-assoc)
    //   @MAKE_NOT         - pop 1: factor -> not(factor)
    //   @MAKE_SIGNFACTOR  - pop 2: sign, factor -> sign(factor)
    //   @MAKE_VAR         - pop 2: id, indiceList -> var(id, indiceList)
    //   @MAKE_DOT         - pop 2: left, right -> dot(left, right)
    //   @MAKE_FUNCCALL    - pop 2: id, aParamsList -> functionCall(id, aParams)
    //
    // --- VARIABLE-ARITY SUBTREE CREATORS (pop until epsilon) ---
    //   @MAKE_CLASSLIST    - pop until epsilon -> classList(classDecl*)
    //   @MAKE_FUNCDEFLIST  - pop until epsilon -> funcDefList(funcDef*)
    //   @MAKE_INHERITLIST  - pop until epsilon -> inheritList(id*)
    //   @MAKE_MEMBERLIST   - pop until epsilon -> memberList(memberDecl*)
    //   @MAKE_VARDECLLIST  - pop until epsilon -> varDeclList(varDecl*)
    //   @MAKE_STATLIST     - pop until epsilon -> statList(statement*)
    //   @MAKE_STATBLOCK    - pop until epsilon -> statBlock(statement*)
    //   @MAKE_DIMLIST      - pop until epsilon -> dimList(intNum|epsilon*)
    //   @MAKE_INDICELIST   - pop until epsilon -> indiceList(arithExpr*)
    //   @MAKE_FPARAMSLIST  - pop until epsilon -> fParamsList(fParam*)
    //   @MAKE_APARAMSLIST  - pop until epsilon -> aParamsList(expr*)
    //
    // --- SPECIAL ---
    //   @PUSH_NULLSCOPE    - for free functions: pop id, push epsilon, push id
    //                        (inserts epsilon scope BELOW func name on stack)
    //   @RETYPE_TOP_TO_TYPE - change top-of-stack node type from "id" to "type"
    //                         (used when MEMBERDECL id turns out to be a type name)
    //
    // =========================================================================
    // PARSER CHANGES REQUIRED:
    // =========================================================================
    //
    // 1. When splitting a RHS string, recognize "@" prefix tokens as semantic
    //    action symbols. They are neither terminals nor non-terminals.
    //
    // 2. In the main parse loop, add a third case:
    //      if top-of-stack starts with "@" -> call handleSemanticAction(action)
    //    This goes alongside the existing terminal-match and non-terminal cases.
    //
    // 3. Rules where "EPSILON" was replaced with semantic actions (rules 4, 24,
    //    49, 51, 99) now have RHS containing ONLY "@" symbols. The parser must
    //    still treat these as epsilon productions for parsing purposes — they
    //    consume no input tokens. Check: if all symbols in RHS start with "@",
    //    it's an epsilon production with side effects.
    //
    // 4. Maintain a `lastMatchedToken` reference so @MAKE_ID, @MAKE_INTNUM,
    //    @MAKE_TYPE, etc. can create leaf nodes from the most recently matched
    //    terminal token.
    //
    // =========================================================================

    // All grammar rules indexed by number — augmented with semantic actions
    public static final String[][] rules = {

            // ===================== OPERATORS & SIGNS =====================
            // Each operator terminal creates a leaf node for use by
            // @MAKE_ADDNODE / @MAKE_MULTNODE / @MAKE_RELEXPR later.

            /*   0 */ {"ADDOP", "plus @MAKE_ADDOP"},
            /*   1 */ {"ADDOP", "minus @MAKE_ADDOP"},
            /*   2 */ {"ADDOP", "or @MAKE_ADDOP"},

            // ===================== ACTUAL PARAMETERS =====================
            // Epsilon marker before first expr; each EXPR pushed between epsilon marker
            // and @MAKE_APARAMSLIST. APARAMSTAIL just pushes more EXPRs.

            /*   3 */ {"APARAMS", "@PUSH_EPSILON EXPR REPTAPARAMS1 @MAKE_APARAMSLIST"},
            /*   4 */ {"APARAMS", "@PUSH_EPSILON @MAKE_APARAMSLIST"},  // empty params
            /*   5 */ {"APARAMSTAIL", "comma EXPR"},                   // pushes one more expr

            // ===================== ARITHMETIC EXPRESSION =================
            // Left-recursion eliminated: TERM stays on stack as "left operand".
            // Each RIGHTRECARITHEXPR iteration pops left+op+right via @MAKE_ADDNODE
            // and pushes the result back as the new left operand.
            //
            // Example: a + b - c
            //   TERM(a) -> push a
            //   ADDOP(+) -> push addOp(+);  TERM(b) -> push b
            //   @MAKE_ADDNODE -> pop b, +, a -> push addNode(+, a, b)
            //   ADDOP(-) -> push addOp(-);  TERM(c) -> push c
            //   @MAKE_ADDNODE -> pop c, -, addNode(+,a,b) -> push addNode(-, addNode(+,a,b), c)
            //   EPSILON -> done.  Result: left-associative tree.

            /*   6 */ {"ARITHEXPR", "TERM RIGHTRECARITHEXPR"},

            // ===================== ARRAY SIZE ============================
            // Each ARRAYSIZE pushes one node (intNum or epsilon for [])
            // onto the stack. Collected later by @MAKE_DIMLIST.

            /*   7 */ {"ARRAYSIZE", "lsqbr ARRAYSIZE2"},
            /*   8 */ {"ARRAYSIZE2", "intnum @MAKE_INTNUM rsqbr"},       // sized: [2]
            /*   9 */ {"ARRAYSIZE2", "rsqbr @MAKE_NODE_EPSILON"},        // unsized: []

            /*  10 */ {"ASSIGNOP", "assign"},  // not used directly in AST; assignment handled in STMTIDNEST

            // ===================== CLASS DECLARATION =====================
            // class id OPTCLASSDECL2 { members } ;
            //   @MAKE_ID          -> push className
            //   OPTCLASSDECL2     -> push inheritList (even if empty)
            //   @PUSH_EPSILON    -> epsilon marker for member list
            //   REPTCLASSDECL4    -> pushes memberDecl nodes
            //   @MAKE_MEMBERLIST  -> pop until epsilon -> memberList
            //   @MAKE_CLASSDECL   -> pop 3: name, inheritList, memberList

            /*  11 */ {"CLASSDECL", "class id @MAKE_ID OPTCLASSDECL2 lcurbr @PUSH_EPSILON REPTCLASSDECL4 @MAKE_MEMBERLIST rcurbr semi @MAKE_CLASSDECL"},

            // ===================== EXPRESSION ============================
            // EXPR -> ARITHEXPR EXPR2
            // If EXPR2 has a RELOP, creates relExpr. Otherwise arithExpr passes through.

            /*  12 */ {"EXPR", "ARITHEXPR EXPR2"},
            /*  13 */ {"EXPR2", "RELOP ARITHEXPR @MAKE_RELEXPR"},  // pop right, relOp, left -> relExpr
            /*  14 */ {"EXPR2", "EPSILON"},                        // arithExpr passes through

            // ===================== FACTOR ================================
            // Starting with id: FACTOR2 decides variable vs function call,
            // REPTVARIABLEORFUNCTIONCALL adds dot-chain continuation.

            /*  15 */ {"FACTOR", "id @MAKE_ID FACTOR2 REPTVARIABLEORFUNCTIONCALL"},
            /*  16 */ {"FACTOR", "intnum @MAKE_INTNUM"},
            /*  17 */ {"FACTOR", "floatnum @MAKE_FLOATNUM"},
            /*  18 */ {"FACTOR", "lpar ARITHEXPR rpar"},           // parenthesized: subtree passes through
            /*  19 */ {"FACTOR", "not FACTOR @MAKE_NOT"},          // pop factor -> not(factor)
            /*  20 */ {"FACTOR", "SIGN FACTOR @MAKE_SIGNFACTOR"},  // pop factor, sign -> sign(factor)

            // FACTOR2: determines if id is function call or variable
            //   Rule 21: id(...) -> function call
            //   Rule 22: id[i][j] -> variable with indices

            /*  21 */ {"FACTOR2", "lpar APARAMS rpar @MAKE_FUNCCALL"},                       // pop aParams, id -> funcCall
            /*  22 */ {"FACTOR2", "@PUSH_EPSILON REPTIDNEST1 @MAKE_INDICELIST @MAKE_VAR"},  // pop indiceList, id -> var

            // ===================== FORMAL PARAMETERS =====================
            // Outer epsilon marker for the fParamsList. First param built inline,
            // additional params via FPARAMSTAIL (each pushes one fParam).
            //
            // Rule 23 trace: @PUSH_EPSILON(list) TYPE id @MAKE_ID
            //   @PUSH_EPSILON(dims) REPTFPARAMS2 @MAKE_DIMLIST @MAKE_FPARAM
            //   REPTFPARAMS3(more fParams) @MAKE_FPARAMSLIST

            /*  23 */ {"FPARAMS", "@PUSH_EPSILON TYPE id @MAKE_ID @PUSH_EPSILON REPTFPARAMS2 @MAKE_DIMLIST @MAKE_FPARAM REPTFPARAMS3 @MAKE_FPARAMSLIST"},
            /*  24 */ {"FPARAMS", "@PUSH_EPSILON @MAKE_FPARAMSLIST"},  // empty param list
            /*  25 */ {"FPARAMSTAIL", "comma TYPE id @MAKE_ID @PUSH_EPSILON REPTFPARAMSTAIL3 @MAKE_DIMLIST @MAKE_FPARAM"},

            // ===================== FUNCTION BODY =========================
            // OPTFUNCBODY0 pushes varDeclList (even if empty).
            // REPTFUNCBODY2 collects statements between epsilon marker and @MAKE_STATLIST.
            // @MAKE_FUNCBODY pops statList and varDeclList.

            /*  26 */ {"FUNCBODY", "OPTFUNCBODY0 do @PUSH_EPSILON REPTFUNCBODY2 @MAKE_STATLIST end @MAKE_FUNCBODY"},

            // ===================== FUNCTION DECLARATION (in class) =======
            // Used standalone and via MEMBERDECL2 rule 43.

            /*  27 */ {"FUNCDECL", "id @MAKE_ID lpar FPARAMS rpar colon FUNCDECL2 @MAKE_FUNCDECL"},
            /*  28 */ {"FUNCDECL2", "TYPE semi"},          // TYPE pushes type leaf via @MAKE_TYPE
            /*  29 */ {"FUNCDECL2", "void @MAKE_VOID semi"},

            // ===================== FUNCTION DEFINITION ====================
            // FUNCHEAD pushes funcHead subtree, FUNCBODY pushes funcBody subtree.

            /*  30 */ {"FUNCDEF", "FUNCHEAD FUNCBODY semi @MAKE_FUNCDEF"},

            // ===================== FUNCTION HEAD ==========================
            // Rule 31 pushes the first id. FUNCHEAD1 determines if scoped.
            //
            // Rule 32 (member fn): id1 = scope (already on stack), id2 = funcName
            //   coloncolon id @MAKE_ID lpar FPARAMS rpar colon FUNCHEAD2 @MAKE_FUNCHEAD
            //   Stack: [scope, funcName, fParamsList, returnType] -> pop 4
            //
            // Rule 33 (free fn): id = funcName (already on stack), no scope
            //   @PUSH_NULLSCOPE swaps: pop funcName, push epsilon, push funcName
            //   Stack: [epsilon, funcName, fParamsList, returnType] -> pop 4

            /*  31 */ {"FUNCHEAD", "id @MAKE_ID FUNCHEAD1"},
            /*  32 */ {"FUNCHEAD1", "coloncolon id @MAKE_ID lpar FPARAMS rpar colon FUNCHEAD2 @MAKE_FUNCHEAD"},
            /*  33 */ {"FUNCHEAD1", "@PUSH_NULLSCOPE lpar FPARAMS rpar colon FUNCHEAD2 @MAKE_FUNCHEAD"},
            /*  34 */ {"FUNCHEAD2", "TYPE"},               // TYPE pushes return type leaf
            /*  35 */ {"FUNCHEAD2", "void @MAKE_VOID"},    // push void return type

            // ===================== IDNEST (dot chain in expressions) ======
            // Used by REPTVARIABLEORFUNCTIONCALL to extend a factor's dot chain.
            // After IDNEST2, @MAKE_DOT combines new var/funcCall with previous.
            //
            // Rule 37: .id(args)  -> funcCall + dot with previous
            // Rule 38: .id[i][j]  -> var + dot with previous

            /*  36 */ {"IDNEST", "dot id @MAKE_ID IDNEST2"},
            /*  37 */ {"IDNEST2", "lpar APARAMS rpar @MAKE_FUNCCALL @MAKE_DOT"},
            /*  38 */ {"IDNEST2", "@PUSH_EPSILON REPTIDNEST1 @MAKE_INDICELIST @MAKE_VAR @MAKE_DOT"},

            /*  39 */ {"INDICE", "lsqbr ARITHEXPR rsqbr"},  // ARITHEXPR pushes index subtree

            // ===================== MEMBER DECLARATION (in class) ==========
            // Rule 40: starts with id — could be funcDecl or varDecl (class-type)
            // Rule 41/42: starts with float/integer — always varDecl
            //
            // Rule 44 detail: the id from rule 40 is actually the TYPE name.
            //   @RETYPE_TOP_TO_TYPE changes it from "id" to "type" node.

            /*  40 */ {"MEMBERDECL", "id @MAKE_ID MEMBERDECL2"},
            /*  41 */ {"MEMBERDECL", "float @MAKE_TYPE id @MAKE_ID @PUSH_EPSILON REPTVARDECL2 @MAKE_DIMLIST semi @MAKE_VARDECL"},
            /*  42 */ {"MEMBERDECL", "integer @MAKE_TYPE id @MAKE_ID @PUSH_EPSILON REPTVARDECL2 @MAKE_DIMLIST semi @MAKE_VARDECL"},
            /*  43 */ {"MEMBERDECL2", "lpar FPARAMS rpar colon FUNCDECL2 @MAKE_FUNCDECL"},  // funcDecl: id was funcName
            /*  44 */ {"MEMBERDECL2", "@RETYPE_TOP_TO_TYPE id @MAKE_ID @PUSH_EPSILON REPTVARDECL2 @MAKE_DIMLIST semi @MAKE_VARDECL"},  // varDecl: id was type

            // ===================== MULTIPLICATIVE OPERATORS ===============

            /*  45 */ {"MULTOP", "mult @MAKE_MULTOP"},
            /*  46 */ {"MULTOP", "div @MAKE_MULTOP"},
            /*  47 */ {"MULTOP", "and @MAKE_MULTOP"},

            // ===================== OPTIONAL INHERITS (class) ==============
            // Epsilon marker before first inherited id; REPTOPTCLASSDECL22 pushes more ids.

            /*  48 */ {"OPTCLASSDECL2", "inherits @PUSH_EPSILON id @MAKE_ID REPTOPTCLASSDECL22 @MAKE_INHERITLIST"},
            /*  49 */ {"OPTCLASSDECL2", "@PUSH_EPSILON @MAKE_INHERITLIST"},  // no inherits -> empty list

            // ===================== OPTIONAL LOCAL VARS (func body) ========
            // Epsilon marker before varDecl repetition.

            /*  50 */ {"OPTFUNCBODY0", "local @PUSH_EPSILON REPTOPTFUNCBODY01 @MAKE_VARDECLLIST"},
            /*  51 */ {"OPTFUNCBODY0", "@PUSH_EPSILON @MAKE_VARDECLLIST"},  // no local vars -> empty list

            // ===================== PROGRAM ================================
            // Top-level: classList + funcDefList + main funcBody
            //   Each list uses epsilon marker + collector pattern.

            /*  52 */ {"PROG", "@PUSH_EPSILON REPTPROG0 @MAKE_CLASSLIST @PUSH_EPSILON REPTPROG1 @MAKE_FUNCDEFLIST main FUNCBODY @MAKE_PROG"},

            // ===================== RELATIONAL EXPRESSION ==================
            // Used in if/while conditions. Always produces relExpr node.

            /*  53 */ {"RELEXPR", "ARITHEXPR RELOP ARITHEXPR @MAKE_RELEXPR"},

            // ===================== RELATIONAL OPERATORS ===================

            /*  54 */ {"RELOP", "eq @MAKE_RELOP"},
            /*  55 */ {"RELOP", "neq @MAKE_RELOP"},
            /*  56 */ {"RELOP", "lt @MAKE_RELOP"},
            /*  57 */ {"RELOP", "gt @MAKE_RELOP"},
            /*  58 */ {"RELOP", "leq @MAKE_RELOP"},
            /*  59 */ {"RELOP", "geq @MAKE_RELOP"},

            // ===================== REPETITIONS (no actions needed) =========
            // These just repeat their child rule. List collection is handled
            // by the epsilon marker + collector in the parent rule.

            /*  60 */ {"REPTAPARAMS1", "APARAMSTAIL REPTAPARAMS1"},
            /*  61 */ {"REPTAPARAMS1", "EPSILON"},

            // Each iteration: VISIBILITY pushes vis leaf, MEMBERDECL pushes decl,
            // @MAKE_MEMBERDECL combines them into one memberDecl node.
            /*  62 */ {"REPTCLASSDECL4", "VISIBILITY MEMBERDECL @MAKE_MEMBERDECL REPTCLASSDECL4"},
            /*  63 */ {"REPTCLASSDECL4", "EPSILON"},

            /*  64 */ {"REPTFPARAMS2", "ARRAYSIZE REPTFPARAMS2"},
            /*  65 */ {"REPTFPARAMS2", "EPSILON"},
            /*  66 */ {"REPTFPARAMS3", "FPARAMSTAIL REPTFPARAMS3"},
            /*  67 */ {"REPTFPARAMS3", "EPSILON"},
            /*  68 */ {"REPTFPARAMSTAIL3", "ARRAYSIZE REPTFPARAMSTAIL3"},
            /*  69 */ {"REPTFPARAMSTAIL3", "EPSILON"},
            /*  70 */ {"REPTFUNCBODY2", "STATEMENT REPTFUNCBODY2"},
            /*  71 */ {"REPTFUNCBODY2", "EPSILON"},
            /*  72 */ {"REPTIDNEST1", "INDICE REPTIDNEST1"},     // each INDICE pushes one arithExpr
            /*  73 */ {"REPTIDNEST1", "EPSILON"},
            /*  74 */ {"REPTOPTCLASSDECL22", "comma id @MAKE_ID REPTOPTCLASSDECL22"},
            /*  75 */ {"REPTOPTCLASSDECL22", "EPSILON"},
            /*  76 */ {"REPTOPTFUNCBODY01", "VARDECL REPTOPTFUNCBODY01"},
            /*  77 */ {"REPTOPTFUNCBODY01", "EPSILON"},
            /*  78 */ {"REPTPROG0", "CLASSDECL REPTPROG0"},
            /*  79 */ {"REPTPROG0", "EPSILON"},
            /*  80 */ {"REPTPROG1", "FUNCDEF REPTPROG1"},
            /*  81 */ {"REPTPROG1", "EPSILON"},
            /*  82 */ {"REPTSTATBLOCK1", "STATEMENT REPTSTATBLOCK1"},
            /*  83 */ {"REPTSTATBLOCK1", "EPSILON"},
            /*  84 */ {"REPTVARDECL2", "ARRAYSIZE REPTVARDECL2"},
            /*  85 */ {"REPTVARDECL2", "EPSILON"},
            /*  86 */ {"REPTVARIABLE", "VARIDNEST REPTVARIABLE"},
            /*  87 */ {"REPTVARIABLE", "EPSILON"},
            /*  88 */ {"REPTVARIABLEORFUNCTIONCALL", "IDNEST REPTVARIABLEORFUNCTIONCALL"},
            /*  89 */ {"REPTVARIABLEORFUNCTIONCALL", "EPSILON"},

            // ===================== RIGHT-RECURSIVE ARITH/TERM ============
            // Left-associativity via stack: after ADDOP+TERM (or MULTOP+FACTOR),
            // @MAKE_ADDNODE/@MAKE_MULTNODE pops right, op, left -> creates node,
            // pushes result as new left operand for next iteration.

            /*  90 */ {"RIGHTRECARITHEXPR", "EPSILON"},
            /*  91 */ {"RIGHTRECARITHEXPR", "ADDOP TERM @MAKE_ADDNODE RIGHTRECARITHEXPR"},
            /*  92 */ {"RIGHTRECTERM", "EPSILON"},
            /*  93 */ {"RIGHTRECTERM", "MULTOP FACTOR @MAKE_MULTNODE RIGHTRECTERM"},

            // ===================== SIGN ==================================

            /*  94 */ {"SIGN", "plus @MAKE_SIGN"},
            /*  95 */ {"SIGN", "minus @MAKE_SIGN"},

            // ===================== START =================================

            /*  96 */ {"START", "PROG"},

            // ===================== STATEMENT BLOCK =======================
            // Epsilon marker + collector for statement lists.

            /*  97 */ {"STATBLOCK", "do @PUSH_EPSILON REPTSTATBLOCK1 @MAKE_STATBLOCK end"},
            /*  98 */ {"STATBLOCK", "@PUSH_EPSILON STATEMENT @MAKE_STATBLOCK"},    // single statement
            /*  99 */ {"STATBLOCK", "@PUSH_EPSILON @MAKE_STATBLOCK"},              // empty block

            // ===================== STATEMENTS ============================
            //
            // Rule 100-109 handle the tricky "id STATEMENT2" case where
            // the first id could start an assignment OR a function call.
            //
            // ASSIGNMENT PATH (id -> indices -> dot chain -> assign):
            //   Rule 100: push id
            //   Rule 106: build var (id + indices), then STMTIDNEST
            //   Rule 123: dot chain continuation -> STMTIDNEST2
            //   Rule 126: build var, @MAKE_DOT with previous, continue
            //   Rule 124: assign EXPR -> @MAKE_ASSIGNSTAT
            //
            // FUNCTION CALL PATH (id -> (args) -> maybe dot chain):
            //   Rule 100: push id
            //   Rule 107: (args) -> @MAKE_FUNCCALL, then STATEMENT3
            //   Rule 108: dot continuation -> STMTIDNEST2
            //   Rule 125: (args) -> @MAKE_FUNCCALL, @MAKE_DOT, then STMTIDNEST3
            //   Rule 109/128: semi -> done (funcCall is the statement)
            //
            // Example trace for "a.b[1] = 5;":
            //   100: push id:a
            //   106: epsilon marker, no indices, @MAKE_INDICELIST, @MAKE_VAR -> var(a,[])
            //   123: push id:b
            //   126: epsilon marker, INDICE(1), @MAKE_INDICELIST, @MAKE_VAR -> var(b,[1])
            //        @MAKE_DOT -> dot(var(a,[]), var(b,[1]))
            //   124: push expr:5, @MAKE_ASSIGNSTAT -> assignStat(dot(...), 5)

            /* 100 */ {"STATEMENT", "id @MAKE_ID STATEMENT2"},
            /* 101 */ {"STATEMENT", "if lpar RELEXPR rpar then STATBLOCK else STATBLOCK semi @MAKE_IFSTAT"},
            /* 102 */ {"STATEMENT", "while lpar RELEXPR rpar STATBLOCK semi @MAKE_WHILESTAT"},
            /* 103 */ {"STATEMENT", "read lpar VARIABLE rpar semi @MAKE_READSTAT"},
            /* 104 */ {"STATEMENT", "write lpar EXPR rpar semi @MAKE_WRITESTAT"},
            /* 105 */ {"STATEMENT", "return lpar EXPR rpar semi @MAKE_RETURNSTAT"},

            // STATEMENT2: variable path (indices then assign/dot) vs function call path
            /* 106 */ {"STATEMENT2", "@PUSH_EPSILON REPTIDNEST1 @MAKE_INDICELIST @MAKE_VAR STMTIDNEST"},
            /* 107 */ {"STATEMENT2", "lpar APARAMS rpar @MAKE_FUNCCALL STATEMENT3"},

            // STATEMENT3: after id(args), continue dot chain or end with semi
            /* 108 */ {"STATEMENT3", "dot id @MAKE_ID STMTIDNEST2"},
            /* 109 */ {"STATEMENT3", "semi"},  // funcCall already on stack as the statement

            // ===================== TERM ==================================
            // Same left-assoc pattern as ARITHEXPR but for mult/div/and.

            /* 110 */ {"TERM", "FACTOR RIGHTRECTERM"},

            // ===================== TYPE ==================================

            /* 111 */ {"TYPE", "integer @MAKE_TYPE"},
            /* 112 */ {"TYPE", "float @MAKE_TYPE"},
            /* 113 */ {"TYPE", "id @MAKE_TYPE"},

            // ===================== VARIABLE DECLARATION ==================
            // TYPE pushes type leaf, id @MAKE_ID pushes id leaf,
            // epsilon marker + REPTVARDECL2 + @MAKE_DIMLIST collects dimensions,
            // @MAKE_VARDECL pops 3: type, id, dimList.

            /* 114 */ {"VARDECL", "TYPE id @MAKE_ID @PUSH_EPSILON REPTVARDECL2 @MAKE_DIMLIST semi @MAKE_VARDECL"},

            // ===================== VARIABLE (for read/assign LHS) =========
            // Rule 115 pushes the first id. VARIABLE2 builds var + dot chain.
            //
            // Rule 116: variable path - build var, then REPTVARIABLE for dots
            // Rule 117: function call path - id(args), then VARIDNEST for .field
            //
            // VARIDNEST (rules 118-120) handles dot chain continuation:
            //   Rule 118: .id -> push id, VARIDNEST2 decides indices vs funcCall
            //   Rule 119: .id(args) -> funcCall, @MAKE_DOT, continue chain
            //   Rule 120: .id[i] -> var, @MAKE_DOT

            /* 115 */ {"VARIABLE", "id @MAKE_ID VARIABLE2"},
            /* 116 */ {"VARIABLE2", "@PUSH_EPSILON REPTIDNEST1 @MAKE_INDICELIST @MAKE_VAR REPTVARIABLE"},
            /* 117 */ {"VARIABLE2", "lpar APARAMS rpar @MAKE_FUNCCALL VARIDNEST"},
            /* 118 */ {"VARIDNEST", "dot id @MAKE_ID VARIDNEST2"},
            /* 119 */ {"VARIDNEST2", "lpar APARAMS rpar @MAKE_FUNCCALL @MAKE_DOT dot id @MAKE_ID VARIDNEST2"},
            /* 120 */ {"VARIDNEST2", "@PUSH_EPSILON REPTIDNEST1 @MAKE_INDICELIST @MAKE_VAR @MAKE_DOT"},

            // ===================== VISIBILITY ============================

            /* 121 */ {"VISIBILITY", "public @MAKE_VISIBILITY"},
            /* 122 */ {"VISIBILITY", "private @MAKE_VISIBILITY"},

            // ===================== STATEMENT ID-NEST (dot/assign chain) ===
            // Continuation of STATEMENT2 rule 106 (variable path).
            //
            // Rule 123: dot continuation - push new id, enter STMTIDNEST2
            // Rule 124: assignment - push expr, @MAKE_ASSIGNSTAT
            //
            // STMTIDNEST2 (rules 125-126): after dot+id in statement context
            //   Rule 125: id(args) -> funcCall, @MAKE_DOT, continue via STMTIDNEST3
            //   Rule 126: id[indices] -> var, @MAKE_DOT, continue via STMTIDNEST
            //
            // STMTIDNEST3 (rules 127-128): after funcCall in dot chain
            //   Rule 127: another dot - continue chain
            //   Rule 128: semi - done (funcCall chain is the statement)

            /* 123 */ {"STMTIDNEST", "dot id @MAKE_ID STMTIDNEST2"},
            /* 124 */ {"STMTIDNEST", "assign EXPR semi @MAKE_ASSIGNSTAT"},
            /* 125 */ {"STMTIDNEST2", "lpar APARAMS rpar @MAKE_FUNCCALL @MAKE_DOT STMTIDNEST3"},
            /* 126 */ {"STMTIDNEST2", "@PUSH_EPSILON REPTIDNEST1 @MAKE_INDICELIST @MAKE_VAR @MAKE_DOT STMTIDNEST"},
            /* 127 */ {"STMTIDNEST3", "dot id @MAKE_ID STMTIDNEST2"},
            /* 128 */ {"STMTIDNEST3", "semi"},  // end of func call chain statement
    };

    // Parsing table: TT[nonTerminal][terminal] = rule index
    // Returns -1 if error (empty cell)
    public static final Map<String, Map<String, Integer>> table = new HashMap<>();

    static {
        table.put("ADDOP", new HashMap<>() {{
            put("minus", 1);
            put("or", 2);
            put("plus", 0);
        }});
        table.put("APARAMS", new HashMap<>() {{
            put("floatnum", 3);
            put("id", 3);
            put("intnum", 3);
            put("lpar", 3);
            put("minus", 3);
            put("not", 3);
            put("plus", 3);
            put("rpar", 4);
        }});
        table.put("APARAMSTAIL", new HashMap<>() {{
            put("comma", 5);
        }});
        table.put("ARITHEXPR", new HashMap<>() {{
            put("floatnum", 6);
            put("id", 6);
            put("intnum", 6);
            put("lpar", 6);
            put("minus", 6);
            put("not", 6);
            put("plus", 6);
        }});
        table.put("ARRAYSIZE", new HashMap<>() {{
            put("lsqbr", 7);
        }});
        table.put("ARRAYSIZE2", new HashMap<>() {{
            put("intnum", 8);
            put("rsqbr", 9);
        }});
        table.put("ASSIGNOP", new HashMap<>() {{
            put("assign", 10);
        }});
        table.put("CLASSDECL", new HashMap<>() {{
            put("class", 11);
        }});
        table.put("EXPR", new HashMap<>() {{
            put("floatnum", 12);
            put("id", 12);
            put("intnum", 12);
            put("lpar", 12);
            put("minus", 12);
            put("not", 12);
            put("plus", 12);
        }});
        table.put("EXPR2", new HashMap<>() {{
            put("eq", 13);
            put("geq", 13);
            put("gt", 13);
            put("leq", 13);
            put("lt", 13);
            put("neq", 13);
            put("comma", 14);
            put("rpar", 14);
            put("semi", 14);
        }});
        table.put("FACTOR", new HashMap<>() {{
            put("floatnum", 17);
            put("id", 15);
            put("intnum", 16);
            put("lpar", 18);
            put("minus", 20);
            put("not", 19);
            put("plus", 20);
        }});
        table.put("FACTOR2", new HashMap<>() {{
            put("and", 22);
            put("comma", 22);
            put("div", 22);
            put("dot", 22);
            put("eq", 22);
            put("geq", 22);
            put("gt", 22);
            put("leq", 22);
            put("lpar", 21);
            put("lsqbr", 22);
            put("lt", 22);
            put("minus", 22);
            put("mult", 22);
            put("neq", 22);
            put("or", 22);
            put("plus", 22);
            put("rpar", 22);
            put("rsqbr", 22);
            put("semi", 22);
        }});
        table.put("FPARAMS", new HashMap<>() {{
            put("float", 23);
            put("id", 23);
            put("integer", 23);
            put("rpar", 24);
        }});
        table.put("FPARAMSTAIL", new HashMap<>() {{
            put("comma", 25);
        }});
        table.put("FUNCBODY", new HashMap<>() {{
            put("do", 26);
            put("local", 26);
        }});
        table.put("FUNCDECL", new HashMap<>() {{
            put("id", 27);
        }});
        table.put("FUNCDECL2", new HashMap<>() {{
            put("float", 28);
            put("id", 28);
            put("integer", 28);
            put("void", 29);
        }});
        table.put("FUNCDEF", new HashMap<>() {{
            put("id", 30);
        }});
        table.put("FUNCHEAD", new HashMap<>() {{
            put("id", 31);
        }});
        table.put("FUNCHEAD1", new HashMap<>() {{
            put("lpar", 33);
            put("coloncolon", 32);
        }});
        table.put("FUNCHEAD2", new HashMap<>() {{
            put("float", 34);
            put("id", 34);
            put("integer", 34);
            put("void", 35);
        }});
        table.put("IDNEST", new HashMap<>() {{
            put("dot", 36);
        }});
        table.put("IDNEST2", new HashMap<>() {{
            put("and", 38);
            put("div", 38);
            put("dot", 38);
            put("eq", 38);
            put("geq", 38);
            put("gt", 38);
            put("leq", 38);
            put("lpar", 37);
            put("lsqbr", 38);
            put("lt", 38);
            put("minus", 38);
            put("mult", 38);
            put("neq", 38);
            put("or", 38);
            put("plus", 38);
            put("rpar", 38);
            put("rsqbr", 38);
            put("semi", 38);
        }});
        table.put("INDICE", new HashMap<>() {{
            put("lsqbr", 39);
        }});
        table.put("MEMBERDECL", new HashMap<>() {{
            put("float", 41);
            put("id", 40);
            put("integer", 42);
        }});
        table.put("MEMBERDECL2", new HashMap<>() {{
            put("id", 44);
            put("lpar", 43);
        }});
        table.put("MULTOP", new HashMap<>() {{
            put("and", 47);
            put("div", 46);
            put("mult", 45);
        }});
        table.put("OPTCLASSDECL2", new HashMap<>() {{
            put("inherits", 48);
            put("lcurbr", 49);
        }});
        table.put("OPTFUNCBODY0", new HashMap<>() {{
            put("do", 51);
            put("local", 50);
        }});
        table.put("PROG", new HashMap<>() {{
            put("class", 52);
            put("id", 52);
            put("main", 52);
        }});
        table.put("RELEXPR", new HashMap<>() {{
            put("floatnum", 53);
            put("id", 53);
            put("intnum", 53);
            put("lpar", 53);
            put("minus", 53);
            put("not", 53);
            put("plus", 53);
        }});
        table.put("RELOP", new HashMap<>() {{
            put("eq", 54);
            put("geq", 59);
            put("gt", 57);
            put("leq", 58);
            put("lt", 56);
            put("neq", 55);
        }});
        table.put("REPTAPARAMS1", new HashMap<>() {{
            put("comma", 60);
            put("rpar", 61);
        }});
        table.put("REPTCLASSDECL4", new HashMap<>() {{
            put("private", 62);
            put("public", 62);
            put("rcurbr", 63);
        }});
        table.put("REPTFPARAMS2", new HashMap<>() {{
            put("lsqbr", 64);
            put("comma", 65);
            put("rpar", 65);
        }});
        table.put("REPTFPARAMS3", new HashMap<>() {{
            put("comma", 66);
            put("rpar", 67);
        }});
        table.put("REPTFPARAMSTAIL3", new HashMap<>() {{
            put("comma", 69);
            put("lsqbr", 68);
            put("rpar", 69);
        }});
        table.put("REPTFUNCBODY2", new HashMap<>() {{
            put("end", 71);
            put("id", 70);
            put("if", 70);
            put("read", 70);
            put("return", 70);
            put("while", 70);
            put("write", 70);
        }});
        table.put("REPTIDNEST1", new HashMap<>() {{
            put("and", 73);
            put("comma", 73);
            put("div", 73);
            put("dot", 73);
            put("eq", 73);
            put("assign", 73);
            put("geq", 73);
            put("gt", 73);
            put("leq", 73);
            put("lsqbr", 72);
            put("lt", 73);
            put("minus", 73);
            put("mult", 73);
            put("neq", 73);
            put("or", 73);
            put("plus", 73);
            put("rpar", 73);
            put("rsqbr", 73);
            put("semi", 73);
        }});
        table.put("REPTOPTCLASSDECL22", new HashMap<>() {{
            put("comma", 74);
            put("lcurbr", 75);
        }});
        table.put("REPTOPTFUNCBODY01", new HashMap<>() {{
            put("do", 77);
            put("float", 76);
            put("id", 76);
            put("integer", 76);
        }});
        table.put("REPTPROG0", new HashMap<>() {{
            put("class", 78);
            put("id", 79);
            put("main", 79);
        }});
        table.put("REPTPROG1", new HashMap<>() {{
            put("id", 80);
            put("main", 81);
        }});
        table.put("REPTSTATBLOCK1", new HashMap<>() {{
            put("end", 83);
            put("id", 82);
            put("if", 82);
            put("read", 82);
            put("return", 82);
            put("while", 82);
            put("write", 82);
        }});
        table.put("REPTVARDECL2", new HashMap<>() {{
            put("lsqbr", 84);
            put("semi", 85);
        }});
        table.put("REPTVARIABLE", new HashMap<>() {{
            put("dot", 86);
            put("assign", 87);
            put("rpar", 87);
        }});
        table.put("REPTVARIABLEORFUNCTIONCALL", new HashMap<>() {{
            put("and", 89);
            put("comma", 89);
            put("div", 89);
            put("dot", 88);
            put("eq", 89);
            put("geq", 89);
            put("gt", 89);
            put("leq", 89);
            put("lt", 89);
            put("minus", 89);
            put("mult", 89);
            put("neq", 89);
            put("or", 89);
            put("plus", 89);
            put("rpar", 89);
            put("rsqbr", 89);
            put("semi", 89);
        }});
        table.put("RIGHTRECARITHEXPR", new HashMap<>() {{
            put("comma", 90);
            put("eq", 90);
            put("geq", 90);
            put("gt", 90);
            put("leq", 90);
            put("lt", 90);
            put("minus", 91);
            put("neq", 90);
            put("or", 91);
            put("plus", 91);
            put("rpar", 90);
            put("rsqbr", 90);
            put("semi", 90);
        }});
        table.put("RIGHTRECTERM", new HashMap<>() {{
            put("and", 93);
            put("comma", 92);
            put("div", 93);
            put("eq", 92);
            put("geq", 92);
            put("gt", 92);
            put("leq", 92);
            put("lt", 92);
            put("minus", 92);
            put("mult", 93);
            put("neq", 92);
            put("or", 92);
            put("plus", 92);
            put("rpar", 92);
            put("rsqbr", 92);
            put("semi", 92);
        }});
        table.put("SIGN", new HashMap<>() {{
            put("minus", 95);
            put("plus", 94);
        }});
        table.put("START", new HashMap<>() {{
            put("class", 96);
            put("id", 96);
            put("main", 96);
        }});
        table.put("STATBLOCK", new HashMap<>() {{
            put("do", 97);
            put("else", 99);
            put("id", 98);
            put("if", 98);
            put("read", 98);
            put("return", 98);
            put("semi", 99);
            put("while", 98);
            put("write", 98);
        }});
        table.put("STATEMENT", new HashMap<>() {{
            put("id", 100);
            put("if", 101);
            put("read", 103);
            put("return", 105);
            put("while", 102);
            put("write", 104);
        }});
        table.put("STATEMENT2", new HashMap<>() {{
            put("dot", 106);
            put("assign", 106);
            put("lpar", 107);
            put("lsqbr", 106);
        }});
        table.put("STATEMENT3", new HashMap<>() {{
            put("dot", 108);
            put("semi", 109);
        }});
        table.put("STMTIDNEST", new HashMap<>() {{
            put("dot", 123);
            put("assign", 124);
        }});
        table.put("STMTIDNEST2", new HashMap<>() {{
            put("lpar", 125);
            put("lsqbr", 126);
            put("dot", 126);
            put("assign", 126);
        }});
        table.put("STMTIDNEST3", new HashMap<>() {{
            put("dot", 127);
            put("semi", 128);
        }});
        table.put("TERM", new HashMap<>() {{
            put("floatnum", 110);
            put("id", 110);
            put("intnum", 110);
            put("lpar", 110);
            put("minus", 110);
            put("not", 110);
            put("plus", 110);
        }});
        table.put("TYPE", new HashMap<>() {{
            put("float", 112);
            put("id", 113);
            put("integer", 111);
        }});
        table.put("VARDECL", new HashMap<>() {{
            put("float", 114);
            put("id", 114);
            put("integer", 114);
        }});
        table.put("VARIABLE", new HashMap<>() {{
            put("id", 115);
        }});
        table.put("VARIABLE2", new HashMap<>() {{
            put("dot", 116);
            put("lpar", 117);
            put("lsqbr", 116);
            put("rpar", 116);
        }});
        table.put("VARIDNEST", new HashMap<>() {{
            put("dot", 118);
        }});
        table.put("VARIDNEST2", new HashMap<>() {{
            put("dot", 120);
            put("assign", 120);
            put("lpar", 119);
            put("lsqbr", 120);
            put("rpar", 120);
        }});
        table.put("VISIBILITY", new HashMap<>() {{
            put("private", 122);
            put("public", 121);
        }});
    }

    public static int lookup(String nonTerminal, String terminal) {
        Map<String, Integer> row = table.get(nonTerminal);
        if (row == null) return -1;
        Integer ruleIdx = row.get(terminal);
        return ruleIdx != null ? ruleIdx : -1;
    }

    // FIRST sets for error recovery
    public static final Map<String, Set<String>> firstSets = new HashMap<>();
    static {
        firstSets.put("START", new HashSet<>(Arrays.asList("class", "id", "main")));
        firstSets.put("APARAMS", new HashSet<>(Arrays.asList("EPSILON", "floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        firstSets.put("APARAMSTAIL", new HashSet<>(Arrays.asList()));
        firstSets.put("ADDOP", new HashSet<>(Arrays.asList("minus", "or", "plus")));
        firstSets.put("ARITHEXPR", new HashSet<>(Arrays.asList("floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        firstSets.put("ARRAYSIZE", new HashSet<>(Arrays.asList("lsqbr")));
        firstSets.put("ARRAYSIZE2", new HashSet<>(Arrays.asList("intnum", "rsqbr")));
        firstSets.put("ASSIGNOP", new HashSet<>(Arrays.asList("assign")));
        firstSets.put("CLASSDECL", new HashSet<>(Arrays.asList("class")));
        firstSets.put("EXPR", new HashSet<>(Arrays.asList("floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        firstSets.put("EXPR2", new HashSet<>(Arrays.asList("EPSILON", "eq", "geq", "gt", "leq", "lt", "neq")));
        firstSets.put("FPARAMS", new HashSet<>(Arrays.asList("EPSILON", "float", "id", "integer")));
        firstSets.put("FPARAMSTAIL", new HashSet<>(Arrays.asList()));
        firstSets.put("FACTOR", new HashSet<>(Arrays.asList("floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        firstSets.put("FACTOR2", new HashSet<>(Arrays.asList("EPSILON", "lpar", "lsqbr")));
        firstSets.put("FUNCBODY", new HashSet<>(Arrays.asList("do", "local")));
        firstSets.put("FUNCDECL", new HashSet<>(Arrays.asList("id")));
        firstSets.put("FUNCDECL2", new HashSet<>(Arrays.asList("float", "id", "integer", "void")));
        firstSets.put("FUNCDEF", new HashSet<>(Arrays.asList("id")));
        firstSets.put("FUNCHEAD", new HashSet<>(Arrays.asList("id")));
        firstSets.put("FUNCHEAD1", new HashSet<>(Arrays.asList("lpar", "coloncolon")));
        firstSets.put("FUNCHEAD2", new HashSet<>(Arrays.asList("float", "id", "integer", "void")));
        firstSets.put("IDNEST", new HashSet<>(Arrays.asList("dot")));
        firstSets.put("IDNEST2", new HashSet<>(Arrays.asList("EPSILON", "lpar", "lsqbr")));
        firstSets.put("INDICE", new HashSet<>(Arrays.asList("lsqbr")));
        firstSets.put("MEMBERDECL", new HashSet<>(Arrays.asList("float", "id", "integer")));
        firstSets.put("MEMBERDECL2", new HashSet<>(Arrays.asList("id", "lpar")));
        firstSets.put("MULTOP", new HashSet<>(Arrays.asList("and", "div", "mult")));
        firstSets.put("OPTCLASSDECL2", new HashSet<>(Arrays.asList("EPSILON", "inherits")));
        firstSets.put("OPTFUNCBODY0", new HashSet<>(Arrays.asList("EPSILON", "local")));
        firstSets.put("PROG", new HashSet<>(Arrays.asList("class", "id", "main")));
        firstSets.put("RELEXPR", new HashSet<>(Arrays.asList("floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        firstSets.put("RELOP", new HashSet<>(Arrays.asList("eq", "geq", "gt", "leq", "lt", "neq")));
        firstSets.put("REPTAPARAMS1", new HashSet<>(Arrays.asList("EPSILON")));
        firstSets.put("REPTCLASSDECL4", new HashSet<>(Arrays.asList("EPSILON", "private", "public")));
        firstSets.put("REPTFPARAMS2", new HashSet<>(Arrays.asList("EPSILON", "lsqbr")));
        firstSets.put("REPTFPARAMS3", new HashSet<>(Arrays.asList("EPSILON")));
        firstSets.put("REPTFPARAMSTAIL3", new HashSet<>(Arrays.asList("EPSILON", "lsqbr")));
        firstSets.put("REPTFUNCBODY2", new HashSet<>(Arrays.asList("EPSILON", "id", "if", "read", "return", "while", "write")));
        firstSets.put("REPTIDNEST1", new HashSet<>(Arrays.asList("EPSILON", "lsqbr")));
        firstSets.put("REPTOPTCLASSDECL22", new HashSet<>(Arrays.asList("EPSILON")));
        firstSets.put("REPTOPTFUNCBODY01", new HashSet<>(Arrays.asList("EPSILON", "float", "id", "integer")));
        firstSets.put("REPTPROG0", new HashSet<>(Arrays.asList("EPSILON", "class")));
        firstSets.put("REPTPROG1", new HashSet<>(Arrays.asList("EPSILON", "id")));
        firstSets.put("REPTSTATBLOCK1", new HashSet<>(Arrays.asList("EPSILON", "id", "if", "read", "return", "while", "write")));
        firstSets.put("REPTVARDECL2", new HashSet<>(Arrays.asList("EPSILON", "lsqbr")));
        firstSets.put("REPTVARIABLE", new HashSet<>(Arrays.asList("EPSILON", "dot")));
        firstSets.put("REPTVARIABLEORFUNCTIONCALL", new HashSet<>(Arrays.asList("EPSILON", "dot")));
        firstSets.put("RIGHTRECARITHEXPR", new HashSet<>(Arrays.asList("EPSILON", "minus", "or", "plus")));
        firstSets.put("RIGHTRECTERM", new HashSet<>(Arrays.asList("EPSILON", "and", "div", "mult")));
        firstSets.put("SIGN", new HashSet<>(Arrays.asList("minus", "plus")));
        firstSets.put("STATBLOCK", new HashSet<>(Arrays.asList("EPSILON", "do", "id", "if", "read", "return", "while", "write")));
        firstSets.put("STATEMENT", new HashSet<>(Arrays.asList("id", "if", "read", "return", "while", "write")));
        firstSets.put("STATEMENT2", new HashSet<>(Arrays.asList("dot", "assign", "lpar", "lsqbr")));
        firstSets.put("STATEMENT3", new HashSet<>(Arrays.asList("dot", "semi")));
        firstSets.put("STMTIDNEST", new HashSet<>(Arrays.asList("dot", "assign")));
        firstSets.put("STMTIDNEST2", new HashSet<>(Arrays.asList("lpar", "lsqbr", "dot", "assign")));
        firstSets.put("STMTIDNEST3", new HashSet<>(Arrays.asList("dot", "semi")));
        firstSets.put("TERM", new HashSet<>(Arrays.asList("floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        firstSets.put("TYPE", new HashSet<>(Arrays.asList("float", "id", "integer")));
        firstSets.put("VARDECL", new HashSet<>(Arrays.asList("float", "id", "integer")));
        firstSets.put("VARIABLE", new HashSet<>(Arrays.asList("id")));
        firstSets.put("VARIABLE2", new HashSet<>(Arrays.asList("EPSILON", "dot", "lpar", "lsqbr")));
        firstSets.put("VARIDNEST", new HashSet<>(Arrays.asList("dot")));
        firstSets.put("VARIDNEST2", new HashSet<>(Arrays.asList("EPSILON", "lpar", "lsqbr")));
        firstSets.put("VISIBILITY", new HashSet<>(Arrays.asList("private", "public")));
    }

    // FOLLOW sets for error recovery
    public static final Map<String, Set<String>> followSets = new HashMap<>();
    static {
        followSets.put("START", new HashSet<>(Arrays.asList("$")));
        followSets.put("APARAMS", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("APARAMSTAIL", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("ADDOP", new HashSet<>(Arrays.asList("floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        followSets.put("ARITHEXPR", new HashSet<>(Arrays.asList("eq", "geq", "gt", "leq", "lt", "neq", "rpar", "rsqbr", "semi")));
        followSets.put("ARRAYSIZE", new HashSet<>(Arrays.asList("lsqbr", "rpar", "semi")));
        followSets.put("ARRAYSIZE2", new HashSet<>(Arrays.asList("lsqbr", "rpar", "semi")));
        followSets.put("ASSIGNOP", new HashSet<>(Arrays.asList()));
        followSets.put("CLASSDECL", new HashSet<>(Arrays.asList("class", "id", "main")));
        followSets.put("EXPR", new HashSet<>(Arrays.asList("rpar", "semi")));
        followSets.put("EXPR2", new HashSet<>(Arrays.asList("rpar", "semi")));
        followSets.put("FPARAMS", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("FPARAMSTAIL", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("FACTOR", new HashSet<>(Arrays.asList("and", "div", "eq", "geq", "gt", "leq", "lt", "minus", "mult", "neq", "or", "plus", "rpar", "rsqbr", "semi")));
        followSets.put("FACTOR2", new HashSet<>(Arrays.asList("and", "div", "dot", "eq", "geq", "gt", "leq", "lt", "minus", "mult", "neq", "or", "plus", "rpar", "rsqbr", "semi")));
        followSets.put("FUNCBODY", new HashSet<>(Arrays.asList("$", "semi")));
        followSets.put("FUNCDECL", new HashSet<>(Arrays.asList()));
        followSets.put("FUNCDECL2", new HashSet<>(Arrays.asList("private", "public", "rcurbr")));
        followSets.put("FUNCDEF", new HashSet<>(Arrays.asList("id", "main")));
        followSets.put("FUNCHEAD", new HashSet<>(Arrays.asList("do", "local")));
        followSets.put("FUNCHEAD1", new HashSet<>(Arrays.asList("do", "local")));
        followSets.put("FUNCHEAD2", new HashSet<>(Arrays.asList("do", "local")));
        followSets.put("IDNEST", new HashSet<>(Arrays.asList("and", "div", "dot", "eq", "geq", "gt", "leq", "lt", "minus", "mult", "neq", "or", "plus", "rpar", "rsqbr", "semi")));
        followSets.put("IDNEST2", new HashSet<>(Arrays.asList("and", "div", "dot", "eq", "geq", "gt", "leq", "lt", "minus", "mult", "neq", "or", "plus", "rpar", "rsqbr", "semi")));
        followSets.put("INDICE", new HashSet<>(Arrays.asList("and", "div", "dot", "eq", "assign", "geq", "gt", "leq", "lsqbr", "lt", "minus", "mult", "neq", "or", "plus", "rpar", "rsqbr", "semi")));
        followSets.put("MEMBERDECL", new HashSet<>(Arrays.asList("private", "public", "rcurbr")));
        followSets.put("MEMBERDECL2", new HashSet<>(Arrays.asList("private", "public", "rcurbr")));
        followSets.put("MULTOP", new HashSet<>(Arrays.asList("floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        followSets.put("OPTCLASSDECL2", new HashSet<>(Arrays.asList("lcurbr")));
        followSets.put("OPTFUNCBODY0", new HashSet<>(Arrays.asList("do")));
        followSets.put("PROG", new HashSet<>(Arrays.asList("$")));
        followSets.put("RELEXPR", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("RELOP", new HashSet<>(Arrays.asList("floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        followSets.put("REPTAPARAMS1", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("REPTCLASSDECL4", new HashSet<>(Arrays.asList("rcurbr")));
        followSets.put("REPTFPARAMS2", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("REPTFPARAMS3", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("REPTFPARAMSTAIL3", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("REPTFUNCBODY2", new HashSet<>(Arrays.asList("end")));
        followSets.put("REPTIDNEST1", new HashSet<>(Arrays.asList("and", "div", "dot", "eq", "assign", "geq", "gt", "leq", "lt", "minus", "mult", "neq", "or", "plus", "rpar", "rsqbr", "semi")));
        followSets.put("REPTOPTCLASSDECL22", new HashSet<>(Arrays.asList("lcurbr")));
        followSets.put("REPTOPTFUNCBODY01", new HashSet<>(Arrays.asList("do")));
        followSets.put("REPTPROG0", new HashSet<>(Arrays.asList("id", "main")));
        followSets.put("REPTPROG1", new HashSet<>(Arrays.asList("main")));
        followSets.put("REPTSTATBLOCK1", new HashSet<>(Arrays.asList("end")));
        followSets.put("REPTVARDECL2", new HashSet<>(Arrays.asList("semi")));
        followSets.put("REPTVARIABLE", new HashSet<>(Arrays.asList("assign", "rpar")));
        followSets.put("REPTVARIABLEORFUNCTIONCALL", new HashSet<>(Arrays.asList("and", "div", "eq", "geq", "gt", "leq", "lt", "minus", "mult", "neq", "or", "plus", "rpar", "rsqbr", "semi")));
        followSets.put("RIGHTRECARITHEXPR", new HashSet<>(Arrays.asList("eq", "geq", "gt", "leq", "lt", "neq", "rpar", "rsqbr", "semi")));
        followSets.put("RIGHTRECTERM", new HashSet<>(Arrays.asList("eq", "geq", "gt", "leq", "lt", "minus", "neq", "or", "plus", "rpar", "rsqbr", "semi")));
        followSets.put("SIGN", new HashSet<>(Arrays.asList("floatnum", "id", "intnum", "lpar", "minus", "not", "plus")));
        followSets.put("STATBLOCK", new HashSet<>(Arrays.asList("else", "semi")));
        followSets.put("STATEMENT", new HashSet<>(Arrays.asList("else", "end", "id", "if", "read", "return", "semi", "while", "write")));
        followSets.put("STATEMENT2", new HashSet<>(Arrays.asList("else", "end", "id", "if", "read", "return", "semi", "while", "write")));
        followSets.put("STATEMENT3", new HashSet<>(Arrays.asList("else", "end", "id", "if", "read", "return", "semi", "while", "write")));
        followSets.put("STMTIDNEST", new HashSet<>(Arrays.asList("else", "end", "id", "if", "read", "return", "semi", "while", "write")));
        followSets.put("STMTIDNEST2", new HashSet<>(Arrays.asList("else", "end", "id", "if", "read", "return", "semi", "while", "write")));
        followSets.put("STMTIDNEST3", new HashSet<>(Arrays.asList("else", "end", "id", "if", "read", "return", "semi", "while", "write")));
        followSets.put("TERM", new HashSet<>(Arrays.asList("eq", "geq", "gt", "leq", "lt", "minus", "neq", "or", "plus", "rpar", "rsqbr", "semi")));
        followSets.put("TYPE", new HashSet<>(Arrays.asList("do", "id", "local", "semi")));
        followSets.put("VARDECL", new HashSet<>(Arrays.asList("do", "float", "id", "integer")));
        followSets.put("VARIABLE", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("VARIABLE2", new HashSet<>(Arrays.asList("rpar")));
        followSets.put("VARIDNEST", new HashSet<>(Arrays.asList("dot", "assign", "rpar")));
        followSets.put("VARIDNEST2", new HashSet<>(Arrays.asList("dot", "assign", "rpar")));
        followSets.put("VISIBILITY", new HashSet<>(Arrays.asList("float", "id", "integer")));
    }
}