package src.SyntacticalAnalyzer;// LL(1) Parsing Table - Auto-generated
// Usage: table.get("NONTERMINAL").get("terminal") returns int[] of RHS symbols

import java.util.*;

public class ParsingTable {

    // All grammar rules indexed by number
    public static final String[][] rules = {
            /*   0 */ {"ADDOP", "plus"},
            /*   1 */ {"ADDOP", "minus"},
            /*   2 */ {"ADDOP", "or"},
            /*   3 */ {"APARAMS", "EXPR REPTAPARAMS1"},
            /*   4 */ {"APARAMS", "EPSILON"},
            /*   5 */ {"APARAMSTAIL", "comma EXPR"},
            /*   6 */ {"ARITHEXPR", "TERM RIGHTRECARITHEXPR"},
            /*   7 */ {"ARRAYSIZE", "lsqbr ARRAYSIZE2"},
            /*   8 */ {"ARRAYSIZE2", "intnum rsqbr"},
            /*   9 */ {"ARRAYSIZE2", "rsqbr"},
            /*  10 */ {"ASSIGNOP", "assign"},
            /*  11 */ {"CLASSDECL", "class id OPTCLASSDECL2 lcurbr REPTCLASSDECL4 rcurbr semi"},
            /*  12 */ {"EXPR", "ARITHEXPR EXPR2"},
            /*  13 */ {"EXPR2", "RELOP ARITHEXPR"},
            /*  14 */ {"EXPR2", "EPSILON"},
            /*  15 */ {"FACTOR", "id FACTOR2 REPTVARIABLEORFUNCTIONCALL"},
            /*  16 */ {"FACTOR", "intnum"},
            /*  17 */ {"FACTOR", "floatnum"},
            /*  18 */ {"FACTOR", "lpar ARITHEXPR rpar"},
            /*  19 */ {"FACTOR", "not FACTOR"},
            /*  20 */ {"FACTOR", "SIGN FACTOR"},
            /*  21 */ {"FACTOR2", "lpar APARAMS rpar"},
            /*  22 */ {"FACTOR2", "REPTIDNEST1"},
            /*  23 */ {"FPARAMS", "TYPE id REPTFPARAMS2 REPTFPARAMS3"},
            /*  24 */ {"FPARAMS", "EPSILON"},
            /*  25 */ {"FPARAMSTAIL", "comma TYPE id REPTFPARAMSTAIL3"},
            /*  26 */ {"FUNCBODY", "OPTFUNCBODY0 do REPTFUNCBODY2 end"},
            /*  27 */ {"FUNCDECL", "id lpar FPARAMS rpar colon FUNCDECL2"},
            /*  28 */ {"FUNCDECL2", "TYPE semi"},
            /*  29 */ {"FUNCDECL2", "void semi"},
            /*  30 */ {"FUNCDEF", "FUNCHEAD FUNCBODY semi"},
            /*  31 */ {"FUNCHEAD", "id FUNCHEAD1"},
            /*  32 */ {"FUNCHEAD1", "coloncolon id lpar FPARAMS rpar colon FUNCHEAD2"},
            /*  33 */ {"FUNCHEAD1", "lpar FPARAMS rpar colon FUNCHEAD2"},
            /*  34 */ {"FUNCHEAD2", "TYPE"},
            /*  35 */ {"FUNCHEAD2", "void"},
            /*  36 */ {"IDNEST", "dot id IDNEST2"},
            /*  37 */ {"IDNEST2", "lpar APARAMS rpar"},
            /*  38 */ {"IDNEST2", "REPTIDNEST1"},
            /*  39 */ {"INDICE", "lsqbr ARITHEXPR rsqbr"},
            /*  40 */ {"MEMBERDECL", "id MEMBERDECL2"},
            /*  41 */ {"MEMBERDECL", "float id REPTVARDECL2 semi"},
            /*  42 */ {"MEMBERDECL", "integer id REPTVARDECL2 semi"},
            /*  43 */ {"MEMBERDECL2", "lpar FPARAMS rpar colon FUNCDECL2"},
            /*  44 */ {"MEMBERDECL2", "id REPTVARDECL2 semi"},
            /*  45 */ {"MULTOP", "mult"},
            /*  46 */ {"MULTOP", "div"},
            /*  47 */ {"MULTOP", "and"},
            /*  48 */ {"OPTCLASSDECL2", "inherits id REPTOPTCLASSDECL22"},
            /*  49 */ {"OPTCLASSDECL2", "EPSILON"},
            /*  50 */ {"OPTFUNCBODY0", "local REPTOPTFUNCBODY01"},
            /*  51 */ {"OPTFUNCBODY0", "EPSILON"},
            /*  52 */ {"PROG", "REPTPROG0 REPTPROG1 main FUNCBODY"},
            /*  53 */ {"RELEXPR", "ARITHEXPR RELOP ARITHEXPR"},
            /*  54 */ {"RELOP", "eq"},
            /*  55 */ {"RELOP", "neq"},
            /*  56 */ {"RELOP", "lt"},
            /*  57 */ {"RELOP", "gt"},
            /*  58 */ {"RELOP", "leq"},
            /*  59 */ {"RELOP", "geq"},
            /*  60 */ {"REPTAPARAMS1", "APARAMSTAIL REPTAPARAMS1"},
            /*  61 */ {"REPTAPARAMS1", "EPSILON"},
            /*  62 */ {"REPTCLASSDECL4", "VISIBILITY MEMBERDECL REPTCLASSDECL4"},
            /*  63 */ {"REPTCLASSDECL4", "EPSILON"},
            /*  64 */ {"REPTFPARAMS2", "ARRAYSIZE REPTFPARAMS2"},
            /*  65 */ {"REPTFPARAMS2", "EPSILON"},
            /*  66 */ {"REPTFPARAMS3", "FPARAMSTAIL REPTFPARAMS3"},
            /*  67 */ {"REPTFPARAMS3", "EPSILON"},
            /*  68 */ {"REPTFPARAMSTAIL3", "ARRAYSIZE REPTFPARAMSTAIL3"},
            /*  69 */ {"REPTFPARAMSTAIL3", "EPSILON"},
            /*  70 */ {"REPTFUNCBODY2", "STATEMENT REPTFUNCBODY2"},
            /*  71 */ {"REPTFUNCBODY2", "EPSILON"},
            /*  72 */ {"REPTIDNEST1", "INDICE REPTIDNEST1"},
            /*  73 */ {"REPTIDNEST1", "EPSILON"},
            /*  74 */ {"REPTOPTCLASSDECL22", "comma id REPTOPTCLASSDECL22"},
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
            /*  90 */ {"RIGHTRECARITHEXPR", "EPSILON"},
            /*  91 */ {"RIGHTRECARITHEXPR", "ADDOP TERM RIGHTRECARITHEXPR"},
            /*  92 */ {"RIGHTRECTERM", "EPSILON"},
            /*  93 */ {"RIGHTRECTERM", "MULTOP FACTOR RIGHTRECTERM"},
            /*  94 */ {"SIGN", "plus"},
            /*  95 */ {"SIGN", "minus"},
            /*  96 */ {"START", "PROG"},
            /*  97 */ {"STATBLOCK", "do REPTSTATBLOCK1 end"},
            /*  98 */ {"STATBLOCK", "STATEMENT"},
            /*  99 */ {"STATBLOCK", "EPSILON"},
            /* 100 */ {"STATEMENT", "id STATEMENT2"},
            /* 101 */ {"STATEMENT", "if lpar RELEXPR rpar then STATBLOCK else STATBLOCK semi"},
            /* 102 */ {"STATEMENT", "while lpar RELEXPR rpar STATBLOCK semi"},
            /* 103 */ {"STATEMENT", "read lpar VARIABLE rpar semi"},
            /* 104 */ {"STATEMENT", "write lpar EXPR rpar semi"},
            /* 105 */ {"STATEMENT", "return lpar EXPR rpar semi"},
            /* 106 */ {"STATEMENT2", "REPTIDNEST1 REPTVARIABLE assign EXPR semi"},
            /* 107 */ {"STATEMENT2", "lpar APARAMS rpar STATEMENT3"},
            /* 108 */ {"STATEMENT3", "VARIDNEST assign EXPR semi"},
            /* 109 */ {"STATEMENT3", "semi"},
            /* 110 */ {"TERM", "FACTOR RIGHTRECTERM"},
            /* 111 */ {"TYPE", "integer"},
            /* 112 */ {"TYPE", "float"},
            /* 113 */ {"TYPE", "id"},
            /* 114 */ {"VARDECL", "TYPE id REPTVARDECL2 semi"},
            /* 115 */ {"VARIABLE", "id VARIABLE2"},
            /* 116 */ {"VARIABLE2", "REPTIDNEST1 REPTVARIABLE"},
            /* 117 */ {"VARIABLE2", "lpar APARAMS rpar VARIDNEST"},
            /* 118 */ {"VARIDNEST", "dot id VARIDNEST2"},
            /* 119 */ {"VARIDNEST2", "lpar APARAMS rpar dot id VARIDNEST2"},
            /* 120 */ {"VARIDNEST2", "REPTIDNEST1"},
            /* 121 */ {"VISIBILITY", "public"},
            /* 122 */ {"VISIBILITY", "private"},
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