import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * OWO kompresijas algoritma testa piemērs
 */
public class OWOCompressorTest {
    
    public static void main(String[] args) {
        try {
            // Izveidot testa HTML failu
            String testHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Testa lapa</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Sveiki pasaule!</h1>\n" +
                "    <p>Šis ir testa HTML fails kompresijas algoritma pārbaudei.</p>\n" +
                "    <p>Algoritms izmanto BWT, MTF, RLE un Huffman kodēšanu.</p>\n" +
                "    <ul>\n" +
                "        <li>Punkts 1</li>\n" +
                "        <li>Punkts 2</li>\n" +
                "        <li>Punkts 3</li>\n" +
                "    </ul>\n" +
                "</body>\n" +
                "</html>";
            
            String inputFile = "test.html";
            String compressedFile = "test.owo";
            String decompressedFile = "test_decompressed.html";
            
            // Saglabāt testa failu
            Files.write(Paths.get(inputFile), testHtml.getBytes("UTF-8"));
            System.out.println("Izveidots testa fails: " + inputFile);
            System.out.println("Oriģinālais izmērs: " + testHtml.length() + " baiti");
            
            // Kompresēt
            System.out.println("\nKompresē...");
            OWOCompressor.compress(inputFile, compressedFile);
            
            long compressedSize = Files.size(Paths.get(compressedFile));
            System.out.println("Kompresētais izmērs: " + compressedSize + " baiti");
            double ratio = (1.0 - (double)compressedSize / testHtml.length()) * 100;
            System.out.println("Kompresijas koeficients: " + 
                String.format("%.2f", ratio) + "%");
            
            // Dekompresēt
            System.out.println("\nDekompresē...");
            OWOCompressor.decompress(compressedFile, decompressedFile);
            
            // Pārbaudīt, vai dati sakrīt
            String decompressed = new String(
                Files.readAllBytes(Paths.get(decompressedFile)), "UTF-8");
            
            if (testHtml.equals(decompressed)) {
                System.out.println("✓ Dekompresija veiksmīga! Dati sakrīt.");
            } else {
                System.out.println("✗ Kļūda: Dekompresētie dati nesakrīt ar oriģinālu!");
                System.out.println("Oriģināla garums: " + testHtml.length());
                System.out.println("Dekompresēta garums: " + decompressed.length());
            }
            
            // Rādīt failu izmērus
            System.out.println("\nFailu izmēri:");
            System.out.println("  " + inputFile + ": " + 
                Files.size(Paths.get(inputFile)) + " baiti");
            System.out.println("  " + compressedFile + ": " + 
                Files.size(Paths.get(compressedFile)) + " baiti");
            System.out.println("  " + decompressedFile + ": " + 
                Files.size(Paths.get(decompressedFile)) + " baiti");
            
        } catch (Exception e) {
            System.err.println("Kļūda: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

