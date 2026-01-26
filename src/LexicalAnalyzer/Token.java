package src.LexicalAnalyzer;

/**
 * Token class representing a lexical token with its type, lexeme, and location.
 */
public class Token {

    public enum TokenType {
        // Basic types
        ID, INTEGER, FLOAT,

        // Operators
        EQ,          // ==
        NEQ,         // <>
        LT,          // <
        GT,          // >
        LEQ,         // <=
        GEQ,         // >=
        PLUS,        // +
        MINUS,       // -
        MULT,        // *
        DIV,         // /
        ASSIGN,      // =

        // Punctuation
        LPAREN,      // (
        RPAREN,      // )
        LBRACE,      // {
        RBRACE,      // }
        LBRACKET,    // [
        RBRACKET,    // ]
        SEMICOLON,   // ;
        COMMA,       // ,
        DOT,         // .
        COLON,       // :
        COLONCOLON,  // ::

        // Reserved words
        IF, THEN, ELSE, WHILE, CLASS,
        DO, END, PUBLIC, PRIVATE,
        OR, AND, NOT,
        READ, WRITE, RETURN,
        INHERITS, LOCAL, VOID, MAIN,
        INTEGER_KEYWORD,  // 'integer' reserved word
        FLOAT_KEYWORD,    // 'float' reserved word

        // Comments (for tracking, usually skipped)
        BLOCKCMT,
        INLINECMT,

        // Error types
        INVALIDNUM,
        INVALIDID,
        INVALIDCHAR,
        UNTERMINATEDCMT,

        // End of file
        EOF
    }

    private TokenType type;
    private String lexeme;
    private int line;

    public Token() {
        this.type = null;
        this.lexeme = "";
        this.line = 0;
    }

    public Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    /**
     * Returns the token in format for .outlextokens file
     */
    @Override
    public String toString() {
        // Escape newlines in the lexeme for output
        String escapedLexeme = lexeme.replace("\n", "\\n");
        return "[" + getTokenTypeName() + ", " + escapedLexeme + ", " + line + "]";
    }

    /**
     * Returns the token type name in the expected lowercase format
     */
    public String getTokenTypeName() {
        switch (type) {
            case ID: return "id";
            case INTEGER: return "intnum";
            case FLOAT: return "floatnum";
            case EQ: return "eq";
            case NEQ: return "noteq";
            case LT: return "lt";
            case GT: return "gt";
            case LEQ: return "leq";
            case GEQ: return "geq";
            case PLUS: return "plus";
            case MINUS: return "minus";
            case MULT: return "mult";
            case DIV: return "div";
            case ASSIGN: return "assign";
            case LPAREN: return "openpar";
            case RPAREN: return "closepar";
            case LBRACE: return "opencubr";
            case RBRACE: return "closecubr";
            case LBRACKET: return "opensqbr";
            case RBRACKET: return "closesqbr";
            case SEMICOLON: return "semi";
            case COMMA: return "comma";
            case DOT: return "dot";
            case COLON: return "colon";
            case COLONCOLON: return "coloncolon";
            case IF: return "if";
            case THEN: return "then";
            case ELSE: return "else";
            case WHILE: return "while";
            case CLASS: return "class";
            case DO: return "do";
            case END: return "end";
            case PUBLIC: return "public";
            case PRIVATE: return "private";
            case OR: return "or";
            case AND: return "and";
            case NOT: return "not";
            case READ: return "read";
            case WRITE: return "write";
            case RETURN: return "return";
            case INHERITS: return "inherits";
            case LOCAL: return "local";
            case VOID: return "void";
            case MAIN: return "main";
            case INTEGER_KEYWORD: return "integer";
            case FLOAT_KEYWORD: return "float";
            case BLOCKCMT: return "blockcmt";
            case INLINECMT: return "inlinecmt";
            case INVALIDNUM: return "invalidnum";
            case INVALIDID: return "invalidid";
            case INVALIDCHAR: return "invalidchar";
            case UNTERMINATEDCMT: return "unterminatedcmt";
            case EOF: return "$";
            default: return type.toString().toLowerCase();
        }
    }

    /**
     * Returns the token type name for Flaci output format
     */
    public String toFlaciString() {
        switch (type) {
            case ID: return "id";
            case INTEGER: return "intnum";
            case FLOAT: return "floatnum";
            case EQ: return "eq";
            case NEQ: return "neq";
            case LT: return "lt";
            case GT: return "gt";
            case LEQ: return "leq";
            case GEQ: return "geq";
            case PLUS: return "plus";
            case MINUS: return "minus";
            case MULT: return "mult";
            case DIV: return "div";
            case ASSIGN: return "assign";
            case LPAREN: return "lpar";
            case RPAREN: return "rpar";
            case LBRACE: return "lcurbr";
            case RBRACE: return "rcurbr";
            case LBRACKET: return "lsqbr";
            case RBRACKET: return "rsqbr";
            case SEMICOLON: return "semi";
            case COMMA: return "comma";
            case DOT: return "dot";
            case COLON: return "colon";
            case COLONCOLON: return "coloncolon";
            case IF: return "if";
            case THEN: return "then";
            case ELSE: return "else";
            case WHILE: return "while";
            case CLASS: return "class";
            case DO: return "do";
            case END: return "end";
            case PUBLIC: return "public";
            case PRIVATE: return "private";
            case OR: return "or";
            case AND: return "and";
            case NOT: return "not";
            case READ: return "read";
            case WRITE: return "write";
            case RETURN: return "return";
            case INHERITS: return "inherits";
            case LOCAL: return "local";
            case VOID: return "void";
            case MAIN: return "main";
            case INTEGER_KEYWORD: return "integer";
            case FLOAT_KEYWORD: return "float";
            case EOF: return "$";
            default: return type.toString().toLowerCase();
        }
    }

    /**
     * Check if this token is an error token
     */
    public boolean isError() {
        return type == TokenType.INVALIDNUM ||
               type == TokenType.INVALIDID ||
               type == TokenType.INVALIDCHAR ||
               type == TokenType.UNTERMINATEDCMT;
    }
}