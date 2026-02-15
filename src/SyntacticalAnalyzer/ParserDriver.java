package src.SyntacticalAnalyzer;

import src.LexicalAnalyzer.LexicalAnalyzer;

import java.io.*;

/**
 * Driver for the Syntactic Analyzer.
 * Processes .src files and generates:
 * - .outderivation: Leftmost derivation proving the program is valid
 * - .outsyntaxerrors: Syntax error messages with location information
 *
 * Also runs the lexical analyzer to produce .outlextokens and .outlexerrors.
 */
public class ParserDriver {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java src.SyntacticAnalyzer.ParserDriver <file.src> [file2.src ...]");
            System.out.println("       Or provide no arguments to process all .src files in current directory");
            processAllSrcFiles(".");
            return;
        }

        for (String filename : args) {
            processFile(filename);
        }
    }

    /**
     * Process all .src files in a directory.
     */
    public static void processAllSrcFiles(String directory) {
        File dir = new File(directory);
        File[] srcFiles = dir.listFiles((d, name) -> name.endsWith(".src"));

        if (srcFiles == null || srcFiles.length == 0) {
            System.out.println("No .src files found in directory: " + directory);
            return;
        }

        // Sort for consistent ordering
        java.util.Arrays.sort(srcFiles);

        for (File srcFile : srcFiles) {
            processFile(srcFile.getPath());
        }
    }

    /**
     * Process a single source file through the lexer and parser.
     */
    public static void processFile(String filename) {
        System.out.println("Processing: " + filename);

        if (!filename.endsWith(".src")) {
            System.err.println("Warning: File does not have .src extension: " + filename);
        }

        String derivationFile = filename + ".outderivation";
        String errorsFile = filename + ".outsyntaxerrors";

        try (
                BufferedReader reader = new BufferedReader(new FileReader(filename));
                PrintWriter derivationWriter = new PrintWriter(new FileWriter(derivationFile));
                PrintWriter errorWriter = new PrintWriter(new FileWriter(errorsFile))
        ) {
            // Create lexer and parser
            LexicalAnalyzer lexer = new LexicalAnalyzer(reader);
            Parser parser = new Parser(lexer, derivationWriter, errorWriter);

            // Parse
            boolean result = parser.parse();

            System.out.println("  Result: " + (result ? "SUCCESS" : "ERRORS FOUND"));
            System.out.println("  Errors: " + parser.getErrorCount());
            System.out.println("  Generated: " + derivationFile);
            System.out.println("  Generated: " + errorsFile);

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename + " - " + e.getMessage());
        }
    }
}