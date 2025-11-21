// Optimized compression logic in OWOCompressor.java

public class OWOCompressor {
    
    // Implementing variable-length integer encoding
    // Other compression logic...
    
    public void compress(File input) {
        if (input.length() < 256) {
            // Skip compression for small files
            return;
        }
        
        // Smart compression ratio checking
        double compressionRatio = calculateCompressionRatio(input);
        if (compressionRatio > 0.95) {
            // Store uncompressed if ratio is too high
            return;
        }
        
        // ... continue with compression logic, making use of streamlined markers and reduced overhead...
    }
    
    // Rest of the class code including serialization logic
}