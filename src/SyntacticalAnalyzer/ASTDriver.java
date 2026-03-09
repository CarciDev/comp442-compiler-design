package src.SyntacticalAnalyzer;

import src.LexicalAnalyzer.LexicalAnalyzer;
import src.LexicalAnalyzer.Token;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ASTDriver {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java src.SyntacticalAnalyzer.ASTDriver <file.src> [file2.src ...]");
            System.out.println("       Or provide no arguments to process all .src files in current directory");
            processAllSrcFiles(".");
            return;
        }

        for (String filename : args) {
            processFile(filename);
        }
    }

    public static void processAllSrcFiles(String directory) {
        File dir = new File(directory);
        File[] srcFiles = dir.listFiles((d, name) -> name.endsWith(".src"));

        if (srcFiles == null || srcFiles.length == 0) {
            System.out.println("No .src files found in directory: " + directory);
            return;
        }

        java.util.Arrays.sort(srcFiles);

        for (File srcFile : srcFiles) {
            processFile(srcFile.getPath());
        }
    }

    public static void processFile(String filename) {
        System.out.println("Processing: " + filename);

        if (!filename.endsWith(".src")) {
            System.err.println("Warning: File does not have .src extension: " + filename);
        }

        File srcFile = new File(filename);
        File parentDir = srcFile.getParentFile();
        File outputDir = (parentDir != null) ? new File(parentDir, "output") : new File("output");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String baseName = srcFile.getName();
        if (baseName.endsWith(".src")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }

        // A1 output files
        String lexTokensFile = new File(outputDir, baseName + ".outlextokens").getPath();
        String lexErrorsFile = new File(outputDir, baseName + ".outlexerrors").getPath();
        // A2 output files
        String derivationFile = new File(outputDir, baseName + ".outderivation").getPath();
        String syntaxErrorsFile = new File(outputDir, baseName + ".outsyntaxerrors").getPath();
        // A3 output files
        String astFile = new File(outputDir, baseName + ".outast").getPath();
        String dotFile = new File(outputDir, baseName + ".outast.dot").getPath();

        // Pass 1: Lexer output (A1)
        generateLexerOutput(filename, lexTokensFile, lexErrorsFile);

        // Pass 2: Parser + AST output (A2 + A3)
        try (
                BufferedReader reader = new BufferedReader(new FileReader(filename));
                PrintWriter derivationWriter = new PrintWriter(new FileWriter(derivationFile));
                PrintWriter errorWriter = new PrintWriter(new FileWriter(syntaxErrorsFile));
                PrintWriter astWriter = new PrintWriter(new FileWriter(astFile));
                PrintWriter dotWriter = new PrintWriter(new FileWriter(dotFile))
        ) {
            LexicalAnalyzer lexer = new LexicalAnalyzer(reader);
            Parser parser = new Parser(lexer, derivationWriter, errorWriter);

            boolean result = parser.parse();

            // Write AST output
            ASTNode astRoot = parser.getAstRoot();
            if (astRoot != null) {
                // Text format (.outast)
                PrintStream oldOut = System.out;
                System.setOut(new PrintStream(new OutputStream() {
                    public void write(int b) { astWriter.write(b); }
                    public void write(byte[] b, int off, int len) {
                        astWriter.write(new String(b, off, len));
                    }
                }));
                astRoot.accept(new ASTPrinter());
                System.setOut(oldOut);

                // Graphviz DOT format (.outast.dot)
                astRoot.accept(new ASTDotPrinter(dotWriter));
            }

            System.out.println("  Result: " + (result ? "SUCCESS" : "ERRORS FOUND"));
            System.out.println("  Errors: " + parser.getErrorCount());
            System.out.println("  Generated: " + lexTokensFile);
            System.out.println("  Generated: " + lexErrorsFile);
            System.out.println("  Generated: " + derivationFile);
            System.out.println("  Generated: " + syntaxErrorsFile);
            System.out.println("  Generated: " + astFile);
            System.out.println("  Generated: " + dotFile);

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename + " - " + e.getMessage());
        }
    }

    private static void generateLexerOutput(String filename, String tokensFile, String errorsFile) {
        List<Token> tokens = new ArrayList<>();
        List<Token> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            LexicalAnalyzer lexer = new LexicalAnalyzer(reader);
            Token token;
            do {
                token = lexer.nextToken();
                if (token.isError()) {
                    errors.add(token);
                }
                if (token.getType() != Token.TokenType.EOF) {
                    tokens.add(token);
                }
            } while (token.getType() != Token.TokenType.EOF);
        } catch (IOException e) {
            System.err.println("Error during lexer pass: " + e.getMessage());
            return;
        }

        // Write .outlextokens
        try (PrintWriter writer = new PrintWriter(new FileWriter(tokensFile))) {
            if (!tokens.isEmpty()) {
                StringBuilder currentLine = new StringBuilder();
                int currentLineNum = tokens.get(0).getLine();
                for (Token token : tokens) {
                    if (token.getLine() != currentLineNum) {
                        writer.println(currentLine.toString());
                        currentLine = new StringBuilder();
                        currentLineNum = token.getLine();
                    }
                    if (currentLine.length() > 0) currentLine.append(" ");
                    currentLine.append(token.toString());
                }
                if (currentLine.length() > 0) writer.print(currentLine.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing tokens file: " + e.getMessage());
        }

        // Write .outlexerrors
        try (PrintWriter writer = new PrintWriter(new FileWriter(errorsFile))) {
            if (errors.isEmpty()) {
                writer.print(" ");
            } else {
                for (Token error : errors) {
                    String desc;
                    switch (error.getType()) {
                        case INVALIDNUM: desc = "Invalid number"; break;
                        case INVALIDID: desc = "Invalid identifier"; break;
                        case INVALIDCHAR: desc = "Invalid character"; break;
                        case UNTERMINATEDCMT: desc = "Unterminated block comment"; break;
                        default: desc = "Unknown error"; break;
                    }
                    writer.println("Lexical error: " + desc + ": \"" +
                            error.getLexeme() + "\": line " + error.getLine() + ".");
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing lex errors file: " + e.getMessage());
        }
    }
}
