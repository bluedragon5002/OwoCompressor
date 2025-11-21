import java.util.*;

/**
 * LZ77 style kompresija ar sliding window
 * Labāk darbojas tekstam nekā BWT un ir vienkāršāka
 */
public class LZ77Compression {
    
    private static final int WINDOW_SIZE = 8192; // Lielāks logs = labāka kompresija
    private static final int LOOKAHEAD_SIZE = 258; // Lielāks lookahead = labāka kompresija
    
    /**
     * Kompresē tekstu ar LZ77
     * @param input Ievades teksts
     * @return Kompresēti dati kā (offset, length, nextChar) tripleti
     */
    public static List<LZ77Token> compress(String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<LZ77Token> result = new ArrayList<>();
        int pos = 0;
        
        while (pos < input.length()) {
            int matchLength = 0;
            int matchOffset = 0;
            
            // Meklējam garāko atbilstību sliding window
            int searchStart = Math.max(0, pos - WINDOW_SIZE);
            int maxLength = Math.min(LOOKAHEAD_SIZE, input.length() - pos);
            
            for (int i = searchStart; i < pos; i++) {
                int len = 0;
                while (len < maxLength && 
                       pos + len < input.length() && 
                       i + len < pos && 
                       input.charAt(i + len) == input.charAt(pos + len)) {
                    len++;
                }
                
                if (len > matchLength) {
                    matchLength = len;
                    matchOffset = pos - i;
                }
            }
            
            // Ja atradām atbilstību ar garumu >= 3, izmantojam to
            // Minimum 3, jo (offset, length, char) = 3 ints, kas ir 12 baiti, bet 3 chars = 6 baiti (UTF-8)
            if (matchLength >= 3) {
                char nextChar = (pos + matchLength < input.length()) ? 
                    input.charAt(pos + matchLength) : '\0';
                result.add(new LZ77Token(matchOffset, matchLength, nextChar));
                pos += matchLength + 1;
            } else {
                // Citādi izvadām tikai nākamo rakstzīmi
                result.add(new LZ77Token(0, 0, input.charAt(pos)));
                pos++;
            }
        }
        
        return result;
    }
    
    /**
     * Dekompresē LZ77 datus
     * @param tokens Kompresēti tokeni
     * @return Dekompresēts teksts
     */
    public static String decompress(List<LZ77Token> tokens) {
        StringBuilder result = new StringBuilder();
        
        for (LZ77Token token : tokens) {
            if (token.offset == 0 && token.length == 0) {
                // Literāls
                if (token.nextChar != '\0') {
                    result.append(token.nextChar);
                }
            } else {
                // Atbilstība - kopējam no iepriekšējās pozīcijas
                int startPos = result.length() - token.offset;
                for (int i = 0; i < token.length; i++) {
                    result.append(result.charAt(startPos + i));
                }
                // Pievienojam nākamo rakstzīmi
                if (token.nextChar != '\0') {
                    result.append(token.nextChar);
                }
            }
        }
        
        return result.toString();
    }
    
    /**
     * LZ77 token klase
     */
    public static class LZ77Token {
        public final int offset;
        public final int length;
        public final char nextChar;
        
        public LZ77Token(int offset, int length, char nextChar) {
            this.offset = offset;
            this.length = length;
            this.nextChar = nextChar;
        }
    }
}

