import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * OWO optimizēts kompresijas algoritms
 * Izmanto: LZ77 -> Huffman ar kompaktāku serializāciju
 * Optimizēts maziem failiem ar minimālu overhead
 */
public class OWOCompressor {
    
    private static final String MAGIC_HEADER = "OWO2";
    private static final int MIN_FILE_SIZE_TO_COMPRESS = 256; // Necompresē failus mazākus par 256 baitiem
    private static final int COMPRESSION_THRESHOLD = 95; // Compression ratio % - ja > 95%, glabā nekompresētu
    
    /**
     * Kompresē failu
     * @param inputPath Ievades faila ceļš
     * @param outputPath Izvades faila ceļš (.owo)
     * @throws IOException Ja rodas I/O kļūda
     */
    public static void compress(String inputPath, String outputPath) throws IOException {
        // Nolasīt failu
        String content = new String(Files.readAllBytes(Paths.get(inputPath)), "UTF-8");
        
        // Kompresēt
        byte[] compressed = compressString(content);
        
        // Saglabāt .owo failā
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputPath)))) {
            out.writeBytes(MAGIC_HEADER);
            out.write(compressed);
        }
    }
    
    /**
     * Kompresē tekstu ar optimizētu pieeja
     * @param input Ievades teksts
     * @return Kompresēti baiti
     */
    public static byte[] compressString(String input) throws IOException {
        byte[] originalBytes = input.getBytes("UTF-8");
        
        // Pārāk maziem failiem - glabāt nekompresētu
        if (originalBytes.length < MIN_FILE_SIZE_TO_COMPRESS) {
            return serializeUncompressed(originalBytes);
        }
        
        // LZ77 + Huffman kompresija
        List<LZ77Compression.LZ77Token> lz77Result = LZ77Compression.compress(input);
        int[] lz77Array = lz77ToArray(lz77Result);
        HuffmanCoding.HuffmanResult huffmanResult = HuffmanCoding.encode(lz77Array);
        byte[] compressedData = serializeCompressedDataOptimized(huffmanResult);
        
        // Pārbaudīt, vai kompresija ir vērta
        int totalSize = compressedData.length;
        double ratio = (double) totalSize / originalBytes.length * 100;
        
        // Ja kompresija nepalīdz (overhead pārāk liels), glabāt nekompresētu
        if (ratio >= COMPRESSION_THRESHOLD) {
            return serializeUncompressed(originalBytes);
        }
        
        return compressedData;
    }
    
    /**
     * Serializē nekompresētos datus ar minimālu overhead
     */
    private static byte[] serializeUncompressed(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        
        out.writeByte(0); // Marker: nekompresēts
        out.writeInt(data.length);
        out.write(data);
        out.flush();
        
        return baos.toByteArray();
    }
    
    /**
     * Optimizēta Huffman datu serializācija ar kompaktāku formātu
     */
    private static byte[] serializeCompressedDataOptimized(
            HuffmanCoding.HuffmanResult huffmanResult) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        
        // Marker: kompresēts (1 = Huffman + LZ77)
        out.writeByte(1);
        
        // === Kompaktā Huffman kodeksa serializācija ===
        Map<Integer, String> codebook = huffmanResult.codebook;
        
        // Kodeksa izmērs (variable length encoding)
        writeVariableLengthInt(out, codebook.size());
        
        // Katrs kodeksa ieraksts: tikai key un code (bez pārmērīga overhead)
        for (Map.Entry<Integer, String> entry : codebook.entrySet()) {
            int key = entry.getKey();
            String code = entry.getValue();
            
            // Key - variable length encoding (ekonomē vietu maziem skaitļiem)
            writeVariableLengthInt(out, key);
            
            // Code garums un biti (kompakti)
            writeVariableLengthInt(out, code.length());
            byte[] codeBits = codeToBytes(code);
            out.write(codeBits);
        }
        
        // === Huffman datu serializācija ===
        // Datu garums (variable length encoding)
        writeVariableLengthInt(out, huffmanResult.encodedData.length);
        out.write(huffmanResult.encodedData);
        
        out.flush();
        return baos.toByteArray();
    }
    
    /**
     * Variable Length Integer encoding - ietaupa vietu maziem skaitļiem
     */
    private static void writeVariableLengthInt(DataOutputStream out, int value) throws IOException {
        if (value < 128) {
            out.writeByte(value);
        } else if (value < 16384) {
            out.writeByte((value >> 8) | 0x80);
            out.writeByte(value & 0xFF);
        } else {
            out.writeByte(0xFF);
            out.writeInt(value);
        }
    }
    
    /**
     * Variable Length Integer dekodēšana
     */
    private static int readVariableLengthInt(DataInputStream in) throws IOException {
        int first = in.readByte() & 0xFF;
        if (first < 128) {
            return first;
        } else if (first == 0xFF) {
            return in.readInt();
        } else {
            int second = in.readByte() & 0xFF;
            return ((first & 0x7F) << 8) | second;
        }
    }
    
    /**
     * Dekompresē failu
     * @param inputPath Ievades faila ceļš (.owo)
     * @param outputPath Izvades faila ceļš
     * @throws IOException Ja rodas I/O kļūda
     */
    public static void decompress(String inputPath, String outputPath) throws IOException {
        // Nolasīt .owo failu
        byte[] compressed;
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(inputPath)))) {
            // Pārbaudīt header
            byte[] header = new byte[4];
            in.readFully(header);
            String headerStr = new String(header, "UTF-8");
            if (!headerStr.equals(MAGIC_HEADER)) {
                throw new IOException("Nederīgs OWO faila formāts");
            }
            
            // Nolasīt atlikušos datus
            compressed = in.readAllBytes();
        }
        
        // Dekompresēt
        String decompressed = decompressBytes(compressed);
        
        // Saglabāt
        Files.write(Paths.get(outputPath), decompressed.getBytes("UTF-8"));
    }
    
    /**
     * Dekompresē baitus
     * @param compressed Kompresēti baiti
     * @return Dekompresēts teksts
     */
    public static String decompressBytes(byte[] compressed) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(compressed));
        
        byte marker = in.readByte();
        
        // Nekompresēts fails
        if (marker == 0) {
            int length = in.readInt();
            byte[] data = new byte[length];
            in.readFully(data);
            return new String(data, "UTF-8");
        }
        
        // Kompresēts fails (Huffman + LZ77)
        if (marker == 1) {
            // Nolasīt kodeksu
            int codebookSize = readVariableLengthInt(in);
            Map<Integer, String> codebook = new HashMap<>();
            
            for (int i = 0; i < codebookSize; i++) {
                int key = readVariableLengthInt(in);
                int codeLength = readVariableLengthInt(in);
                
                byte[] codeBytes = new byte[(codeLength + 7) / 8];
                in.readFully(codeBytes);
                String code = bytesToCode(codeBytes, codeLength);
                
                codebook.put(key, code);
            }
            
            // Nolasīt Huffman datus
            int huffmanDataLength = readVariableLengthInt(in);
            byte[] huffmanData = new byte[huffmanDataLength];
            in.readFully(huffmanData);
            
            // Dekodē Huffman
            int[] huffmanDecoded = HuffmanCoding.decode(huffmanData, codebook);
            
            // Konvertē no LZ77 array uz tokenus un dekompresē
            List<LZ77Compression.LZ77Token> lz77Tokens = arrayToLZ77(huffmanDecoded);
            return LZ77Compression.decompress(lz77Tokens);
        }
        
        throw new IOException("Nezināms kompresijas marker: " + marker);
    }
    
    /**
     * Konvertē LZ77 tokenus uz masīvu
     */
    private static int[] lz77ToArray(List<LZ77Compression.LZ77Token> tokens) {
        List<Integer> result = new ArrayList<>();
        for (LZ77Compression.LZ77Token token : tokens) {
            result.add(token.offset);
            result.add(token.length);
            result.add((int) token.nextChar);
        }
        return result.stream().mapToInt(i -> i).toArray();
    }
    
    /**
     * Konvertē masīvu uz LZ77 tokenus
     */
    private static List<LZ77Compression.LZ77Token> arrayToLZ77(int[] array) {
        List<LZ77Compression.LZ77Token> result = new ArrayList<>();
        for (int i = 0; i < array.length; i += 3) {
            if (i + 2 < array.length) {
                result.add(new LZ77Compression.LZ77Token(
                    array[i],
                    array[i + 1],
                    (char) array[i + 2]
                ));
            }
        }
        return result;
    }
    
    /**
     * Konvertē bitus uz baitiem
     */
    private static byte[] codeToBytes(String bits) {
        int byteCount = (bits.length() + 7) / 8;
        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') {
                bytes[i / 8] |= (1 << (7 - (i % 8)));
            }
        }
        return bytes;
    }
    
    /**
     * Konvertē baitus uz bitiem
     */
    private static String bytesToCode(byte[] bytes, int bitLength) {
        StringBuilder bits = new StringBuilder();
        for (int i = 0; i < bitLength; i++) {
            int byteIndex = i / 8;
            int bitIndex = 7 - (i % 8);
            if ((bytes[byteIndex] & (1 << bitIndex)) != 0) {
                bits.append('1');
            } else {
                bits.append('0');
            }
        }
        return bits.toString();
    }
}
