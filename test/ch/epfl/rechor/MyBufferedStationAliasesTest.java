package ch.epfl.rechor;

import ch.epfl.rechor.timetable.mapped.BufferedStationAliases;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MyBufferedStationAliasesTest {
    // La structure utilisée par BufferedStationAliases comporte deux champs U16, soit 2 octets chacun,
    // ce qui donne un enregistrement de 4 octets.
    private static final int RECORD_SIZE = 4;

    @Test
    void testSingleRecordExactOutput() {
        // On définit une table de chaînes.
        List<String> stringTable = Arrays.asList("Alias0", "Alias1", "Station0", "Station1");
        // On souhaite tester avec un seul enregistrement :
        // - Pour l'alias, on met l'indice 2 ("Station0")
        // - Pour le nom de la station, on met l'indice 3 ("Station1")
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE3);
        buffer.putShort((short) 2); // alias = stringTable.get(2) => "Station0"
        buffer.putShort((short) 3); // stationName = stringTable.get(3) => "Station1"
        buffer.flip();

        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);

        assertEquals(1, bsa.size());
        assertEquals("Station0", bsa.alias(0));
        assertEquals("Station1", bsa.stationName(0));
    }

    @Test
    void testMultipleRecordsExactOutput() {
        // On définit une table de chaînes contenant plusieurs valeurs.
        List<String> stringTable = Arrays.asList("A0", "A1", "A2", "S0", "S1", "S2", "Extra");
        // On construit un ByteBuffer pour 3 enregistrements (3 * 4 = 12 octets).
        ByteBuffer buffer = ByteBuffer.allocate(3 * RECORD_SIZE3);

        // Enregistrement 0 :
        // alias = index 0 ("A0"), stationName = index 3 ("S0")
        buffer.putShort((short) 0);
        buffer.putShort((short) 3);
        // Enregistrement 1 :
        // alias = index 1 ("A1"), stationName = index 4 ("S1")
        buffer.putShort((short) 1);
        buffer.putShort((short) 4);
        // Enregistrement 2 :
        // alias = index 2 ("A2"), stationName = index 5 ("S2")
        buffer.putShort((short) 2);
        buffer.putShort((short) 5);
        buffer.flip();

        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);
        assertEquals(3, bsa.size());

        // Vérification des enregistrements
        assertEquals("A0", bsa.alias(0));
        assertEquals("S0", bsa.stationName(0));

        assertEquals("A1", bsa.alias(1));
        assertEquals("S1", bsa.stationName(1));

        assertEquals("A2", bsa.alias(2));
        assertEquals("S2", bsa.stationName(2));
    }

    @Test
    void testIndexOutOfBoundsNegative() {
        // Table de chaînes minimale et un buffer pour un enregistrement.
        List<String> stringTable = Arrays.asList("Alias", "Station");
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE3);
        buffer.putShort((short) 0);
        buffer.putShort((short) 1);
        buffer.flip();

        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);

        // Un indice négatif doit lever une exception.
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.alias(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.stationName(-1));
    }

    @Test
    void testIndexOutOfBoundsTooHigh() {
        // Table de chaînes minimale et un buffer pour 2 enregistrements.
        List<String> stringTable = Arrays.asList("Alias0", "Station0", "Alias1", "Station1");
        ByteBuffer buffer = ByteBuffer.allocate(2 * RECORD_SIZE3);
        // Premier enregistrement
        buffer.putShort((short) 0);
        buffer.putShort((short) 1);
        // Deuxième enregistrement
        buffer.putShort((short) 0);
        buffer.putShort((short) 1);
        buffer.flip();

        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);

        // La taille est 2, donc accéder à l'indice 2 ou supérieur doit lever une exception.
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.alias(2));
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.stationName(2));
    }

    @Test
    void testBufferCapacityNotMultipleOfRecordSize() {
        // Vérifie que le constructeur lève une exception si le buffer n'est pas multiple de 4 octets.
        List<String> stringTable = Arrays.asList("A", "B");
        // Créer un ByteBuffer avec une capacité non multiple de RECORD_SIZE (ex. 6 octets).
        ByteBuffer buffer = ByteBuffer.allocate(6);
        assertThrows(IllegalArgumentException.class, () -> new BufferedStationAliases(stringTable, buffer));
    }

    @Test
    void testExactOutputWithModifiedBufferValues() {
        // On prépare une table de chaînes.
        List<String> stringTable = Arrays.asList("FirstAlias", "FirstStation", "SecondAlias", "SecondStation");
        // On prépare un buffer pour 2 enregistrements.
        ByteBuffer buffer = ByteBuffer.allocate(2 * RECORD_SIZE3);

        // Enregistrement 0 :
        // alias = index 0 ("FirstAlias"), stationName = index 1 ("FirstStation")
        buffer.putShort((short) 0);
        buffer.putShort((short) 1);
        // Enregistrement 1 :
        // alias = index 2 ("SecondAlias"), stationName = index 3 ("SecondStation")
        buffer.putShort((short) 2);
        buffer.putShort((short) 3);
        buffer.flip();

        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);

        // Vérification des enregistrements
        assertEquals("FirstAlias", bsa.alias(0));
        assertEquals("FirstStation", bsa.stationName(0));
        assertEquals("SecondAlias", bsa.alias(1));
        assertEquals("SecondStation", bsa.stationName(1));
    }

    // La structure attendue dans BufferedStationAliases comporte deux champs U16,
    // soit 2 octets par champ, donc RECORD_SIZE = 4 octets par enregistrement.
    private static final int RECORD_SIZE3 = 4;

    @Test
    void testEmptyStringTable() {
        // Si la table de chaînes est vide, toute tentative d'accès devra lever une exception.
        List<String> emptyTable = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE3);
        buffer.putShort((short) 0);
        buffer.putShort((short) 0);
        buffer.flip();
        BufferedStationAliases bsa = new BufferedStationAliases(emptyTable, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> bsa.alias(0));
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.stationName(0));
    }

    @Test
    void testOutOfRangeStringTableIndex() {
        // La table de chaînes ne contient qu'un seul élément, mais le buffer référence un indice hors bornes.
        List<String> stringTable = List.of("OnlyOne");
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE3);
        // On met ici un indice 5 qui est bien hors bornes pour stringTable.
        buffer.putShort((short) 5);
        buffer.putShort((short) 5);
        buffer.flip();
        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);

        // L'accès à alias ou stationName doit lever une IndexOutOfBoundsException lors de la récupération dans la table.
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.alias(0));
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.stationName(0));
    }

    @Test
    void testLargeNumberOfRecordsEdge() {
        // Teste le comportement avec un grand nombre d'enregistrements.
        int n = 10_000;
        List<String> stringTable = new ArrayList<>();
        // Création d'une table de chaînes avec suffisamment d'entrées.
        for (int i = 0; i < n * 2; i++) {
            stringTable.add("Str" + i);
        }
        ByteBuffer buffer = ByteBuffer.allocate(n * RECORD_SIZE3);
        for (int i = 0; i < n; i++) {
            // Pour chaque enregistrement, on utilise des indices calculés pour rester dans les bornes.
            buffer.putShort((short) (i % (n * 2)));        // alias
            buffer.putShort((short) ((i + n) % (n * 2)));    // stationName
        }
        buffer.flip();
        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);
        assertEquals(n, bsa.size());

        // Vérifier quelques enregistrements clés : premier, milieu et dernier.
        assertEquals("Str" + 0, bsa.alias(0));
        assertEquals("Str" + (0 + n), bsa.stationName(0));

        int mid = n / 2;
        assertEquals("Str" + (mid % (n * 2)), bsa.alias(mid));
        assertEquals("Str" + ((mid + n) % (n * 2)), bsa.stationName(mid));

        int last = n - 1;
        assertEquals("Str" + (last % (n * 2)), bsa.alias(last));
        assertEquals("Str" + ((last + n) % (n * 2)), bsa.stationName(last));
    }

    @Test
    void testBufferModificationEdge() {
        // Vérifie que si le contenu du ByteBuffer est modifié après la création de BufferedStationAliases,
        // ces modifications sont bien reflétées dans les accès.
        List<String> stringTable = List.of("A", "B", "C", "D");
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE3);
        // Enregistrement initial : alias = 0 ("A"), stationName = 1 ("B")
        buffer.putShort((short) 0);
        buffer.putShort((short) 1);
        buffer.flip();
        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);
        assertEquals("A", bsa.alias(0));
        assertEquals("B", bsa.stationName(0));

        // Modifier le buffer : changer alias à 2 et stationName à 3.
        buffer.rewind();
        buffer.putShort((short) 2);
        buffer.putShort((short) 3);
        buffer.flip();
        // Les lectures doivent refléter les nouvelles valeurs.
        assertEquals("C", bsa.alias(0));
        assertEquals("D", bsa.stationName(0));
    }

    @Test
    void testEmptyBufferAsRecordCountEdge() {
        // Un ByteBuffer de capacité 0 est valide et doit indiquer qu'il n'y a aucun enregistrement.
        List<String> stringTable = List.of("X", "Y");
        ByteBuffer buffer = ByteBuffer.allocate(0);
        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);
        assertEquals(0, bsa.size());
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.alias(0));
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.stationName(0));
    }

    @Test
    void testNegativeRecordIndexEdge() {
        // Vérifie que l'accès à un enregistrement via un indice négatif lève une exception.
        List<String> stringTable = List.of("Alias", "Station");
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE3);
        buffer.putShort((short) 0);
        buffer.putShort((short) 1);
        buffer.flip();
        BufferedStationAliases bsa = new BufferedStationAliases(stringTable, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> bsa.alias(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bsa.stationName(-1));
    }

    @Test
    void testBufferedStationAliases() {
        List<String> stringTable = List.of("Lausanne", "Losanna", "Ins", "Anet");
        byte[] bytes = {0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x00, 0x02};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals("Losanna", aliases.alias(0));
        assertEquals("Lausanne", aliases.stationName(0));
        assertEquals("Anet", aliases.alias(1));
        assertEquals("Ins", aliases.stationName(1));
    }
}
