import java.util.ArrayList;
import java.util.List;

/**
 * Move-to-Front (MTF) transformācija
 * Pārveido BWT izvadi tā, lai būtu vairāk mazu skaitļu (0, 1, 2...)
 */
public class MoveToFront {
    
    private static final int ALPHABET_SIZE = 256;
    
    /**
     * Piemēro MTF transformāciju
     * @param input Ievades teksts
     * @return Transformētais teksts kā skaitļu masīvs
     */
    public static int[] transform(String input) {
        if (input == null || input.isEmpty()) {
            return new int[0];
        }
        
        // Izveido alfabētu (paplašināts līdz 65536, lai atbalstītu visus Unicode simbolus)
        List<Integer> alphabet = new ArrayList<>();
        for (int i = 0; i < 65536; i++) {
            alphabet.add(i);
        }
        
        int[] result = new int[input.length()];
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int charValue = (int) c;
            int index = alphabet.indexOf(charValue);
            
            if (index == -1) {
                // Ja simbols nav alfabētā, pievieno to
                alphabet.add(charValue);
                index = alphabet.size() - 1;
            }
            
            result[i] = index;
            
            // Pārvieto simbolu uz priekšu
            alphabet.remove(index);
            alphabet.add(0, charValue);
        }
        
        return result;
    }
    
    /**
     * Piemēro MTF transformāciju masīvam
     * @param input Ievades masīvs
     * @return Transformētais masīvs
     */
    public static int[] transformArray(int[] input) {
        if (input == null || input.length == 0) {
            return new int[0];
        }
        
        // Izveido alfabētu (paplašināts līdz 65536)
        List<Integer> alphabet = new ArrayList<>();
        for (int i = 0; i < 65536; i++) {
            alphabet.add(i);
        }
        
        int[] result = new int[input.length];
        
        for (int i = 0; i < input.length; i++) {
            int value = input[i];
            int index = alphabet.indexOf(value);
            
            if (index == -1) {
                alphabet.add(value);
                index = alphabet.size() - 1;
            }
            
            result[i] = index;
            
            // Pārvieto simbolu uz priekšu
            alphabet.remove(index);
            alphabet.add(0, value);
        }
        
        return result;
    }
    
    /**
     * Atgriež MTF transformāciju masīvam
     * @param encoded Kodētais masīvs
     * @return Oriģinālais masīvs
     */
    public static int[] inverseTransformArray(int[] encoded) {
        if (encoded == null || encoded.length == 0) {
            return new int[0];
        }
        
        // Izveido alfabētu (paplašināts līdz 65536)
        List<Integer> alphabet = new ArrayList<>();
        for (int i = 0; i < 65536; i++) {
            alphabet.add(i);
        }
        
        int[] result = new int[encoded.length];
        
        for (int i = 0; i < encoded.length; i++) {
            int index = encoded[i];
            if (index < 0 || index >= alphabet.size()) {
                throw new IllegalArgumentException("Nederīgs indekss: " + index);
            }
            
            int value = alphabet.get(index);
            result[i] = value;
            
            // Pārvieto simbolu uz priekšu
            alphabet.remove(index);
            alphabet.add(0, value);
        }
        
        return result;
    }
    
    /**
     * Atgriež MTF transformāciju
     * @param encoded Kodētais masīvs
     * @return Oriģinālais teksts
     */
    public static String inverseTransform(int[] encoded) {
        if (encoded == null || encoded.length == 0) {
            return "";
        }
        
        // Izveido alfabētu (paplašināts līdz 65536)
        List<Integer> alphabet = new ArrayList<>();
        for (int i = 0; i < 65536; i++) {
            alphabet.add(i);
        }
        
        StringBuilder result = new StringBuilder();
        
        for (int index : encoded) {
            if (index < 0 || index >= alphabet.size()) {
                throw new IllegalArgumentException("Nederīgs indekss: " + index);
            }
            
            int symbol = alphabet.get(index);
            result.append((char) symbol);
            
            // Pārvieto simbolu uz priekšu
            alphabet.remove(index);
            alphabet.add(0, symbol);
        }
        
        return result.toString();
    }
}

