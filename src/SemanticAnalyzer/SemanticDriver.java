package src.SemanticAnalyzer;

import src.LexicalAnalyzer.LexicalAnalyzer;
import src.SyntacticalAnalyzer.*;
import java.io.*;
import java.util.*;

public class SemanticDriver {

    public static void main(String[] args) {
        if (args.length == 0) {
            processAllSrcFiles("tests/testcases");
            return;
        }
        for (String f : args) processFile(f);
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

        // A1-A3 outputs
        ASTDriver.processFile(filename);

        // Parse for semantic analysis
        ASTNode astRoot = parseFile(filename);
        if (astRoot == null) {
            System.out.println("  Semantic analysis skipped (no AST)");
            return;
        }

        // Pass 1: Symbol table construction
        SymbolTableBuilder builder = new SymbolTableBuilder();
        astRoot.accept(builder);
        SymbolTable globalTable = builder.getGlobalTable();

        // Pass 2: Semantic checking
        SemanticChecker checker = new SemanticChecker(globalTable, builder.getFuncTableMap());
        astRoot.accept(checker);

        // Collect and sort errors by line number
        List<String[]> allErrors = new ArrayList<>();
        allErrors.addAll(builder.getErrors());
        allErrors.addAll(checker.getErrors());
        allErrors.sort(Comparator.comparingInt(a -> Integer.parseInt(a[0])));

        // Write .outsymboltables
        String stFile = new File(outputDir, baseName + ".outsymboltables").getPath();
        try (PrintWriter w = new PrintWriter(new FileWriter(stFile))) {
            w.print(printSymbolTable(globalTable));
        } catch (IOException e) {
            System.err.println("Error writing symbol tables: " + e.getMessage());
        }

        // Write .outsemanticerrors
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

        System.out.println("  Generated: " + stFile);
        System.out.println("  Generated: " + seFile);
        if (!allErrors.isEmpty())
            System.out.println("  Semantic errors/warnings: " + allErrors.size());
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

    // ==================== Symbol Table Printer ====================

    static String printSymbolTable(SymbolTable table) {
        StringBuilder sb = new StringBuilder();
        printTable(table, 0, sb);
        sb.append("\n");
        return sb.toString();
    }

    private static final int[] BW = {83, 75, 66};
    private static final String[] PRE = {"", "|    ", "|    |     "};
    private static final String[] SUF = {"", "  |", "  |  |"};

    private static void printTable(SymbolTable table, int level, StringBuilder sb) {
        if (level >= BW.length) return;
        String pre = PRE[level], suf = SUF[level];
        int bw = BW[level], cw = bw - 4;

        sb.append(pre).append("=".repeat(bw)).append(suf).append("\n");
        sb.append(pre).append("| ").append(pad("table: " + table.getName(), cw)).append(" |").append(suf).append("\n");
        sb.append(pre).append("=".repeat(bw)).append(suf).append("\n");

        for (SymbolTableEntry e : table.getEntries()) {
            sb.append(pre).append(fmtEntry(e, bw)).append(suf).append("\n");
            if (e.getLink() != null)
                printTable(e.getLink(), level + 1, sb);
        }
        sb.append(pre).append("=".repeat(bw)).append(suf).append("\n");
    }

    private static String fmtEntry(SymbolTableEntry e, int bw) {
        String kind = e.getKind(), name = e.getName(), type = e.getType(), vis = e.getVisibility();
        if ("class".equals(kind) || "inherit".equals(kind))
            return String.format("| %-10s | %-" + (bw - 17) + "s |", kind, name);
        if (vis != null)
            return String.format("| %-10s | %-12s | %-" + (bw - 45) + "s | %-10s |", kind, name, type, vis);
        return String.format("| %-10s | %-12s | %-" + (bw - 32) + "s |", kind, name, type);
    }

    private static String pad(String s, int w) {
        return s.length() >= w ? s.substring(0, w) : s + " ".repeat(w - s.length());
    }
}
