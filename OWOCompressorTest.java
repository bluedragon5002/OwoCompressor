import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * OWO kompresijas algoritma testa piemērs
 * Testē visus 4 failus no TestFiles mapes
 */
public class OWOCompressorTest {
    
    private static class CompressionResult {
        String fileName;
        long originalSize;
        long compressedSize;
        double compressionRatio;
        long compressionTime;
        long decompressionTime;
        boolean decompressedSuccessfully;
        
        CompressionResult(String fileName, long originalSize, long compressedSize, 
                         long compressionTime, long decompressionTime, 
                         boolean decompressedSuccessfully) {
            this.fileName = fileName;
            this.originalSize = originalSize;
            this.compressedSize = compressedSize;
            this.compressionRatio = (1.0 - (double)compressedSize / originalSize) * 100;
            this.compressionTime = compressionTime;
            this.decompressionTime = decompressionTime;
            this.decompressedSuccessfully = decompressedSuccessfully;
        }
        
        @Override
        public String toString() {
            String status = decompressedSuccessfully ? "✓" : "✗";
            return String.format(
                "%s %-15s | Original: %10d B | Compressed: %10d B | Ratio: %6.2f%% | " +
                "Compress: %5d ms | Decompress: %5d ms",
                status, fileName, originalSize, compressedSize, compressionRatio,
                compressionTime, decompressionTime
            );
        }
    }
    
    public static void main(String[] args) {
        try {
            String[] testFiles = {
                "TestFiles/File1.html",
                "TestFiles/File2.html",
                "TestFiles/File3.html",
                "TestFiles/File4.html"
            };
            
            List<CompressionResult> results = new ArrayList<>();
            
            System.out.println("════════════════════════════════════════════════════════════════════════════════════");
            System.out.println("OWO Compression Algorithm - Comprehensive Test Suite");
            System.out.println("════════════════════════════════════════════════════════════════════════════════════\n");
            
            for (String testFile : testFiles) {
                System.out.println("Testing: " + testFile);
                System.out.println("─────────────────────────────────────────────────────────────────────────────────");
                
                CompressionResult result = testFile(testFile);
                results.add(result);
                
                System.out.println();
            }
            
            // Print summary
            printSummary(results);
            
        } catch (Exception e) {
            System.err.println("Kļūda: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static CompressionResult testFile(String inputFile) throws Exception {
        // Read original file
        byte[] originalContent = Files.readAllBytes(Paths.get(inputFile));
        String originalText = new String(originalContent, "UTF-8");
        long originalSize = originalContent.length;
        
        String fileName = new File(inputFile).getName();
        String compressedFile = "TestFiles/" + fileName.replace(".html", ".owo");
        String decompressedFile = "TestFiles/" + fileName.replace(".html", "_decompressed.html");
        
        System.out.println("  Oriģinālais izmērs: " + formatFileSize(originalSize));
        
        // Compress
        System.out.print("  Kompresē... ");
        long compressionStart = System.currentTimeMillis();
        OWOCompressor.compress(inputFile, compressedFile);
        long compressionTime = System.currentTimeMillis() - compressionStart;
        
        long compressedSize = Files.size(Paths.get(compressedFile));
        double ratio = (1.0 - (double)compressedSize / originalSize) * 100;
        
        System.out.println("Pabeigts (" + compressionTime + " ms)");
        System.out.println("  Kompresētais izmērs: " + formatFileSize(compressedSize));
        System.out.println("  Kompresijas koeficients: " + String.format("%.2f", ratio) + "%");
        
        // Decompress
        System.out.print("  Dekompresē... ");
        long decompressionStart = System.currentTimeMillis();
        OWOCompressor.decompress(compressedFile, decompressedFile);
        long decompressionTime = System.currentTimeMillis() - decompressionStart;
        System.out.println("Pabeigts (" + decompressionTime + " ms)");
        
        // Verify
        String decompressed = new String(
            Files.readAllBytes(Paths.get(decompressedFile)), "UTF-8");
        
        boolean success = originalText.equals(decompressed);
        
        if (success) {
            System.out.println("  ✓ Dekompresija veiksmīga! Dati sakrīt.");
        } else {
            System.out.println("  ✗ Kļūda: Dekompresētie dati nesakrīt ar oriģinālu!");
            System.out.println("    Oriģināla garums: " + originalText.length());
            System.out.println("    Dekompresēta garums: " + decompressed.length());
        }
        
        System.out.println("  Failu izmēri:");
        System.out.println("    " + inputFile + ": " + formatFileSize(Files.size(Paths.get(inputFile))));
        System.out.println("    " + compressedFile + ": " + formatFileSize(Files.size(Paths.get(compressedFile))));
        System.out.println("    " + decompressedFile + ": " + formatFileSize(Files.size(Paths.get(decompressedFile))));
        
        return new CompressionResult(fileName, originalSize, compressedSize, 
                                     compressionTime, decompressionTime, success);
    }
    
    private static void printSummary(List<CompressionResult> results) {
        System.out.println("════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("Apkopojums");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════\n");
        
        System.out.println("Detalizēti rezultāti:");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");
        
        for (CompressionResult result : results) {
            System.out.println(result);
        }
        
        System.out.println("\nKopējā statistika:");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");
        
        long totalOriginal = 0;
        long totalCompressed = 0;
        long totalCompressionTime = 0;
        long totalDecompressionTime = 0;
        int successCount = 0;
        
        for (CompressionResult result : results) {
            totalOriginal += result.originalSize;
            totalCompressed += result.compressedSize;
            totalCompressionTime += result.compressionTime;
            totalDecompressionTime += result.decompressionTime;
            if (result.decompressedSuccessfully) {
                successCount++;
            }
        }
        
        double overallRatio = (1.0 - (double)totalCompressed / totalOriginal) * 100;
        
        System.out.println("  Kopējais oriģinālais izmērs: " + formatFileSize(totalOriginal));
        System.out.println("  Kopējais kompresētais izmērs: " + formatFileSize(totalCompressed));
        System.out.println("  Kopējais kompresijas koeficients: " + String.format("%.2f", overallRatio) + "%");
        System.out.println("  Kopējais ietaupītais vietas: " + formatFileSize(totalOriginal - totalCompressed));
        System.out.println("  Vidējais kompresijas laiks: " + (totalCompressionTime / results.size()) + " ms");
        System.out.println("  Vidējais dekompresijas laiks: " + (totalDecompressionTime / results.size()) + " ms");
        System.out.println("  Sekmīgas dekompresijas: " + successCount + "/" + results.size());
        
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════");
        
        // Print individual file statistics
        System.out.println("\nFailu reitings pēc kompresijas efektivitātes (labākie pirmie):");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");
        
        results.sort((a, b) -> Double.compare(b.compressionRatio, a.compressionRatio));
        
        int rank = 1;
        for (CompressionResult result : results) {
            System.out.printf("%d. %-15s - Kompresijas koeficients: %6.2f%%\n", 
                            rank++, result.fileName, result.compressionRatio);
        }
        
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════");
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
