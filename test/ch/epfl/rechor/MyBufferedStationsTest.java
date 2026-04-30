package ch.epfl.rechor;

import ch.epfl.rechor.timetable.mapped.BufferedStations;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MyBufferedStationsTest {
    // La structure utilisée dans BufferedStations est composée de :
    // - U16 pour le nom (2 octets)
    // - S32 pour la longitude (4 octets)
    // - S32 pour la latitude (4 octets)
    // soit un enregistrement de 10 octets.
    private static final int RECORD_SIZE = 10;

    @Test
    void testSingleStation() {
        // Création d'une table de chaînes.
        List<String> stringTable = Arrays.asList("StationA", "StationB", "StationC");

        // Création d'un ByteBuffer pour un enregistrement.
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE2);
        // Pour l'enregistrement, on écrit :
        // - Nom : index 1 (donc "StationB")
        // - Longitude : 1 000 000 (exemple)
        // - Latitude : -2 000 000 (exemple)
        buffer.putShort((short) 1);
        buffer.putInt(1000000);
        buffer.putInt(-2000000);
        buffer.flip();

        BufferedStations bs = new BufferedStations(stringTable, buffer);

        // Vérifie que le nombre d'éléments est correct.
        assertEquals(1, bs.size());
        // Vérifie que le nom retourné est celui attendu.
        assertEquals("StationB", bs.name(0));

        // Conversion : factor = Math.scalb(360, -32) = 360/2^32.
        double factor = Math.scalb(360, -32);
        // Vérifie longitude et latitude après conversion.
        assertEquals(1000000 * factor, bs.longitude(0), 1e-12);
        assertEquals(-2000000 * factor, bs.latitude(0), 1e-12);
    }

    @Test
    void testMultipleStations() {
        // Table de chaînes avec plusieurs entrées.
        List<String> stringTable = Arrays.asList("A", "B", "C", "D");
        // Création d'un buffer pour 3 enregistrements.
        ByteBuffer buffer = ByteBuffer.allocate(3 * RECORD_SIZE2);

        // Station 0 : nom index = 0, longitude = 0, latitude = 0.
        buffer.putShort((short) 0);
        buffer.putInt(0);
        buffer.putInt(0);
        // Station 1 : nom index = 2 ("C"), longitude = 123456, latitude = -654321.
        buffer.putShort((short) 2);
        buffer.putInt(123456);
        buffer.putInt(-654321);
        // Station 2 : nom index = 3 ("D"), longitude = -100000, latitude = 100000.
        buffer.putShort((short) 3);
        buffer.putInt(-100000);
        buffer.putInt(100000);
        buffer.flip();

        BufferedStations bs = new BufferedStations(stringTable, buffer);
        assertEquals(3, bs.size());

        // Vérification des noms.
        assertEquals("A", bs.name(0));
        assertEquals("C", bs.name(1));
        assertEquals("D", bs.name(2));

        double factor = Math.scalb(360, -32);
        // Station 0.
        assertEquals(0, bs.longitude(0), 1e-12);
        assertEquals(0, bs.latitude(0), 1e-12);
        // Station 1.
        assertEquals(123456 * factor, bs.longitude(1), 1e-12);
        assertEquals(-654321 * factor, bs.latitude(1), 1e-12);
        // Station 2.
        assertEquals(-100000 * factor, bs.longitude(2), 1e-12);
        assertEquals(100000 * factor, bs.latitude(2), 1e-12);
    }

    @Test
    void testIndexOutOfBounds() {
        List<String> stringTable = Arrays.asList("A", "B");
        ByteBuffer buffer = ByteBuffer.allocate(2 * RECORD_SIZE2);
        // Remplissage arbitraire pour 2 enregistrements.
        buffer.putShort((short) 0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putShort((short) 1);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.flip();

        BufferedStations bs = new BufferedStations(stringTable, buffer);
        assertEquals(2, bs.size());

        // Index négatif.
        assertThrows(IndexOutOfBoundsException.class, () -> bs.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bs.longitude(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bs.latitude(-1));
        // Index hors bornes.
        assertThrows(IndexOutOfBoundsException.class, () -> bs.name(2));
        assertThrows(IndexOutOfBoundsException.class, () -> bs.longitude(2));
        assertThrows(IndexOutOfBoundsException.class, () -> bs.latitude(2));
    }

    @Test
    void testScalbConversionAccuracy() {
        // Vérifie que la conversion par Math.scalb est correcte.
        double factor = Math.scalb(360, -32);
        double expectedFactor = 360.0 / 4294967296.0; // 2^32 = 4294967296.
        assertEquals(expectedFactor, factor, 1e-15);

        // Utilisation dans BufferedStations.
        List<String> stringTable = Arrays.asList("X");
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE2);
        // Utilise Integer.MAX_VALUE et Integer.MIN_VALUE pour longitude et latitude.
        buffer.putShort((short) 0);
        buffer.putInt(Integer.MAX_VALUE);
        buffer.putInt(Integer.MIN_VALUE);
        buffer.flip();

        BufferedStations bs = new BufferedStations(stringTable, buffer);
        double expectedLon = Integer.MAX_VALUE * factor;
        double expectedLat = Integer.MIN_VALUE * factor;
        assertEquals(expectedLon, bs.longitude(0), 1e-10);
        assertEquals(expectedLat, bs.latitude(0), 1e-10);
    }

    @Test
    void testBufferCapacityNotMultipleOfRecordSize() {
        // Vérifie qu'une exception est levée si la capacité du buffer n'est pas un multiple de la taille de la structure.
        List<String> stringTable = Arrays.asList("A");
        // Capacité non multiple de 10 (par exemple, 15 octets).
        ByteBuffer buffer = ByteBuffer.allocate(15);
        assertThrows(IllegalArgumentException.class, () -> new BufferedStations(stringTable, buffer));
    }
    // La structure attendue :
    // - U16 pour le nom (2 octets)
    // - S32 pour la longitude (4 octets)
    // - S32 pour la latitude (4 octets)
    // Donnant un enregistrement de 10 octets.
    private static final int RECORD_SIZE2 = 10;
    private static final double CONVERSION_FACTOR = Math.scalb(360, -32); // 360 / 2^32

    @Test
    void testExactOutputsForMultipleStations() {
        // On définit une table de chaînes avec des noms précis.
        List<String> stringTable = Arrays.asList("Alpha", "Beta", "Gamma");

        // Création d'un ByteBuffer pour 3 enregistrements (3 * 10 octets).
        ByteBuffer buffer = ByteBuffer.allocate(3 * RECORD_SIZE2);

        // Station 0 :
        // Nom : index 0 ("Alpha")
        // Longitude : 0
        // Latitude : 0
        buffer.putShort((short) 0);
        buffer.putInt(0);
        buffer.putInt(0);

        // Station 1 :
        // Nom : index 1 ("Beta")
        // Longitude : 200_000
        // Latitude : -300_000
        buffer.putShort((short) 1);
        buffer.putInt(200_000);
        buffer.putInt(-300_000);

        // Station 2 :
        // Nom : index 2 ("Gamma")
        // Longitude : -500_000
        // Latitude : 500_000
        buffer.putShort((short) 2);
        buffer.putInt(-500_000);
        buffer.putInt(500_000);

        buffer.flip();

        BufferedStations bs = new BufferedStations(stringTable, buffer);

        // Vérification de la taille
        assertEquals(3, bs.size());

        // Station 0
        assertEquals("Alpha", bs.name(0));
        assertEquals(0, bs.longitude(0), 1e-12);
        assertEquals(0, bs.latitude(0), 1e-12);

        // Station 1
        assertEquals("Beta", bs.name(1));
        double expectedLon1 = 200_000 * CONVERSION_FACTOR;
        double expectedLat1 = -300_000 * CONVERSION_FACTOR;
        assertEquals(expectedLon1, bs.longitude(1), 1e-12);
        assertEquals(expectedLat1, bs.latitude(1), 1e-12);

        // Station 2
        assertEquals("Gamma", bs.name(2));
        double expectedLon2 = -500_000 * CONVERSION_FACTOR;
        double expectedLat2 = 500_000 * CONVERSION_FACTOR;
        assertEquals(expectedLon2, bs.longitude(2), 1e-12);
        assertEquals(expectedLat2, bs.latitude(2), 1e-12);
    }

    @Test
    void testExactOutputWithExtremeCoordinateValues() {
        // On définit une table avec un seul nom.
        List<String> stringTable = Arrays.asList("ExtremeStation");

        // Création d'un ByteBuffer pour un enregistrement.
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE2);

        // On utilise les valeurs extrêmes pour S32.
        // Nom : index 0 ("ExtremeStation")
        // Longitude : Integer.MAX_VALUE
        // Latitude : Integer.MIN_VALUE
        buffer.putShort((short) 0);
        buffer.putInt(Integer.MAX_VALUE);
        buffer.putInt(Integer.MIN_VALUE);
        buffer.flip();

        BufferedStations bs = new BufferedStations(stringTable, buffer);

        assertEquals(1, bs.size());
        assertEquals("ExtremeStation", bs.name(0));
        double expectedLon = Integer.MAX_VALUE * CONVERSION_FACTOR;
        double expectedLat = Integer.MIN_VALUE * CONVERSION_FACTOR;
        assertEquals(expectedLon, bs.longitude(0), 1e-10);
        assertEquals(expectedLat, bs.latitude(0), 1e-10);
    }

    @Test
    void testExactOutputForZeroCoordinates() {
        // Table de chaînes avec plusieurs noms.
        List<String> stringTable = Arrays.asList("ZeroOne", "ZeroTwo");

        // Création d'un buffer pour 2 enregistrements.
        ByteBuffer buffer = ByteBuffer.allocate(2 * RECORD_SIZE2);

        // Pour les deux enregistrements, les coordonnées sont nulles.
        // Station 0 : nom index 0, longitude 0, latitude 0.
        buffer.putShort((short) 0);
        buffer.putInt(0);
        buffer.putInt(0);
        // Station 1 : nom index 1, longitude 0, latitude 0.
        buffer.putShort((short) 1);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.flip();

        BufferedStations bs = new BufferedStations(stringTable, buffer);

        // Vérifie que les noms et coordonnées sont exactement ceux attendus.
        assertEquals("ZeroOne", bs.name(0));
        assertEquals(0, bs.longitude(0), 1e-12);
        assertEquals(0, bs.latitude(0), 1e-12);
        assertEquals("ZeroTwo", bs.name(1));
        assertEquals(0, bs.longitude(1), 1e-12);
        assertEquals(0, bs.latitude(1), 1e-12);
    }

    @Test
    void testBufferedStations() {
        // Table des chaînes complète (§2.6)
        List<String> stringTable = List.of("1", "70", "Anet", "Ins", "Lausanne", "Losanna", "Palézieux");
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("00 04 04 b6 ca 14 21 14 1f a1 00 06 04 dc cc 12 21 18 da 03");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        // Gare 0: Lausanne
        assertEquals("Lausanne", stations.name(0));
        assertEquals(6.629092, stations.longitude(0), 1e-6);
        assertEquals(46.516792, stations.latitude(0), 1e-6);

        // Gare 1: Palézieux
        assertEquals("Palézieux", stations.name(1));
        assertEquals(6.837875, stations.longitude(1), 1e-6);
        assertEquals(46.542764, stations.latitude(1), 1e-6);

        // Taille
        assertEquals(2, stations.size());
    }

    private static final double SCALE = Math.scalb( 360,-32);
    //2.6.1 exemple
    public List a = List.of("1","70", "Anet", "Ins", "Lausanne", "Losanna", "Palézieux");
    HexFormat hexFormat = HexFormat.ofDelimiter(" ");
    byte[] bytes = hexFormat.parseHex("00 04 04 b6 ca 14 21 14 1f a1 00 06 04 dc cc 12 21 18 da 03");
    ByteBuffer b = ByteBuffer.wrap(bytes);
    BufferedStations z = new BufferedStations(a,b);

    @Test
    void name() {
        assertEquals("Lausanne", z.name(0));
    }
    @Test
    void name2() {
        assertEquals("Palézieux",z.name(1));
    }


    @Test
    void longitude() {
        assertEquals(0x04b6ca14 * SCALE,z.longitude(0));
    }

    @Test
    void longitude2() {
        assertEquals(0x04dccc12 * SCALE,z.longitude(1));
    }

    @Test
    void latitude() {
        assertEquals(0x21141fa1 * SCALE,z.latitude(0));
    }

    @Test
    void latitude2() {
        assertEquals(0x2118da03 * SCALE,z.latitude(1));
    }

    @Test
    void size() {
        assertEquals(2,z.size());
    }

    @Test
    void throwsOutOfBounds(){
        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> {
                    z.name(2);
                }),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> {
                    z.name(-1);
                }),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> {
                    z.name(10);
                }),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> {
                    z.longitude(2);
                }),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> {
                    z.longitude(-1);
                }),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> {
                    z.latitude(2);
                }),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> {
                    z.latitude(-1);
                }),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> {
                    z.name(1000);
                }),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> {
                    z.longitude(Integer.MAX_VALUE);
                })
        );
    }

    List table = List.of("1","12", "43", "70", "Anet", "Bulle", "Chavannes", "EPFL", "Flon", "Ins", "Lausanne",
            "Lille", "Losanna", "Ouchy" ,"Palézieux","Paris", "Renens", "Washington");

    /*
        0 | Anet | 47.043717, 7.306047 |
        1 | Bulle | 46.616667, 7.055278 |
        2 | Chavannes | 46.534700, 6.578340 |
        3 | EPFL | 46.518174, 6.567944 |
        4 | Flon | 46.519961, 6.631664 |
        5 | Ins | 47.005040, 7.108830 |
        6 | Lausanne | 46.519653, 6.632273 |
        7 | Lille | 50.629250, 3.057256 |
        8 | Ouchy | 46.506338, 6.630002 |
        9 | Palézieux | 46.553056, 6.830278 |
        10 | Paris | 48.856613, 2.352222 |
        11 | Renens | 46.536020, 6.583870 |
        12 | Washington | 38.895110, -77.036370 |

     */

    // Comme les longitudes et latitudes ne sont meme pas arrondies, ca sert a rien de les tester
    // Donc je mets la meme partout sauf les deux premiers
    ByteBuffer buffer = ByteBuffer.wrap(hexFormat.parseHex(
            "00 04 05 32 06 77 21 74 0c 1d" +
                    " 00 05 05 04 5F C9 21 26 4E 24"+
                    " 00 06 05 32 06 77 21 74 0c 1d" +
                    " 00 07 05 32 06 77 21 74 0c 1d"  +
                    " 00 08 05 32 06 77 21 74 0c 1d"+
                    " 00 09 05 32 06 77 21 74 0c 1d" +
                    " 00 0a 05 32 06 77 21 74 0c 1d" +
                    " 00 0b 05 32 06 77 21 74 0c 1d" +
                    " 00 0d 05 32 06 77 21 74 0c 1d" +
                    " 00 0e 05 32 06 77 21 74 0c 1d" +
                    " 00 0f 05 32 06 77 21 74 0c 1d" +
                    " 00 10 05 32 06 77 21 74 0c 1d" +
                    " 00 11 05 32 06 77 21 74 0c 1d" ));

    BufferedStations s = new BufferedStations(table,buffer);


    @Test
    void nameWorksWithBigBufferedStations(){
        assertAll(
                () -> assertEquals("Anet", s.name(0)),
                () -> assertEquals("Bulle", s.name(1)),
                () -> assertEquals("Chavannes", s.name(2)),
                () -> assertEquals("EPFL", s.name(3)),
                () -> assertEquals("Flon", s.name(4)),
                () -> assertEquals("Ins", s.name(5)),
                () -> assertEquals("Lausanne", s.name(6)),
                () -> assertEquals("Lille", s.name(7)),
                () -> assertEquals("Ouchy", s.name(8)),
                () -> assertEquals("Palézieux", s.name(9)),
                () -> assertEquals("Paris", s.name(10)),
                () -> assertEquals("Renens", s.name(11)),
                () -> assertEquals("Washington", s.name(12)));
    }

    @Test
    void longitudeAndLatitudeWorksWithBigBufferedStations(){
        assertAll(
                () -> assertEquals(0x05320677 * SCALE  , s.longitude(0)),
                () -> assertEquals(0x21740c1d * SCALE, s.latitude(0) ),
                () -> assertEquals(0x05045FC9 * SCALE, s.longitude(1)),
                () -> assertEquals(0x21264E24 * SCALE, s.latitude(1)),
                () -> assertEquals(0x05320677 * SCALE  , s.longitude(3)),
                () -> assertEquals(0x21740c1d * SCALE, s.latitude(3) ),
                () -> assertEquals(0x05320677 * SCALE  , s.longitude(5)),
                () -> assertEquals(0x21740c1d * SCALE, s.latitude(5) ),
                () -> assertEquals(0x05320677 * SCALE  , s.longitude(11)),
                () -> assertEquals(0x21740c1d * SCALE, s.latitude(11) ),
                () -> assertEquals(0x05320677 * SCALE  , s.longitude(7)),
                () -> assertEquals(0x21740c1d * SCALE, s.latitude(7)));
    }

    @Test
    void sizeWorksWithBigBufferedStations(){
        assertEquals(13, s.size());
    }


    //Essayons de mélanger la stringtable pour compliquer les index
    List otherTable = List.of("12","Palézieux" ,"Lille",  "70","Renens",  "Anet","Losanna",
            "Bulle","Washington",  "Chavannes", "EPFL","54",  "Flon",
            "Lausanne", "Ins", "12", "Ouchy" ,"43","4", "8", "10", "104", "58", "102",
            "Paris","1"  );

    ByteBuffer otherBuffer = ByteBuffer.wrap(hexFormat.parseHex(
            "00 05 05 32 06 77 21 74 0c 1d" +
                    " 00 07 05 04 5F C9 21 26 4E 24"+
                    " 00 09 05 32 06 77 21 74 0c 1d" +
                    " 00 0a 05 32 06 77 21 74 0c 1d"  +
                    " 00 0c 05 32 06 77 21 74 0c 1d"+
                    " 00 0e 05 32 06 77 21 74 0c 1d" +
                    " 00 0d 05 32 06 77 21 74 0c 1d" +
                    " 00 02 05 32 06 77 21 74 0c 1d" +
                    " 00 10 05 32 06 77 21 74 0c 1d" +
                    " 00 01 05 32 06 77 21 74 0c 1d" +
                    " 00 18 05 32 06 77 21 74 0c 1d" +
                    " 00 04 05 32 06 77 21 74 0c 1d" +
                    " 00 08 05 32 06 77 21 74 0c 1d"));

    BufferedStations s2 = new BufferedStations(otherTable,otherBuffer);

    @Test
    void nameWorksWithShuffledStringTable(){
        assertAll(
                () -> assertEquals("Anet", s2.name(0)),
                () -> assertEquals("Bulle", s2.name(1)),
                () -> assertEquals("Chavannes", s2.name(2)),
                () -> assertEquals("EPFL", s2.name(3)),
                () -> assertEquals("Flon", s2.name(4)),
                () -> assertEquals("Ins", s2.name(5)),
                () -> assertEquals("Lausanne", s2.name(6)),
                () -> assertEquals("Lille", s2.name(7)),
                () -> assertEquals("Ouchy", s2.name(8)),
                () -> assertEquals("Palézieux", s2.name(9)),
                () -> assertEquals("Paris", s2.name(10)),
                () -> assertEquals("Renens", s2.name(11)),
                () -> assertEquals("Washington", s2.name(12)));
    }


}
