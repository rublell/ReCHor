package ch.epfl.rechor;

import ch.epfl.rechor.timetable.Platforms;
import ch.epfl.rechor.timetable.StationAliases;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.mapped.*;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;
import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MyBufferedPlatformsTest {
    // La structure de BufferedPlatforms est composée de deux champs U16, soit 2 octets chacun,
    // ce qui donne un enregistrement de 4 octets.
    private static final int RECORD_SIZE = 4;

    @Test
    void testSingleRecordExactOutput() {
        // On définit une table de chaînes.
        List<String> stringTable = Arrays.asList("PlatformA", "PlatformB", "Extra");
        // Création d'un ByteBuffer pour un enregistrement (4 octets).
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
        // Pour cet enregistrement :
        // - Le nom (champ 0) contient l'indice 1 => "PlatformB"
        // - Le stationId (champ 1) contient la valeur 42 (en U16).
        buffer.putShort((short) 1); // name field
        buffer.putShort((short) 42); // stationId field
        buffer.flip();

        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);

        // Vérifie que le nombre d'enregistrements est 1.
        assertEquals(1, bp.size());
        // Vérifie que le nom est bien celui correspondant à l'indice stocké.
        assertEquals("PlatformB", bp.name(0));
        // Vérifie que stationId est exactement 42.
        assertEquals(42, bp.stationId(0));
    }

    @Test
    void testMultipleRecordsExactOutput() {
        // Table de chaînes avec plusieurs entrées.
        List<String> stringTable = Arrays.asList("P0", "P1", "P2", "P3", "P4");
        // Création d'un buffer pour 3 enregistrements (3 * 4 = 12 octets).
        ByteBuffer buffer = ByteBuffer.allocate(3 * RECORD_SIZE);

        // Enregistrement 0 :
        // name = index 0 ("P0"), stationId = 100.
        buffer.putShort((short) 0);
        buffer.putShort((short) 100);

        // Enregistrement 1 :
        // name = index 2 ("P2"), stationId = 200.
        buffer.putShort((short) 2);
        buffer.putShort((short) 200);

        // Enregistrement 2 :
        // name = index 1 ("P1"), stationId = 300.
        buffer.putShort((short) 1);
        buffer.putShort((short) 300);

        buffer.flip();

        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);
        assertEquals(3, bp.size());

        // Vérifications précises pour chaque enregistrement.
        assertEquals("P0", bp.name(0));
        assertEquals(100, bp.stationId(0));

        assertEquals("P2", bp.name(1));
        assertEquals(200, bp.stationId(1));

        assertEquals("P1", bp.name(2));
        assertEquals(300, bp.stationId(2));
    }

    @Test
    void testIndexOutOfBoundsNegative() {
        // Table de chaînes de base et buffer pour un enregistrement.
        List<String> stringTable = Arrays.asList("Plat1", "Plat2");
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
        buffer.putShort((short) 0);
        buffer.putShort((short) 10);
        buffer.flip();

        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);

        // Un indice négatif doit lever une exception.
        assertThrows(IndexOutOfBoundsException.class, () -> bp.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bp.stationId(-1));
    }

    @Test
    void testIndexOutOfBoundsTooHigh() {
        // Table de chaînes et buffer pour 2 enregistrements.
        List<String> stringTable = Arrays.asList("P1", "P2", "P3");
        ByteBuffer buffer = ByteBuffer.allocate(2 * RECORD_SIZE);
        // Premier enregistrement
        buffer.putShort((short) 0);
        buffer.putShort((short) 20);
        // Deuxième enregistrement
        buffer.putShort((short) 1);
        buffer.putShort((short) 30);
        buffer.flip();

        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);

        // La taille étant 2, accéder à l'indice 2 ou plus doit lever une exception.
        assertThrows(IndexOutOfBoundsException.class, () -> bp.name(2));
        assertThrows(IndexOutOfBoundsException.class, () -> bp.stationId(2));
    }

    @Test
    void testBufferCapacityNotMultipleOfRecordSize() {
        // Vérifie que le constructeur lève une exception lorsque la capacité du buffer n'est pas un multiple de 4.
        List<String> stringTable = Arrays.asList("P1", "P2");
        // Capacité non multiple de 4, par exemple 6 octets.
        ByteBuffer buffer = ByteBuffer.allocate(6);
        assertThrows(IllegalArgumentException.class, () -> new BufferedPlatforms(stringTable, buffer));
    }

    @Test
    void testLargeNumberOfRecordsEdge() {
        // Teste le comportement avec un grand nombre d'enregistrements.
        int n = 5000;
        List<String> stringTable = new ArrayList<>();
        // Création d'une table de chaînes avec suffisamment d'entrées.
        for (int i = 0; i < n * 2; i++) {
            stringTable.add("Str" + i);
        }

        ByteBuffer buffer = ByteBuffer.allocate(n * RECORD_SIZE);
        for (int i = 0; i < n; i++) {
            // Pour chaque enregistrement, on utilise des indices calculés.
            buffer.putShort((short) (i % (n * 2)));         // pour le nom
            buffer.putShort((short) ((i + 1000) % (n * 2)));  // pour stationId
        }
        buffer.flip();

        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);
        assertEquals(n, bp.size());

        // Vérification sur quelques enregistrements clés.
        assertEquals("Str" + (0 % (n * 2)), bp.name(0));
        assertEquals((int)((0 + 1000) % (n * 2)), bp.stationId(0));

        int mid = n / 2;
        assertEquals("Str" + (mid % (n * 2)), bp.name(mid));
        assertEquals((int)((mid + 1000) % (n * 2)), bp.stationId(mid));

        int last = n - 1;
        assertEquals("Str" + (last % (n * 2)), bp.name(last));
        assertEquals((int)(((last + 1000) % (n * 2))), bp.stationId(last));
    }

    @Test
    void testBufferModificationEdge() {
        // Vérifie que la modification du contenu du ByteBuffer après création de BufferedPlatforms est répercutée.
        List<String> stringTable = Arrays.asList("Alpha", "Beta", "Gamma", "Delta");
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
        // Enregistrement initial : name = 0 ("Alpha"), stationId = 10.
        buffer.putShort((short) 0);
        buffer.putShort((short) 10);
        buffer.flip();

        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);
        assertEquals("Alpha", bp.name(0));
        assertEquals(10, bp.stationId(0));

        // Modification du buffer pour changer les valeurs.
        buffer.rewind();
        buffer.putShort((short) 2);  // maintenant "Gamma"
        buffer.putShort((short) 20); // stationId devient 20
        buffer.flip();

        // Les nouvelles valeurs doivent être retournées.
        assertEquals("Gamma", bp.name(0));
        assertEquals(20, bp.stationId(0));
    }

    @Test
    void testEmptyBufferAsRecordCountEdge() {
        // Un ByteBuffer vide est valide et doit indiquer 0 enregistrements.
        List<String> stringTable = Arrays.asList("X", "Y");
        ByteBuffer buffer = ByteBuffer.allocate(0);
        BufferedPlatforms bp = new BufferedPlatforms(stringTable, buffer);
        assertEquals(0, bp.size());
        assertThrows(IndexOutOfBoundsException.class, () -> bp.name(0));
        assertThrows(IndexOutOfBoundsException.class, () -> bp.stationId(0));
    }

    @Test
    void testBufferedPlatforms() {
        List<String> stringTable = List.of("1", "70", "Lausanne", "Palézieux");
        byte[] bytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertEquals("1", platforms.name(0));
        assertEquals("70", platforms.name(1));
        //TESTER STATION_ID
    }
    private final static int NAME_INDEX = 0;
    private final static int STATION_ID_INDEX = 1;

    @Test
    void testName() {
        List<String> stringTable = Arrays.asList("1", "70", "A");
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putShort((short) 0); // NAME_INDEX = 0 ("1")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 1); // NAME_INDEX = 1 ("70")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 2); // NAME_INDEX = 2 ("A")
        buffer.putShort((short) 1); // STATION_ID_INDEX = 1
        buffer.flip();

        Platforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertEquals("1", platforms.name(0));
        assertEquals("70", platforms.name(1));
        assertEquals("A", platforms.name(2));
    }

    @Test
    void testStationId() {
        List<String> stringTable = Arrays.asList("1", "70", "A");
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putShort((short) 0); // NAME_INDEX = 0 ("1")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 1); // NAME_INDEX = 1 ("70")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 2); // NAME_INDEX = 2 ("A")
        buffer.putShort((short) 1); // STATION_ID_INDEX = 1
        buffer.flip();

        Platforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertEquals(0, platforms.stationId(0));
        assertEquals(0, platforms.stationId(1));
        assertEquals(1, platforms.stationId(2));
    }

    @Test
    void testSize2() {
        List<String> stringTable = Arrays.asList("1", "70", "A");
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putShort((short) 0); // NAME_INDEX = 0 ("1")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 1); // NAME_INDEX = 1 ("70")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 2); // NAME_INDEX = 2 ("A")
        buffer.putShort((short) 1); // STATION_ID_INDEX = 1
        buffer.flip();

        Platforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertEquals(3, platforms.size());
    }

    @Test
    void testNameIndexOutOfBounds() {
        List<String> stringTable = Arrays.asList("1", "70", "A");
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putShort((short) 0); // NAME_INDEX = 0 ("1")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 1); // NAME_INDEX = 1 ("70")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 2); // NAME_INDEX = 2 ("A")
        buffer.putShort((short) 1); // STATION_ID_INDEX = 1
        buffer.flip();

        Platforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> platforms.name(4));
    }
    @Test
    void testStationIdIndexOutOfBounds() {
        List<String> stringTable = Arrays.asList("1", "70", "A");
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putShort((short) 0); // NAME_INDEX = 0 ("1")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 1); // NAME_INDEX = 1 ("70")
        buffer.putShort((short) 0); // STATION_ID_INDEX = 0
        buffer.putShort((short) 2); // NAME_INDEX = 2 ("A")
        buffer.putShort((short) 1); // STATION_ID_INDEX = 1
        buffer.flip();

        Platforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> platforms.stationId(4));
    }
    @Test
    void FormatCorrectly(){
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("00 05 00 04 00 02 00 03");
        List<String>  chainTable= new ArrayList<>();
        chainTable.add("1");
        chainTable.add("70");
        chainTable.add("Anet");
        chainTable.add("Ins");
        chainTable.add("Lausanne");
        chainTable.add("losanna");
        chainTable.add("PalÃ©zieux");



        ArrayList<String> stringTable = new ArrayList<>();
        stringTable.add("Losanna");
        stringTable.add("Lausanne");
        stringTable.add("Anet");
        stringTable.add("Ins");
        BufferedStationAliases bufferedStationAliases = new BufferedStationAliases(chainTable,
                ByteBuffer.wrap(bytes));
        assertEquals("Lausanne",bufferedStationAliases.stationName(0));
        // poser la question
    }

    @Test
    void testAlias() {
        List<String> stringTable = Arrays.asList("Losanna", "Lausanne", "Anet", "Ins");
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putShort((byte) 0); // ALIAS_ID = 0 (Losanna)
        buffer.putShort((byte) 1); // STATION_NAME_INDEX = 1 (Lausanne)
        buffer.putShort((byte) 2); // ALIAS_ID = 2 (Anet)
        buffer.putShort((byte) 3); // STATION_NAME_INDEX = 3 (Ins)
        buffer.flip();

        StationAliases stationAliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals("Losanna", stationAliases.alias(0));
        assertEquals("Anet", stationAliases.alias(1));
    }

    @Test
    void testStationName() {
        List<String> stringTable = Arrays.asList("Losanna", "Lausanne", "Anet", "Ins");
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putShort((short) 0); // ALIAS_ID = 0 (Losanna)
        buffer.putShort((short) 1); // STATION_NAME_INDEX = 1 (Lausanne)
        buffer.putShort((short) 2); // ALIAS_ID = 2 (Anet)
        buffer.putShort((short) 3); // STATION_NAME_INDEX = 3 (Ins)
        buffer.flip();

        StationAliases stationAliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals("Lausanne", stationAliases.stationName(0));
        assertEquals("Ins", stationAliases.stationName(1));
    }

    @Test
    void testSize() {
        List<String> stringTable = Arrays.asList("Losanna", "Lausanne", "Anet", "Ins");
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putShort((short) 0); // ALIAS_ID = 0 (Losanna)
        buffer.putShort((short) 1); // STATION_NAME_INDEX = 1 (Lausanne)
        buffer.putShort((short) 2); // ALIAS_ID = 2 (Anet)
        buffer.putShort((short) 3); // STATION_NAME_INDEX = 3 (Ins)
        buffer.flip();

        StationAliases stationAliases = new BufferedStationAliases(stringTable, buffer);
        assertEquals(2, stationAliases.size());
    }

    @Test
    void testEmptyBuffer() {
        List<String> stringTable = Arrays.asList("Losanna", "Lausanne", "Anet", "Ins");
        ByteBuffer buffer = ByteBuffer.allocate(0);

        StationAliases stationAliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals(0, stationAliases.size());
    }
    private final static int NAME_ID = 0;
    private final static int LON = 1;
    private final static int LAT = 2;

    private static final double UNIT_TO_DEGREE = Math.scalb(360.0, -32);

    @Test
    void testName2() {
        List<String> stringTable = Arrays.asList("Lausanne", "PalÃ©zieux");
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.putShort((short) 0); // NAME_ID = 0 (Lausanne)
        buffer.putInt(79088148); // LON
        buffer.putInt(50917265); // LAT
        buffer.putShort((short) 1); // NAME_ID = 1 (PalÃ©zieux)
        buffer.putInt(81483868); // LON
        buffer.putInt(51152980); // LAT
        buffer.flip();

        Stations stations = new BufferedStations(stringTable, buffer);

        assertEquals("Lausanne", stations.name(0));
        assertEquals("PalÃ©zieux", stations.name(1));
    }

    @Test
    void testLongitude() {
        List<String> stringTable = Arrays.asList("Lausanne", "PalÃ©zieux");
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.putShort((short) 0); // NAME_ID = 0 (Lausanne)
        buffer.putInt(79088148); // LON
        buffer.putInt(50917265); // LAT
        buffer.putShort((short) 1); // NAME_ID = 1 (PalÃ©zieux)
        buffer.putInt(81483868); // LON
        buffer.putInt(51152980); // LAT
        buffer.flip();

        Stations stations = new BufferedStations(stringTable, buffer);

        assertEquals(UNIT_TO_DEGREE * 79088148, stations.longitude(0));
        assertEquals(UNIT_TO_DEGREE * 81483868, stations.longitude(1));
    }

    @Test
    void testLatitude() {
        List<String> stringTable = Arrays.asList("Lausanne", "PalÃ©zieux");
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.putShort((short) 0); // NAME_ID = 0 (Lausanne)
        buffer.putInt(79088148); // LON
        buffer.putInt(50917265); // LAT
        buffer.putShort((short) 1); // NAME_ID = 1 (PalÃ©zieux)
        buffer.putInt(81483868); // LON
        buffer.putInt(51152980); // LAT
        buffer.flip();

        Stations stations = new BufferedStations(stringTable, buffer);

        assertEquals(UNIT_TO_DEGREE * 50917265, stations.latitude(0));
        assertEquals(UNIT_TO_DEGREE * 51152980, stations.latitude(1));
    }

    @Test
    void testSize3() {
        List<String> stringTable = Arrays.asList("Lausanne", "PalÃ©zieux");
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.putShort((short) 0); // NAME_ID = 0 (Lausanne)
        buffer.putInt(79088148); // LON
        buffer.putInt(50917265); // LAT
        buffer.putShort((short) 1); // NAME_ID = 1 (PalÃ©zieux)
        buffer.putInt(81483868); // LON
        buffer.putInt(51152980); // LAT
        buffer.flip();

        Stations stations = new BufferedStations(stringTable, buffer);

        assertEquals(2, stations.size());
    }
    @Test
    void FormatCorrectly2(){
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("00 04 04 b6 ca 14 21 14 1f a1 00 06 04 dc cc 12 21 18 da 03");

        List<String>  chainTable= new ArrayList<>();
        chainTable.add("1");
        chainTable.add("70");
        chainTable.add("Anet");
        chainTable.add("Ins");
        chainTable.add("Lausanne");
        chainTable.add("losanna");
        chainTable.add("PalÃ©zieux");
        BufferedStations bufferedStations = new BufferedStations(chainTable,ByteBuffer.wrap(bytes));
        //assertEquals("46.516792",bufferedStations.latitude(0));
        //assertEquals("6.629092",bufferedStations.longitude(0));
        assertEquals("PalÃ©zieux",bufferedStations.name(1));
    }
    @Test
    void testConstructorValid() {
        Structure structure = new Structure(field(0, U8), field(1, U16), field(2, S32));
        ByteBuffer buffer = ByteBuffer.allocate(structure.totalSize() * 2);
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(2, structuredBuffer.size());
    }

    @Test
    void testConstructorInvalidBufferSize() {
        Structure structure = new Structure(field(0, U8), field(1, U16), field(2, S32));
        ByteBuffer buffer = ByteBuffer.allocate(9);
        assertThrows(IllegalArgumentException.class, () -> new StructuredBuffer(structure, buffer));
    }

    @Test
    void testSize5() {
        Structure structure = new Structure(field(0, U8));
        ByteBuffer buffer = ByteBuffer.allocate(3);
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(3, structuredBuffer.size());
    }

    @Test
    void testGetU8() {
        Structure structure = new Structure(field(0, U8));
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) 0xAA);
        buffer.put((byte) 0xBB);
        buffer.flip();
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(0xAA, structuredBuffer.getU8(0, 0));
        assertEquals(0xBB, structuredBuffer.getU8(0, 1));
    }

    @Test
    void testGetU16() {
        Structure structure = new Structure(field(0, U16));
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x03);
        buffer.put((byte) 0x04);
        buffer.flip();
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(0x0102, structuredBuffer.getU16(0, 0));
        assertEquals(0x0304, structuredBuffer.getU16(0, 1));
    }

    @Test
    void testGetS32() {
        Structure structure = new Structure(field(0, S32));
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x03);
        buffer.put((byte) 0x04);
        buffer.put((byte) 0x05);
        buffer.put((byte) 0x06);
        buffer.put((byte) 0x07);
        buffer.put((byte) 0x08);
        buffer.flip();
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(0x01020304, structuredBuffer.getS32(0, 0));
        assertEquals(0x05060708, structuredBuffer.getS32(0, 1));
    }

    @Test
    void testGetU8InvalidIndex() {
        Structure structure = new Structure(field(0, U8));
        ByteBuffer buffer = ByteBuffer.allocate(1);
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getU8(0, 1));
    }

    @Test
    void testGetU16InvalidFieldIndex() {
        Structure structure = new Structure(field(0, U16));
        ByteBuffer buffer = ByteBuffer.allocate(2);
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getU16(1, 0));
    }

    @Test
    void testExampleAlternativeNames() {
        int STATION_NAME_ID = 1;
        Structure structure = new Structure(field(0, U16), field(STATION_NAME_ID, U16));
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x03);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x04);
        buffer.flip();
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        int stationNameStringIndex = structuredBuffer.getU16(STATION_NAME_ID, 0);
        assertEquals(4, stationNameStringIndex);
    }
    @Test
    void testFieldConstructor() {
        Structure.Field field = field(0, U16);
        assertEquals(0, field.index());
        assertEquals(U16, field.type());
    }

    @Test
    void testFieldConstructorNullType() {
        assertThrows(NullPointerException.class, () -> field(0, null));
    }

    @Test
    void testStructureConstructorInvalidOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Structure(field(1, U16), field(0, S32)));
    }

    @Test
    void testTotalSize() {
        Structure structure = new Structure(field(0, U8), field(1, U16), field(2, S32));
        assertEquals(7, structure.totalSize());
    }

    @Test
    void testOffsetValid() {
        Structure structure = new Structure(field(0, U8), field(1, U16), field(2, S32));
        assertEquals(0, structure.offset(0, 0));
        assertEquals(1, structure.offset(1, 0));
        assertEquals(3, structure.offset(2, 0));
        assertEquals(7, structure.offset(0, 1));
        assertEquals(8, structure.offset(1, 1));
        assertEquals(10, structure.offset(2, 1));
    }

    @Test
    void testOffsetInvalidFieldIndex() {
        Structure structure = new Structure(field(0, U8), field(1, U16));
        assertThrows(IndexOutOfBoundsException.class, () -> structure.offset(2, 0));
    }

    @Test
    void testOffsetLargeElementIndex() {
        Structure structure = new Structure(field(0, U8));
        assertEquals(100, structure.offset(0, 100));
    }

    @Test
    void testExampleAlternativeNames6() {
        int ALIAS_ID = 0;
        int STATION_NAME_ID = 1;
        Structure STRUCTURE = new Structure(field(ALIAS_ID, U16), field(STATION_NAME_ID, U16));
        int offset = STRUCTURE.offset(STATION_NAME_ID, 1);
        assertEquals(6, offset);
    }

}