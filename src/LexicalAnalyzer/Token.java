package src.LexicalAnalyzer;

/*
Goal of this class:
Hold a token and information about it, populated by the LexicalAnalyzer.
 */

public class Token{
    public static enum Token_Type
    {
        ID, INTEGER, FLOAT, //Basic types
        EQ, NEQ, LT, GT, LEQ, GEQ, PLUS, MINUS, MULT, DIV, ASSIGN, //Operators: ==,<>,<,>,<=,>=,+,-,*,/,=
        LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,
        SEMICOLON, COMMA, DOT, COLON, COLONCOLON,//Punctuation: (,),{,},[,],;,COMMA,.,:,::
    };

    private Token_Type type;
    private String lexeme;
    private int line;


}