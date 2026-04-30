package ch.epfl.rechor;

import ch.epfl.rechor.timetable.mapped.Structure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyStructureTest {

    @Test
    void testCreationStructureValide() {
        // Cette opération ne doit pas lever d'exception.
        new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
    }

    /**
     * Teste le calcul de la taille totale de la structure.
     * Par exemple, pour une structure composée d'un champ U8 (1 octet),
     * d'un champ S32 (4 octets) et d'un champ U16 (2 octets), la taille totale doit être 7.
     */
    @Test
    void testTotalSize() {
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.S32),
                Structure.field(2, Structure.FieldType.U16)
        );
        assertEquals(7, s.totalSize());
    }

    /**
     * Teste le calcul de l'offset pour différents champs et indices d'élément.
     * Exemple : Pour une structure avec deux champs U16 (2 octets chacun),
     * la taille totale est 4. L'offset du champ d'indice 1 pour l'élément d'indice 1 doit être 6.
     */
    @Test
    void testOffsetCalcul() {
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U16),  // 2 octets
                Structure.field(1, Structure.FieldType.U16)   // 2 octets
        );
        // Pour l'élément 0
        assertEquals(0, s.offset(0, 0), "L'offset du champ 0 pour l'élément 0 doit être 0.");
        assertEquals(2, s.offset(1, 0), "L'offset du champ 1 pour l'élément 0 doit être 2.");
        // Pour l'élément 1, la taille totale de la structure étant 4
        assertEquals(4, s.offset(0, 1), "L'offset du champ 0 pour l'élément 1 doit être 4.");
        assertEquals(6, s.offset(1, 1), "L'offset du champ 1 pour l'élément 1 doit être 6.");
    }

    /**
     * Teste que la création d'une Structure avec des champs dont les indices ne sont pas consécutifs
     * lève une IllegalArgumentException.
     */
    @Test
    void testInvalidFieldOrder() {
        assertThrows(IllegalArgumentException.class, () ->
                new Structure(
                        Structure.field(0, Structure.FieldType.U8),
                        Structure.field(2, Structure.FieldType.U16)
                )
        );
    }

    /**
     * Teste que l'appel à offset avec un index d'élément négatif lève une IndexOutOfBoundsException.
     */
    @Test
    void testOffsetNegativeElement() {
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.S32)
        );
        assertThrows(IndexOutOfBoundsException.class, () -> s.offset(1, -1));
    }

    @Test
    void testOffsetForStructureWithDifferentFieldTypes() {
        // Structure: U8 (1 octet), S32 (4 octets), U16 (2 octets) => totalSize = 7.
        // Field offsets: 0, 1, 5.
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.S32),
                Structure.field(2, Structure.FieldType.U16)
        );
        assertEquals(0, s.offset(0, 0));
        assertEquals(1, s.offset(1, 0));
        assertEquals(5, s.offset(2, 0));

        // Pour l'élément d'indice 2, offset = 2*7 + field offset.
        assertEquals(14, s.offset(0, 2));
        assertEquals(15, s.offset(1, 2));
        assertEquals(19, s.offset(2, 2));
    }

    @Test
    void testOffsetForSingleFieldStructure() {
        // Structure: S32 (4 octets) => totalSize = 4.
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.S32)
        );
        assertEquals(0, s.offset(0, 0));
        assertEquals(4, s.offset(0, 1));
        assertEquals(8, s.offset(0, 2));
    }

    @Test
    void testOffsetForMultipleElements() {
        // Structure: deux champs U16 (2 octets chacun) => totalSize = 4.
        // Offsets: field0 = 0, field1 = 2.
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.U16)
        );
        for (int elem = 0; elem < 5; elem++) {
            int base = elem * 4;
            assertEquals(base, s.offset(0, elem));
            assertEquals(base + 2, s.offset(1, elem));
        }
    }

    @Test
    void testOffsetForInvalidFieldIndex() {
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.S32)
        );
        assertThrows(IndexOutOfBoundsException.class, () -> s.offset(2, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.offset(-1, 0));
    }

    @Test
    void testFieldNullTypeThrows() {
        // Vérifie que le constructeur compact de Field lève une NullPointerException si le type est null.
        assertThrows(NullPointerException.class, () -> {
            new Structure.Field(0, null);
        });
    }

    @Test
    void testFactoryMethodField() {
        // Vérifie que la méthode statique field crée bien un Field avec les bons attributs.
        Structure.Field f = Structure.field(0, Structure.FieldType.U8);
        assertNotNull(f);
        assertEquals(0, f.index());
        assertEquals(Structure.FieldType.U8, f.type());
    }

    @Test
    void testTotalSize2() {
        // Création d'une Structure avec trois champs :
        // U8 -> 1 octet, U16 -> 2 octets, S32 -> 4 octets, total attendu = 7
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        assertEquals(1 + 2 + 4, s.totalSize());
    }

    @Test
    void testOffsetCalculation() {
        // Exemple donné dans l'énoncé : deux champs de type U16, totalSize = 2 + 2 = 4.
        // Le premier champ (index 0) commence à l'offset 0, le second (index 1) à l'offset 2.
        // Ainsi, pour l'élément d'index 1, l'offset du champ 1 doit être 1*4 + 2 = 6.
        int ALIAS_ID = 0;
        int STATION_NAME_ID = 1;
        Structure s = new Structure(
                Structure.field(ALIAS_ID, Structure.FieldType.U16),
                Structure.field(STATION_NAME_ID, Structure.FieldType.U16)
        );
        // Tests sur différents éléments
        assertEquals(0, s.offset(ALIAS_ID, 0));           // Premier élément, champ 0
        assertEquals(2, s.offset(STATION_NAME_ID, 0));       // Premier élément, champ 1
        assertEquals(4, s.offset(ALIAS_ID, 1));              // Second élément, champ 0
        assertEquals(6, s.offset(STATION_NAME_ID, 1));       // Second élément, champ 1
    }

    @Test
    void testInvalidFieldOrderThrows() {
        // La structure doit lever une IllegalArgumentException si les champs ne sont pas fournis dans l'ordre.
        // Ici, le premier champ a l'index 1 au lieu de 0.
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure(
                    Structure.field(1, Structure.FieldType.U8),
                    Structure.field(2, Structure.FieldType.U16)
            );
        });
    }

    @Test
    void testOffsetWithNegativeElementIndexThrows() {
        // Vérifie que offset lève une IndexOutOfBoundsException si l'elementIndex est négatif.
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );
        assertThrows(IndexOutOfBoundsException.class, () -> {
            s.offset(0, -1);
        });
    }

    @Test
    void testOffsetWithInvalidFieldIndexThrows() {
        // Vérifie que offset lève une IndexOutOfBoundsException si le fieldIndex n'est pas valide.
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );
        assertThrows(IndexOutOfBoundsException.class, () -> {
            s.offset(2, 0);
        });
    }

    @Test
    void testOffsetWithLargeElementIndex() {
        // Structure avec trois champs : U8, U16, S32.
        // Total size = 1 + 2 + 4 = 7.
        // Offsets pour chaque champ : champ 0 -> 0, champ 1 -> 1, champ 2 -> 3.
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        int totalSize = s.totalSize();
        assertEquals(7, totalSize);

        // Test pour un élément avec un grand index.
        int elementIndex = 1_000_000;
        // Vérifie pour chaque champ que l'offset correspond à : elementIndex * totalSize + fieldOffset
        // Pour champ 0 : offset = elementIndex * 7 + 0.
        assertEquals(elementIndex * 7 + 0, s.offset(0, elementIndex));
        // Pour champ 1 : offset = elementIndex * 7 + 1.
        assertEquals(elementIndex * 7 + 1, s.offset(1, elementIndex));
        // Pour champ 2 : offset = elementIndex * 7 + 3.
        assertEquals(elementIndex * 7 + 3, s.offset(2, elementIndex));
    }

    @Test
    void testOffsetForAllFields() {
        // Création d'une structure avec plusieurs champs de types variés.
        // On définit ici 4 champs pour tester le calcul d'offset pour chacun.
        // Tailles respectives : U8 (1), U8 (1), U16 (2), S32 (4) -> totalSize = 1+1+2+4 = 8.
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.U8),   // offset attendu 0
                Structure.field(1, Structure.FieldType.U8),   // offset attendu 1
                Structure.field(2, Structure.FieldType.U16),  // offset attendu 2 (1+1)
                Structure.field(3, Structure.FieldType.S32)   // offset attendu 4 (1+1+2)
        );
        int totalSize = s.totalSize();
        assertEquals(8, totalSize);

        // Pour l'élément 0, les offsets devraient être égaux aux offsets des champs.
        assertEquals(0, s.offset(0, 0));
        assertEquals(1, s.offset(1, 0));
        assertEquals(2, s.offset(2, 0));
        assertEquals(4, s.offset(3, 0));

        // Pour l'élément 5, les offsets sont calculés comme 5 * totalSize + offset du champ.
        assertEquals(5 * 8 + 0, s.offset(0, 5));
        assertEquals(5 * 8 + 1, s.offset(1, 5));
        assertEquals(5 * 8 + 2, s.offset(2, 5));
        assertEquals(5 * 8 + 4, s.offset(3, 5));
    }

    @Test
    void testTotalSizeEdgeCaseWithOneField() {
        // Vérifie que totalSize retourne la bonne valeur lorsqu'il n'y a qu'un seul champ.
        Structure s = new Structure(
                Structure.field(0, Structure.FieldType.S32)  // taille attendue = 4
        );
        assertEquals(4, s.totalSize());
        // Pour l'élément 3, offset = 3 * 4 + 0 = 12
        assertEquals(12, s.offset(0, 3));
    }

    @Test
    void testStructureCreation() {
        Structure structure = new Structure(
                new Structure.Field(0, Structure.FieldType.U16),
                new Structure.Field(1, Structure.FieldType.S32)
        );

        assertEquals(6, structure.totalSize());
        assertEquals(0, structure.offset(0, 0));
        assertEquals(2, structure.offset(1, 0));
        assertEquals(6, structure.offset(0, 1));
    }

    @Test
    void testInvalidStructure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure(
                    new Structure.Field(1, Structure.FieldType.U16), // Index 1 avant 0
                    new Structure.Field(0, Structure.FieldType.S32)
            );
        });
    }

 }
