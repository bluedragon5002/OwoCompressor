import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.*;

public class OWOCompressorTest {

    @Test
    public void testCompressionStatistics() {
        String[] fileNames = {"TestFiles/File1.html", "TestFiles/File2.html", "TestFiles/File3.html", "TestFiles/File4.html"};
        Map<String, String> results = new HashMap<>();
        long totalOriginalSize = 0;
        long totalCompressedSize = 0;

        for (String fileName : fileNames) {
            File file = new File(fileName);
            long originalSize = file.length();
            totalOriginalSize += originalSize;
            long startTime = System.nanoTime();

            // Assuming compress() is a method that compresses the file and returns the compressed size
            long compressedSize = compress(file);
            long endTime = System.nanoTime();

            double compressionRatio = (double)originalSize / compressedSize;
            results.put(fileName, String.format("Original: %d bytes, Compressed: %d bytes, Ratio: %.2f, Time: %.2f ms", originalSize, compressedSize, compressionRatio, (endTime - startTime) / 1_000_000.0));
            totalCompressedSize += compressedSize;
        }

        // Calculate overall statistics
        double overallCompressionRatio = (double)totalOriginalSize / totalCompressedSize;
        results.put("Overall", String.format("Total Original: %d bytes, Total Compressed: %d bytes, Overall Ratio: %.2f", totalOriginalSize, totalCompressedSize, overallCompressionRatio));

        // Print results
        results.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    // Dummy compress method for illustration purposes
    private long compress(File file) {
        // Implement the actual compression logic here
        return file.length() / 2; // Example: Return half the original size
    }
}