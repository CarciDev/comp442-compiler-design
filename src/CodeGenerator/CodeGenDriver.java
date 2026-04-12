package src.CodeGenerator;

import src.LexicalAnalyzer.LexicalAnalyzer;
import src.SyntacticalAnalyzer.*;
import src.SemanticAnalyzer.*;
import java.io.*;
import java.util.*;

/**
 * Driver that processes .src files through all compiler phases
 * (lexing, parsing, semantic analysis, code generation) and
 * outputs a .moon file for each input.
 */
public class CodeGenDriver {

    public static void main(String[] args) {
        if (args.length == 0) {
            processAllSrcFiles("tests/testcases");
            return;
        }
        for (String f : args) {
            File file = new File(f);
            if (file.isDirectory()) {
                processAllSrcFiles(f);
            } else {
                processFile(f);
            }
        }
    }

    public static void processAllSrcFiles(String directory) {
        File dir = new File(directory);
        File[] srcFiles = dir.listFiles((d, n) -> n.endsWith(".src"));
        if (srcFiles == null || srcFiles.length == 0) {
            System.out.println("No .src files found in: " + directory);
            return;
        }
        Arrays.sort(srcFiles);
        for (File f : srcFiles) processFile(f.getPath());
    }

    public static void processFile(String filename) {
        System.out.println("Processing: " + filename);
        File srcFile = new File(filename);
        File parentDir = srcFile.getParentFile();
        String baseName = srcFile.getName().replaceAll("\\.src$", "");
        File outputDir = (parentDir != null) ? new File(parentDir, "output/" + baseName) : new File("output/" + baseName);
        if (!outputDir.exists()) outputDir.mkdirs();

        // Run previous phases (A1-A3)
        ASTDriver.processFile(filename);

        // Parse for semantic analysis
        ASTNode astRoot = parseFile(filename);
        if (astRoot == null) {
            System.out.println("  Code generation skipped (no AST)");
            return;
        }

        // Pass 1: Symbol table construction
        SymbolTableBuilder builder = new SymbolTableBuilder();
        astRoot.accept(builder);
        SymbolTable globalTable = builder.getGlobalTable();

        // Pass 2: Semantic checking
        SemanticChecker checker = new SemanticChecker(globalTable, builder.getFuncTableMap());
        astRoot.accept(checker);

        // Collect errors
        List<String[]> allErrors = new ArrayList<>();
        allErrors.addAll(builder.getErrors());
        allErrors.addAll(checker.getErrors());
        allErrors.sort(Comparator.comparingInt(a -> Integer.parseInt(a[0])));

        // Write symbol tables
        String stFile = new File(outputDir, baseName + ".outsymboltables").getPath();
        try (PrintWriter w = new PrintWriter(new FileWriter(stFile))) {
            w.print(SemanticDriver.printSymbolTable(globalTable));
        } catch (IOException e) {
            System.err.println("Error writing symbol tables: " + e.getMessage());
        }

        // Write semantic errors
        String seFile = new File(outputDir, baseName + ".outsemanticerrors").getPath();
        try (PrintWriter w = new PrintWriter(new FileWriter(seFile))) {
            if (allErrors.isEmpty()) {
                w.print(" ");
            } else {
                for (String[] err : allErrors)
                    w.println("[" + err[2] + "][" + err[1] + "] line " + err[0] + ": " + err[3]);
            }
        } catch (IOException e) {
            System.err.println("Error writing semantic errors: " + e.getMessage());
        }

        // Check for hard errors (skip code gen if present)
        boolean hasErrors = false;
        for (String[] err : allErrors) {
            if ("error".equals(err[2])) { hasErrors = true; break; }
        }

        // Pass 3: Compute memory sizes and offsets (pre-computation phase)
        ComputeMemSizeVisitor memSizer = new ComputeMemSizeVisitor(globalTable);
        astRoot.accept(memSizer);

        // Pass 4: Code generation
        CodeGenerator codeGen = new CodeGenerator(globalTable, builder.getFuncTableMap());
        astRoot.accept(codeGen);
        String moonCode = codeGen.getMoonCode();

        // Write .moon file
        String moonFile = new File(outputDir, baseName + ".moon").getPath();
        try (PrintWriter w = new PrintWriter(new FileWriter(moonFile))) {
            w.print(moonCode);
        } catch (IOException e) {
            System.err.println("Error writing moon file: " + e.getMessage());
        }

        System.out.println("  Generated: " + stFile);
        System.out.println("  Generated: " + seFile);
        System.out.println("  Generated: " + moonFile);
        if (!allErrors.isEmpty())
            System.out.println("  Semantic errors/warnings: " + allErrors.size());
        if (hasErrors)
            System.out.println("  WARNING: Semantic errors present — generated code may not execute correctly");
    }

    private static ASTNode parseFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            LexicalAnalyzer lexer = new LexicalAnalyzer(reader);
            PrintWriter nw = new PrintWriter(new StringWriter());
            Parser parser = new Parser(lexer, nw, nw);
            parser.parse();
            return parser.getAstRoot();
        } catch (IOException e) {
            System.err.println("Error parsing file: " + e.getMessage());
            return null;
        }
    }
}
