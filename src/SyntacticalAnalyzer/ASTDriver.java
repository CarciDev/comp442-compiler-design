package src.SyntacticalAnalyzer;

import src.LexicalAnalyzer.LexicalAnalyzer;

import java.io.*;

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

        String derivationFile = new File(outputDir, baseName + ".outderivation").getPath();
        String errorsFile = new File(outputDir, baseName + ".outsyntaxerrors").getPath();
        String astFile = new File(outputDir, baseName + ".outast").getPath();
        String dotFile = new File(outputDir, baseName + ".outast.dot").getPath();

        try (
                BufferedReader reader = new BufferedReader(new FileReader(filename));
                PrintWriter derivationWriter = new PrintWriter(new FileWriter(derivationFile));
                PrintWriter errorWriter = new PrintWriter(new FileWriter(errorsFile));
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
            System.out.println("  Generated: " + derivationFile);
            System.out.println("  Generated: " + errorsFile);
            System.out.println("  Generated: " + astFile);
            System.out.println("  Generated: " + dotFile);

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename + " - " + e.getMessage());
        }
    }
}
