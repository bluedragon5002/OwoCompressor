import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * OWO kompresijas algoritms
 * Izmanto: BWT -> MTF -> RLE -> Huffman
 * Optimizēts tekstam un HTML failiem
 */
public class OWOCompressor {
    
    private static final String MAGIC_HEADER = "OWO1";
    
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
            out.writeInt(compressed.length);
            out.write(compressed);
        }
    }
    
    /**
     * Kompresē tekstu
     * @param input Ievades teksts
     * @return Kompresēti baiti
     */
    public static byte[] compressString(String input) throws IOException {
        // Adaptīva pieeja: izmēģinām abas metodes un izvēlamies labāko
        // Vienmēr pārbaudām, vai kompresija patiešām samazina izmēru
        
        // 1. Tikai Huffman
        int[] chars = new int[input.length()];
        for (int i = 0; i < input.length(); i++) {
            chars[i] = (int) input.charAt(i);
        }
        HuffmanCoding.HuffmanResult huffmanResult = HuffmanCoding.encode(chars);
        byte[] simpleResult = serializeCompressedDataSimple(huffmanResult, true);
        
        // 2. LZ77 + Huffman
        List<LZ77Compression.LZ77Token> lz77Result = LZ77Compression.compress(input);
        int[] lz77Array = lz77ToArray(lz77Result);
        HuffmanCoding.HuffmanResult lz77Huffman = HuffmanCoding.encode(lz77Array);
        byte[] lz77ResultBytes = serializeCompressedDataSimple(lz77Huffman, false);
        
        // 3. Izvēlamies mazāko
        byte[] bestResult = (simpleResult.length < lz77ResultBytes.length) ? simpleResult : lz77ResultBytes;
        
        // 4. Ja kompresija nepalīdz, saglabājam nekompresētu (ar markeri)
        // Bet arī pievienojam header overhead (5 baiti), tāpēc pārbaudām ar to
        int originalSize = input.getBytes("UTF-8").length;
        int uncompressedSize = 1 + 1 + 4 + originalSize; // boolean + byte + int + data
        
        if (bestResult.length >= uncompressedSize) {
            // Nekompresēts - saglabājam ar īpašu markeri
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeBoolean(true); // isSimple
            out.writeByte(0); // codebook size = 0 (marker nekompresētam)
            byte[] originalBytes = input.getBytes("UTF-8");
            out.writeInt(originalBytes.length);
            out.write(originalBytes);
            out.flush();
            return baos.toByteArray();
        }
        
        return bestResult;
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
            
            int length = in.readInt();
            compressed = new byte[length];
            in.readFully(compressed);
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
        // Deserializēt metadatus
        CompressedData data = deserializeCompressedData(compressed);
        
        // Atgriezt Huffman kodēšanu
        int[] huffmanDecoded = HuffmanCoding.decode(
            data.huffmanData, 
            data.huffmanCodebook
        );
        
        // Īpašs gadījums: nekompresēts fails
        if (data.huffmanCodebook.isEmpty() && data.isSimple && data.huffmanData.length > 0) {
            return new String(data.huffmanData, "UTF-8");
        }
        
        // Atgriezt atkarībā no kompresijas veida
        if (data.isSimple) {
            // Vienkārša kompresija: tieši no Huffman
            StringBuilder result = new StringBuilder();
            for (int value : huffmanDecoded) {
                if (value >= 0 && value <= 65535) {
                    result.append((char) value);
                }
            }
            return result.toString();
        } else {
            // LZ77 kompresija
            List<LZ77Compression.LZ77Token> lz77Tokens = arrayToLZ77(huffmanDecoded);
            return LZ77Compression.decompress(lz77Tokens);
        }
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
     * Konvertē RLE sarakstu uz masīvu
     */
    private static int[] rleToArray(List<RunLengthEncoding.RLEPair> rle) {
        List<Integer> result = new ArrayList<>();
        for (RunLengthEncoding.RLEPair pair : rle) {
            result.add(pair.value);
            result.add(pair.length);
        }
        return result.stream().mapToInt(i -> i).toArray();
    }
    
    /**
     * Konvertē masīvu uz RLE sarakstu
     */
    private static List<RunLengthEncoding.RLEPair> arrayToRLE(int[] array) {
        List<RunLengthEncoding.RLEPair> result = new ArrayList<>();
        for (int i = 0; i < array.length; i += 2) {
            if (i + 1 < array.length) {
                result.add(new RunLengthEncoding.RLEPair(
                    array[i], 
                    array[i + 1]
                ));
            }
        }
        return result;
    }
    
    /**
     * Serializē kompresētos datus
     */
    private static byte[] serializeCompressedDataSimple(
            HuffmanCoding.HuffmanResult huffmanResult, boolean isSimple) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        
        // Saglabāt kompresijas veidu
        out.writeBoolean(isSimple);
        
        // Saglabāt Huffman kodeksu (optimizēti - kompaktāka serializācija)
        int codebookSize = huffmanResult.codebook.size();
        if (codebookSize < 256) {
            out.writeByte(codebookSize);
        } else {
            out.writeByte(255);
            out.writeShort(codebookSize);
        }
        
        for (Map.Entry<Integer, String> entry : huffmanResult.codebook.entrySet()) {
            int key = entry.getKey();
            String code = entry.getValue();
            
            // Kompaktāka serializācija: key + code garums + code
            if (key < 256) {
                out.writeByte(key);
            } else if (key < 65536) {
                out.writeByte(255);
                out.writeShort(key);
            } else {
                out.writeByte(254);
                out.writeInt(key);
            }
            
            // Code garums (maksimums 255 biti = 32 baiti)
            out.writeByte(Math.min(255, code.length()));
            if (code.length() < 255) {
                // Konvertēt bitus uz baitiem
                byte[] codeBytes = codeToBytes(code);
                out.write(codeBytes);
            } else {
                // Garš kods - saglabāt kā string
                out.writeShort(code.length());
                out.writeBytes(code);
            }
        }
        
        // Saglabāt Huffman datus
        if (huffmanResult.encodedData.length < 65536) {
            out.writeShort(huffmanResult.encodedData.length);
        } else {
            out.writeShort(65535);
            out.writeInt(huffmanResult.encodedData.length);
        }
        out.write(huffmanResult.encodedData);
        
        out.flush();
        return baos.toByteArray();
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
     * Deserializē kompresētos datus
     */
    private static CompressedData deserializeCompressedData(byte[] data) 
            throws IOException {
        
        DataInputStream in = new DataInputStream(
            new ByteArrayInputStream(data));
        
        // Nolasīt kompresijas veidu
        boolean isSimple = in.readBoolean();
        
        // Nolasīt Huffman kodeksu (optimizēti)
        int codebookSize = in.readByte() & 0xFF;
        if (codebookSize == 255) {
            codebookSize = in.readShort() & 0xFFFF;
        }
        
        // Īpašs gadījums: nekompresēts fails (codebookSize = 0)
        if (codebookSize == 0) {
            int dataLength = in.readInt();
            byte[] uncompressedData = new byte[dataLength];
            in.readFully(uncompressedData);
            return new CompressedData(uncompressedData, new HashMap<>(), true); // isSimple = true, bet tukšs codebook
        }
        
        Map<Integer, String> codebook = new HashMap<>();
        for (int i = 0; i < codebookSize; i++) {
            int key;
            int firstByte = in.readByte() & 0xFF;
            if (firstByte == 255) {
                key = in.readShort() & 0xFFFF;
            } else if (firstByte == 254) {
                key = in.readInt();
            } else {
                key = firstByte;
            }
            
            int codeLength = in.readByte() & 0xFF;
            String code;
            if (codeLength < 255) {
                byte[] codeBytes = new byte[(codeLength + 7) / 8];
                in.readFully(codeBytes);
                code = bytesToCode(codeBytes, codeLength);
            } else {
                int actualLength = in.readShort() & 0xFFFF;
                byte[] codeBytes = new byte[actualLength];
                in.readFully(codeBytes);
                code = new String(codeBytes, "UTF-8");
            }
            codebook.put(key, code);
        }
        
        // Nolasīt Huffman datus
        int huffmanDataLength = in.readShort() & 0xFFFF;
        if (huffmanDataLength == 65535) {
            huffmanDataLength = in.readInt();
        }
        byte[] huffmanData = new byte[huffmanDataLength];
        in.readFully(huffmanData);
        
        return new CompressedData(huffmanData, codebook, isSimple);
    }
    
    /**
     * Kompresēto datu konteiners
     */
    private static class CompressedData {
        final byte[] huffmanData;
        final Map<Integer, String> huffmanCodebook;
        final boolean isSimple;
        
        CompressedData(byte[] huffmanData, 
                      Map<Integer, String> huffmanCodebook,
                      boolean isSimple) {
            this.huffmanData = huffmanData;
            this.huffmanCodebook = huffmanCodebook;
            this.isSimple = isSimple;
        }
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

