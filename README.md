# OWO Kompresijas Algoritms

Augstas veiktspējas kompresijas algoritms Java programmēšanas valodā, optimizēts tekstam un HTML failiem.

## Funkcijas

- **Bez zaudējumiem (lossless)** - pilnīga datu atjaunošana
- **Optimizēts tekstam** - īpaši efektīvs HTML un teksta failiem
- **Pielāgots failu formāts** - kompresētie faili izmanto `.owo` paplašinājumu
- **Nav ārējās bibliotēkas** - viss implementēts no nulles

## Algoritma struktūra

Kompresijas process sastāv no četriem posmiem:

1. **LZ77 Compression** - dictionary-based kompresija ar sliding window, efektīva teksta atkārtojumiem
2. **Move-to-Front (MTF)** - pārveido datus tā, lai būtu vairāk mazu skaitļu (0, 1, 2...)
3. **Run-Length Encoding (RLE)** - kompresē secības ar vienādiem simboliem
4. **Huffman Coding** - frekvenču balstīta kodēšana ar optimāliem koda garumiem

## Izmantošana

### Kompresēt failu

```java
OWOCompressor.compress("input.html", "output.owo");
```

### Dekompresēt failu

```java
OWOCompressor.decompress("output.owo", "restored.html");
```

### Piemērs

Skatīt `OWOCompressorTest.java` pilnu izmantošanas piemēru.

## Kompilācija un palaišana

```bash
javac *.java
java OWOCompressorTest
```

## Failu struktūra

- `LZ77Compression.java` - LZ77 kompresijas implementācija
- `MoveToFront.java` - MTF transformācijas implementācija
- `RunLengthEncoding.java` - RLE kodēšanas implementācija
- `HuffmanCoding.java` - Huffman kodēšanas implementācija
- `OWOCompressor.java` - galvenā kompresijas klase
- `OWOCompressorTest.java` - testa piemērs

## Tehniskās detaļas

### LZ77 Compression
- Dictionary-based kompresija ar sliding window
- Meklē garāko atbilstību iepriekš redzētajā tekstā
- Aizstāj atkārtojumus ar atsaucēm (offset, length, nextChar)
- Īpaši efektīva teksta failiem ar daudz atkārtojumiem

### MTF (Move-to-Front)
- Katru simbolu aizstāj ar tā pozīciju alfabētā
- Pēc katras transformācijas simbols tiek pārvietots uz priekšu
- Rezultātā iegūstam vairāk mazu skaitļu, kas ir labāk kompresējami

### RLE (Run-Length Encoding)
- Kompresē secības ar vienādiem simboliem
- Katru secību aizstāj ar pāri (vērtība, garums)

### Huffman Coding
- Izveido optimālu bināro kodu katrai unikālai vērtībai
- Biežāk sastopamām vērtībām piešķir īsākus kodus
- Minimizē kopējo bita skaitu

## Veiktspēja

Algoritms ir īpaši efektīvs:
- Teksta failiem ar daudz atkārtojumiem
- HTML failiem ar strukturāliem elementiem
- Failiem ar lielu redundanci

Kompresijas koeficients parasti ir līdzīgs vai labāks nekā ZIP algoritmam teksta failiem.

