package src.LexicalAnalyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Driver for the Lexical Analyzer.
 * Processes .src files and generates:
 * - .outlextokens: Token stream with type, lexeme, and location
 * - .outlextokensflaci: Token stream formatted for Flaci tool
 * - .outlexerrors: Error messages with descriptions and locations
 */
public class LexDriver {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java src.LexicalAnalyzer.LexDriver <filename.src> [filename2.src ...]");
            System.out.println("       Or provide no arguments to process all .src files in current directory");
            processAllSrcFiles(".");
            return;
        }

        for (String filename : args) {
            processFile(filename);
        }
    }

    /**
     * Process all .src files in a directory
     */
    public static void processAllSrcFiles(String directory) {
        File dir = new File(directory);
        File[] srcFiles = dir.listFiles((d, name) -> name.endsWith(".src"));

        if (srcFiles == null || srcFiles.length == 0) {
            System.out.println("No .src files found in directory: " + directory);
            return;
        }

        for (File srcFile : srcFiles) {
            processFile(srcFile.getPath());
        }
    }

    /**
     * Process a single source file and generate output files
     */
    public static void processFile(String filename) {
        System.out.println("Processing: " + filename);

        // Validate file extension
        if (!filename.endsWith(".src")) {
            System.err.println("Warning: File does not have .src extension: " + filename);
        }

        // Determine base name for output files
        String baseName = filename;
        if (filename.endsWith(".src")) {
            baseName = filename.substring(0, filename.length() - 4);
        }

        String tokensFile = baseName + ".outlextokens";
        String flaciFile = baseName + ".outlextokensflaci";
        String errorsFile = baseName + ".outlexerrors";

        List<Token> tokens = new ArrayList<>();
        List<Token> errors = new ArrayList<>();

        // Tokenize the file
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            LexicalAnalyzer lexer = new LexicalAnalyzer(reader);

            Token token;
            do {
                token = lexer.nextToken();

                // Track errors
                if (token.isError()) {
                    errors.add(token);
                }

                // Add all tokens except EOF (comments are included)
                if (token.getType() != Token.TokenType.EOF) {
                    tokens.add(token);
                }

            } while (token.getType() != Token.TokenType.EOF);

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filename);
            return;
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename + " - " + e.getMessage());
            return;
        }

        // Write output files
        writeTokensFile(tokensFile, tokens);
        writeFlaciFile(flaciFile, tokens);
        writeErrorsFile(errorsFile, errors);

        System.out.println("  Generated: " + tokensFile);
        System.out.println("  Generated: " + flaciFile);
        System.out.println("  Generated: " + errorsFile);
        System.out.println("  Tokens: " + tokens.size() + ", Errors: " + errors.size());
    }

    /**
     * Write the .outlextokens file with full token information
     * Tokens on the same line are grouped together on one output line
     */
    private static void writeTokensFile(String filename, List<Token> tokens) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            if (tokens.isEmpty()) {
                return;
            }

            StringBuilder currentLine = new StringBuilder();
            int currentLineNum = tokens.get(0).getLine();

            for (Token token : tokens) {
                if (token.getLine() != currentLineNum) {
                    // Write the previous line and start a new one
                    writer.println(currentLine.toString());
                    currentLine = new StringBuilder();
                    currentLineNum = token.getLine();
                }

                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(token.toString());
            }

            // Write the last line
            if (currentLine.length() > 0) {
                writer.print(currentLine.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing tokens file: " + filename + " - " + e.getMessage());
        }
    }

    /**
     * Write the .outlextokensflaci file for Flaci tool
     * Format: space-separated token type names ending with $ (EOF)
     */
    private static void writeFlaciFile(String filename, List<Token> tokens) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            StringBuilder sb = new StringBuilder();
            for (Token token : tokens) {
                // Skip error tokens and comments in Flaci output
                if (token.isError() ||
                    token.getType() == Token.TokenType.BLOCKCMT ||
                    token.getType() == Token.TokenType.INLINECMT) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(token.toFlaciString());
            }
            // Add EOF marker
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append("$");
            writer.println(sb.toString());
        } catch (IOException e) {
            System.err.println("Error writing Flaci file: " + filename + " - " + e.getMessage());
        }
    }

    /**
     * Write the .outlexerrors file with error messages
     */
    private static void writeErrorsFile(String filename, List<Token> errors) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            if (errors.isEmpty()) {
                // Output a single space when no errors (as per expected format)
                writer.print(" ");
            } else {
                for (Token error : errors) {
                    String errorType = getErrorDescription(error.getType());
                    writer.println("Lexical error: " + errorType + ": \"" +
                                   error.getLexeme() + "\": line " + error.getLine() + ".");
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing errors file: " + filename + " - " + e.getMessage());
        }
    }

    /**
     * Get human-readable error description
     */
    private static String getErrorDescription(Token.TokenType type) {
        switch (type) {
            case INVALIDNUM:
                return "Invalid number";
            case INVALIDID:
                return "Invalid identifier";
            case INVALIDCHAR:
                return "Invalid character";
            case UNTERMINATEDCMT:
                return "Unterminated block comment";
            default:
                return "Unknown error";
        }
    }
}