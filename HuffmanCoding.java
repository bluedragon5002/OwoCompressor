import java.util.*;

/**
 * Huffman kodēšana
 * Frekvenču balstīta kodēšana ar optimāliem koda garumiem
 */
public class HuffmanCoding {
    
    /**
     * Huffman mezgla klase
     */
    private static class Node implements Comparable<Node> {
        Integer symbol;
        int frequency;
        Node left;
        Node right;
        
        Node(Integer symbol, int frequency) {
            this.symbol = symbol;
            this.frequency = frequency;
        }
        
        Node(Node left, Node right) {
            this.left = left;
            this.right = right;
            this.frequency = left.frequency + right.frequency;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.frequency, other.frequency);
        }
        
        boolean isLeaf() {
            return left == null && right == null;
        }
    }
    
    /**
     * Kodē datus ar Huffman kodēšanu
     * @param data Ievades dati
     * @return Kodēti dati un kodeks
     */
    public static HuffmanResult encode(int[] data) {
        if (data == null || data.length == 0) {
            return new HuffmanResult(new byte[0], new HashMap<>());
        }
        
        // Aprēķina frekvences
        Map<Integer, Integer> frequencies = new HashMap<>();
        for (int value : data) {
            frequencies.put(value, frequencies.getOrDefault(value, 0) + 1);
        }
        
        // Izveido Huffman koku
        Node root = buildHuffmanTree(frequencies);
        
        // Izveido kodeksu
        Map<Integer, String> codebook = new HashMap<>();
        buildCodebook(root, "", codebook);
        
        // Kodē datus
        StringBuilder encodedBits = new StringBuilder();
        for (int value : data) {
            encodedBits.append(codebook.get(value));
        }
        
        // Pārveido bitus uz baitu masīvu
        byte[] encodedBytes = bitsToBytes(encodedBits.toString());
        
        return new HuffmanResult(encodedBytes, codebook);
    }
    
    /**
     * Atgriež Huffman kodēšanu
     * @param encodedBytes Kodēti baiti
     * @param codebook Kodekss
     * @return Oriģinālie dati
     */
    public static int[] decode(byte[] encodedBytes, Map<Integer, String> codebook) {
        if (encodedBytes == null || encodedBytes.length == 0) {
            return new int[0];
        }
        
        // Pārveido baitus uz bitiem
        String bits = bytesToBits(encodedBytes);
        
        // Izveido reverso kodeksu
        Map<String, Integer> reverseCodebook = new HashMap<>();
        for (Map.Entry<Integer, String> entry : codebook.entrySet()) {
            reverseCodebook.put(entry.getValue(), entry.getKey());
        }
        
        // Dekodē
        List<Integer> result = new ArrayList<>();
        StringBuilder currentCode = new StringBuilder();
        
        for (char bit : bits.toCharArray()) {
            currentCode.append(bit);
            if (reverseCodebook.containsKey(currentCode.toString())) {
                result.add(reverseCodebook.get(currentCode.toString()));
                currentCode.setLength(0);
            }
        }
        
        return result.stream().mapToInt(i -> i).toArray();
    }
    
    /**
     * Izveido Huffman koku
     */
    private static Node buildHuffmanTree(Map<Integer, Integer> frequencies) {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        
        for (Map.Entry<Integer, Integer> entry : frequencies.entrySet()) {
            queue.offer(new Node(entry.getKey(), entry.getValue()));
        }
        
        // Ja tikai viens simbols
        if (queue.size() == 1) {
            Node node = queue.poll();
            return new Node(node, new Node(null, 0));
        }
        
        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            queue.offer(new Node(left, right));
        }
        
        return queue.poll();
    }
    
    /**
     * Izveido kodeksu no koka
     */
    private static void buildCodebook(Node node, String code, Map<Integer, String> codebook) {
        if (node.isLeaf()) {
            if (node.symbol != null) {
                codebook.put(node.symbol, code.isEmpty() ? "0" : code);
            }
        } else {
            buildCodebook(node.left, code + "0", codebook);
            buildCodebook(node.right, code + "1", codebook);
        }
    }
    
    /**
     * Pārveido bitus uz baitiem
     */
    private static byte[] bitsToBytes(String bits) {
        int byteCount = (bits.length() + 7) / 8;
        byte[] bytes = new byte[byteCount + 1]; // +1 lai saglabātu garumu
        bytes[0] = (byte) (bits.length() % 8); // Pēdējā baita bitu skaits
        
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') {
                bytes[i / 8 + 1] |= (1 << (7 - (i % 8)));
            }
        }
        
        return bytes;
    }
    
    /**
     * Pārveido baitus uz bitiem
     */
    private static String bytesToBits(byte[] bytes) {
        if (bytes.length < 2) {
            return "";
        }
        
        int lastByteBits = bytes[0] & 0xFF;
        StringBuilder bits = new StringBuilder();
        
        for (int i = 1; i < bytes.length; i++) {
            int bitsToRead = (i == bytes.length - 1) ? lastByteBits : 8;
            for (int j = 0; j < bitsToRead; j++) {
                if ((bytes[i] & (1 << (7 - j))) != 0) {
                    bits.append('1');
                } else {
                    bits.append('0');
                }
            }
        }
        
        return bits.toString();
    }
    
    /**
     * Rezultāta klase Huffman kodēšanai
     */
    public static class HuffmanResult {
        public final byte[] encodedData;
        public final Map<Integer, String> codebook;
        
        public HuffmanResult(byte[] encodedData, Map<Integer, String> codebook) {
            this.encodedData = encodedData;
            this.codebook = codebook;
        }
    }
}

