package src.LexicalAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Lexical Analyzer implementing DFAs for:
 * - ID: letter (letter | digit | _)*
 * - Integer: nonzero digit* | 0
 * - Float: integer fraction [e[+|-] integer]
 * - Fraction: .digit*nonzero | .0
 * - Operators, punctuation, reserved words
 * - Comments (block and inline)
 */
public class LexicalAnalyzer implements ILexicalAnalyzer {

    private final PushbackReader reader;
    private int currentChar;
    private int line = 1;

    // Reserved words lookup table
    private static final Map<String, Token.TokenType> RESERVED_WORDS = new HashMap<>();

    static {
        RESERVED_WORDS.put("if", Token.TokenType.IF);
        RESERVED_WORDS.put("then", Token.TokenType.THEN);
        RESERVED_WORDS.put("else", Token.TokenType.ELSE);
        RESERVED_WORDS.put("while", Token.TokenType.WHILE);
        RESERVED_WORDS.put("class", Token.TokenType.CLASS);
        RESERVED_WORDS.put("do", Token.TokenType.DO);
        RESERVED_WORDS.put("end", Token.TokenType.END);
        RESERVED_WORDS.put("public", Token.TokenType.PUBLIC);
        RESERVED_WORDS.put("private", Token.TokenType.PRIVATE);
        RESERVED_WORDS.put("or", Token.TokenType.OR);
        RESERVED_WORDS.put("and", Token.TokenType.AND);
        RESERVED_WORDS.put("not", Token.TokenType.NOT);
        RESERVED_WORDS.put("read", Token.TokenType.READ);
        RESERVED_WORDS.put("write", Token.TokenType.WRITE);
        RESERVED_WORDS.put("return", Token.TokenType.RETURN);
        RESERVED_WORDS.put("inherits", Token.TokenType.INHERITS);
        RESERVED_WORDS.put("local", Token.TokenType.LOCAL);
        RESERVED_WORDS.put("void", Token.TokenType.VOID);
        RESERVED_WORDS.put("main", Token.TokenType.MAIN);
        RESERVED_WORDS.put("integer", Token.TokenType.INTEGER_KEYWORD);
        RESERVED_WORDS.put("float", Token.TokenType.FLOAT_KEYWORD);
    }

    public LexicalAnalyzer(BufferedReader reader) throws IOException {
        this.reader = new PushbackReader(reader, 10);
        advance();
    }

    // ==================== Main Entry Point ====================

    /**
     * Main entry point - returns the next token from input
     */
    @Override
    public Token nextToken() {
        try {
            // Skip whitespace
            skipWhitespace();

            // Check for EOF
            if (currentChar == -1) {
                return new Token(Token.TokenType.EOF, "$", line);
            }

            int tokenLine = line;

            // Check for comments first
            if (currentChar == '/') {
                advance();
                if (currentChar == '/') {
                    // Inline comment
                    return scanInlineComment(tokenLine);
                } else if (currentChar == '*') {
                    // Block comment
                    return scanBlockComment(tokenLine);
                } else {
                    // Division operator - already advanced past '/'
                    return new Token(Token.TokenType.DIV, "/", tokenLine);
                }
            }

            // ID or reserved word: starts with letter
            if (isLetter(currentChar)) {
                return scanId(tokenLine);
            }

            // Number (integer or float): starts with digit
            if (isDigit(currentChar)) {
                return scanNumber(tokenLine);
            }

            // Underscore starting - invalid identifier
            if (currentChar == '_') {
                return scanInvalidIdentifier(tokenLine);
            }

            // Operators and punctuation
            if (isOperatorOrPunctuationStart(currentChar)) {
                return scanOperatorOrPunctuation();
            }

            // Invalid character
            String invalidChar = String.valueOf((char) currentChar);
            advance();
            return new Token(Token.TokenType.INVALIDCHAR, invalidChar, tokenLine);

        } catch (IOException e) {
            return new Token(Token.TokenType.EOF, "$", line);
        }
    }

    // ==================== Character Advancement ====================

    private void advance() throws IOException {
        currentChar = reader.read();
        if (currentChar == '\n') {
            line++;
        }
    }

    private void backup(char c) throws IOException {
        if (c == '\n') {
            line--;
        }
        reader.unread(c);
        // Note: Don't set currentChar here - call advance() after backup to load it
    }

