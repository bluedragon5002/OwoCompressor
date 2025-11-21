import java.util.ArrayList;
import java.util.List;

/**
 * Run-Length Encoding (RLE)
 * Kompresē secības ar vienādiem simboliem
 */
public class RunLengthEncoding {
    
    /**
     * Piemēro RLE kodēšanu
     * @param data Ievades dati
     * @return Kodēti dati kā RLE pāri (vērtība, garums)
     */
    public static List<RLEPair> encode(int[] data) {
        if (data == null || data.length == 0) {
            return new ArrayList<>();
        }
        
        List<RLEPair> result = new ArrayList<>();
        int currentValue = data[0];
        int count = 1;
        
        for (int i = 1; i < data.length; i++) {
            if (data[i] == currentValue && count < 255) {
                count++;
            } else {
                result.add(new RLEPair(currentValue, count));
                currentValue = data[i];
                count = 1;
            }
        }
        
        result.add(new RLEPair(currentValue, count));
        return result;
    }
    
    /**
     * Atgriež RLE kodēšanu
     * @param encoded Kodēti dati
     * @return Oriģinālie dati
     */
    public static int[] decode(List<RLEPair> encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return new int[0];
        }
        
        List<Integer> result = new ArrayList<>();
        
        for (RLEPair pair : encoded) {
            for (int i = 0; i < pair.length; i++) {
                result.add(pair.value);
            }
        }
        
        return result.stream().mapToInt(i -> i).toArray();
    }
    
    /**
     * RLE pāra klase
     */
    public static class RLEPair {
        public final int value;
        public final int length;
        
        public RLEPair(int value, int length) {
            this.value = value;
            this.length = length;
        }
    }
}