    private void skipWhitespace() throws IOException {
        while (currentChar != -1 && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    // ==================== Character Classification (DFA transitions) ====================

    /**
     * letter ::= a..z | A..Z
     */
    private boolean isLetter(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * digit ::= 0..9
     */
    private boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    /**
     * nonzero ::= 1..9
     */
    private boolean isNonZero(int c) {
        return c >= '1' && c <= '9';
    }

    /**
     * alphanum ::= letter | digit | _
     */
    private boolean isAlphanum(int c) {
        return isLetter(c) || isDigit(c) || c == '_';
    }

    private boolean isOperatorOrPunctuationStart(int c) {
        return c == '=' || c == '<' || c == '>' || c == '+' || c == '-' ||
               c == '*' || c == '/' || c == '(' || c == ')' || c == '{' ||
               c == '}' || c == '[' || c == ']' || c == ';' || c == ',' ||
               c == '.' || c == ':';
    }

    // ==================== ID Scanner (DFA from drawio (3)) ====================
    /**
     * id ::= letter alphanum*
     *
     * DFA: Start --letter--> Accept --[letter|digit|_]--> Accept (loop)
     */
    private Token scanId(int tokenLine) throws IOException {
        StringBuilder lexeme = new StringBuilder();

        // First character must be letter (already verified)
        lexeme.append((char) currentChar);
        advance();

        // Continue with alphanum (letter | digit | _)
        while (currentChar != -1 && isAlphanum(currentChar)) {
            lexeme.append((char) currentChar);
            advance();
        }

        String id = lexeme.toString();

        // Check if it's a reserved word
        Token.TokenType type = RESERVED_WORDS.get(id);
        if (type != null) {
            return new Token(type, id, tokenLine);
        }

        return new Token(Token.TokenType.ID, id, tokenLine);
    }

    // ==================== Invalid Identifier Scanner ====================
    /**
     * Handles identifiers starting with underscore (invalid per spec)
     */
    private Token scanInvalidIdentifier(int tokenLine) throws IOException {
        StringBuilder lexeme = new StringBuilder();

        // Consume the underscore
        lexeme.append((char) currentChar);
        advance();

        // Continue with alphanum
        while (currentChar != -1 && isAlphanum(currentChar)) {
            lexeme.append((char) currentChar);
            advance();
        }

        return new Token(Token.TokenType.INVALIDID, lexeme.toString(), tokenLine);
    }

    // ==================== Number Scanner (Master DFA) ====================
    /**
     * Master number scanner - follows the master.png flow:
     * integer --> fraction --> e[+|-] --> integer
     *
     * Each step is optional after integer (double circles = accepting states)
     */
    private Token scanNumber(int tokenLine) throws IOException {
        StringBuilder lexeme = new StringBuilder();

        // Step 1: Scan integer part (required)
        String integerPart = scanInteger();
        if (integerPart == null) {
            // This shouldn't happen since we check isDigit before calling
            return new Token(Token.TokenType.INVALIDNUM, lexeme.toString(), tokenLine);
        }
        lexeme.append(integerPart);

        // Check if we stopped early due to leading zero
        // If integer is "0" and next char is digit, return just the 0
        if (integerPart.equals("0") && isDigit(currentChar)) {
            return new Token(Token.TokenType.INTEGER, "0", tokenLine);
        }

        // Step 2: Try to scan fraction (optional)
        if (currentChar == '.') {
            String fractionResult = scanFraction();

            if (fractionResult == null) {
                // No valid fraction (dot not followed by digit), return integer
                return new Token(Token.TokenType.INTEGER, lexeme.toString(), tokenLine);
            }

            if (fractionResult.startsWith("SPLIT:")) {
                // Fraction had trailing zeros - return float, zeros backed up
                lexeme.append(fractionResult.substring(6));
                return new Token(Token.TokenType.FLOAT, lexeme.toString(), tokenLine);
            }

            lexeme.append(fractionResult);

            // Step 3: Try to scan exponent (optional, only after fraction)
            if (currentChar == 'e') {
                String exponentResult = scanExponent();

                if (exponentResult != null) {
                    lexeme.append(exponentResult);
                }
            }

            return new Token(Token.TokenType.FLOAT, lexeme.toString(), tokenLine);
        }

        // No fraction, return as integer
        return new Token(Token.TokenType.INTEGER, lexeme.toString(), tokenLine);
    }

    // ==================== Integer Scanner (DFA from drawio (1)) ====================
    /**
     * integer ::= nonzero digit* | 0
     *
     * DFA:
     *   Start --nonzero--> Accept --digit--> Accept (loop)
     *   Start --0--> Accept
     *
     * @return The integer string, or null if invalid
     */
    private String scanInteger() throws IOException {
        StringBuilder lexeme = new StringBuilder();

        if (currentChar == '0') {
            // Path: Start --0--> Accept
            lexeme.append((char) currentChar);
            advance();
            // Don't consume more digits here - let caller handle leading zero case
            return lexeme.toString();
        }
        else if (isNonZero(currentChar)) {
            // Path: Start --nonzero--> Accept --digit--> Accept (loop)
            lexeme.append((char) currentChar);
            advance();

            while (isDigit(currentChar)) {
                lexeme.append((char) currentChar);
                advance();
            }
            return lexeme.toString();
        }

        // Not a valid integer start
        return null;
    }

    // ==================== Fraction Scanner (DFA from drawio (2)) ====================
    /**
     * fraction ::= .digit*nonzero | .0
     *
     * DFA:
     *   Start --(dot)--> S1 --digit--> S1 (loop) --nonzero--> Accept
     *   Start --(dot)--> S1 --0--> Accept
     *
     * @return The fraction string (including dot),
     *         "SPLIT:fraction" if trailing zeros were removed,
     *         or null if not a valid fraction
     */
    private String scanFraction() throws IOException {
        // Must start with dot
        if (currentChar != '.') {
            return null;
        }

        advance(); // consume the dot

        // Must have at least one digit after dot
        if (!isDigit(currentChar)) {
            // Not a fraction, backup the dot
            backup('.');
            advance();
            return null;
        }

        StringBuilder fraction = new StringBuilder();
        fraction.append('.');

        // Collect all digits after the dot
        StringBuilder digits = new StringBuilder();
        while (isDigit(currentChar)) {
            digits.append((char) currentChar);
            advance();
        }

        String digitStr = digits.toString();

        // Validate: must end with nonzero OR be just "0"
        char lastDigit = digitStr.charAt(digitStr.length() - 1);

        if (digitStr.equals("0") || lastDigit != '0') {
            // Valid fraction: .0 or .XXXnonzero
            fraction.append(digitStr);
            return fraction.toString();
        }
        else {
            // Invalid: has trailing zeros (like .340)
            // Remove trailing zeros, keep valid part
            int endIndex = digitStr.length() - 1;
            while (endIndex > 0 && digitStr.charAt(endIndex) == '0') {
                endIndex--;
            }

            // Build valid fraction
            if (endIndex >= 0 && digitStr.charAt(endIndex) != '0') {
                fraction.append(digitStr.substring(0, endIndex + 1));
            } else if (digitStr.charAt(0) == '0') {
                fraction.append('0');
            }

            // Backup: current char + trailing zeros (in reverse order)
            if (currentChar != -1) {
                backup((char) currentChar);
            }
            String trailingZeros = digitStr.substring(endIndex + 1);
            for (int i = trailingZeros.length() - 1; i >= 0; i--) {
                backup(trailingZeros.charAt(i));
            }
            advance(); // Synchronize currentChar with reader

            // Signal that we split the fraction
            return "SPLIT:" + fraction.toString();
        }
    }

    // ==================== Exponent Scanner (DFA from drawio (4) and (5)) ====================
    /**
     * exponent ::= e[+|-] integer
     *
     * DFA for e[+|-]:
     *   Start --e--> S1 --+--> Accept
     *   Start --e--> S1 ----> Accept
     *   Start --e--> S1 --λ--> Accept (epsilon, no sign)
     *
     * Then followed by integer DFA
     *
     * @return The exponent string (e.g., "e10", "e-5", "e+123"), or null if invalid
     */
    private String scanExponent() throws IOException {
        // Must start with 'e'
        if (currentChar != 'e') {
            return null;
        }

        StringBuilder exponent = new StringBuilder();
        exponent.append('e');
        advance();

        // Optional sign: + or -
        if (currentChar == '+' || currentChar == '-') {
            exponent.append((char) currentChar);
            advance();
        }

        // Must have integer after e[+|-]
        if (!isDigit(currentChar)) {
            // No valid integer, backup everything and return null
            for (int i = exponent.length() - 1; i >= 0; i--) {
                backup(exponent.charAt(i));
            }
            advance();
            return null;
        }

        // Scan the exponent integer
        if (currentChar == '0') {
            exponent.append((char) currentChar);
            advance();

            // Check for leading zero in exponent (e.g., e01)
            if (isDigit(currentChar)) {
                // Invalid: leading zero in exponent
                // Return just e0, leave remaining digits for next token
                return exponent.toString();
            }
        }
        else if (isNonZero(currentChar)) {
            exponent.append((char) currentChar);
            advance();

            while (isDigit(currentChar)) {
                exponent.append((char) currentChar);
                advance();
            }
        }
        else {
            // No valid integer after sign
            for (int i = exponent.length() - 1; i >= 0; i--) {
                backup(exponent.charAt(i));
            }
            advance();
            return null;
        }

        return exponent.toString();
    }

    // ==================== Operator and Punctuation Scanner ====================

    private Token scanOperatorOrPunctuation() throws IOException {
        int tokenLine = line;
        int c = currentChar;
        advance();

        switch (c) {
            case '=':
                if (currentChar == '=') {
                    advance();
                    return new Token(Token.TokenType.EQ, "==", tokenLine);
                }
                return new Token(Token.TokenType.ASSIGN, "=", tokenLine);

            case '<':
                if (currentChar == '>') {
                    advance();
                    return new Token(Token.TokenType.NEQ, "<>", tokenLine);
                } else if (currentChar == '=') {
                    advance();
                    return new Token(Token.TokenType.LEQ, "<=", tokenLine);
                }
                return new Token(Token.TokenType.LT, "<", tokenLine);

            case '>':
                if (currentChar == '=') {
                    advance();
                    return new Token(Token.TokenType.GEQ, ">=", tokenLine);
                }
                return new Token(Token.TokenType.GT, ">", tokenLine);

            case '+':
                return new Token(Token.TokenType.PLUS, "+", tokenLine);

            case '-':
                return new Token(Token.TokenType.MINUS, "-", tokenLine);

            case '*':
                return new Token(Token.TokenType.MULT, "*", tokenLine);

            case '/':
                return new Token(Token.TokenType.DIV, "/", tokenLine);

            case '(':
                return new Token(Token.TokenType.LPAREN, "(", tokenLine);

            case ')':
                return new Token(Token.TokenType.RPAREN, ")", tokenLine);

            case '{':
                return new Token(Token.TokenType.LBRACE, "{", tokenLine);

            case '}':
                return new Token(Token.TokenType.RBRACE, "}", tokenLine);

            case '[':
                return new Token(Token.TokenType.LBRACKET, "[", tokenLine);

            case ']':
                return new Token(Token.TokenType.RBRACKET, "]", tokenLine);

            case ';':
                return new Token(Token.TokenType.SEMICOLON, ";", tokenLine);

            case ',':
                return new Token(Token.TokenType.COMMA, ",", tokenLine);

            case '.':
                return new Token(Token.TokenType.DOT, ".", tokenLine);

            case ':':
                if (currentChar == ':') {
                    advance();
                    return new Token(Token.TokenType.COLONCOLON, "::", tokenLine);
                }
                return new Token(Token.TokenType.COLON, ":", tokenLine);

            default:
                return new Token(Token.TokenType.INVALIDCHAR, String.valueOf((char) c), tokenLine);
        }
    }

    // ==================== Comment Scanners ====================

    private Token scanInlineComment(int tokenLine) throws IOException {
        StringBuilder lexeme = new StringBuilder();
        lexeme.append("//");
        advance(); // consume second '/'

        // Read until end of line
        while (currentChar != -1 && currentChar != '\n') {
            lexeme.append((char) currentChar);
            advance();
        }

        return new Token(Token.TokenType.INLINECMT, lexeme.toString(), tokenLine);
    }

    private Token scanBlockComment(int tokenLine) throws IOException {
        StringBuilder lexeme = new StringBuilder();
        lexeme.append("/*");
        advance(); // consume '*'

        while (currentChar != -1) {
            if (currentChar == '*') {
                lexeme.append((char) currentChar);
                advance();
                if (currentChar == '/') {
                    lexeme.append((char) currentChar);
                    advance();
                    return new Token(Token.TokenType.BLOCKCMT, lexeme.toString(), tokenLine);
                }
            } else {
                lexeme.append((char) currentChar);
                advance();
            }
        }

        // EOF reached without closing */
        return new Token(Token.TokenType.UNTERMINATEDCMT, lexeme.toString(), tokenLine);
    }

    /**
     * Get current line number
     */
    public int getLine() {
        return line;
    }
}
