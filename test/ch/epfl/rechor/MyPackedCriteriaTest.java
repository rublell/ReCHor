package ch.epfl.rechor;

import ch.epfl.rechor.journey.PackedCriteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyPackedCriteriaTest {

    @Test
    void packWorcksForValidValues(){
        long criteria = PackedCriteria.pack(500,5,123456);
        assertEquals(500, PackedCriteria.arrMins(criteria));
        assertEquals(5, PackedCriteria.changes(criteria));
        assertEquals(123456, PackedCriteria.payload(criteria));
    }

    // Test des valeurs limites pour arrMins
    @Test
    void packThrowsForInvalidArrMins() {
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(-241, 5, 0));  // Trop petit
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(2880, 5, 0));  // Trop grand
    }

    // Test des valeurs limites pour changes
    @Test
    void packThrowsForInvalidChanges() {
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(500, -1, 0));   // Trop petit
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(500, 128, 0));  // Trop grand
    }

    // Test hasDepMins pour un critère sans heure de départ
    @Test
    void hasDepMinsReturnsFalseForCriteriaWithoutDepMins() {
        long criteria = PackedCriteria.pack(1000, 2, 98765);
        assertFalse(PackedCriteria.hasDepMins(criteria));
    }

    // Test hasDepMins pour un critère avec heure de départ
    @Test
    void hasDepMinsReturnsTrueForCriteriaWithDepMins() {
        long criteria = PackedCriteria.withDepMins(PackedCriteria.pack(800, 1, 123), 300);
        assertTrue(PackedCriteria.hasDepMins(criteria));
    }

    // Test depMins renvoie l'heure correcte
    @Test
    void depMinsReturnsCorrectValue() {
        long criteria = PackedCriteria.withDepMins(PackedCriteria.pack(1200, 4, 56789), 250);
        assertEquals(250, PackedCriteria.depMins(criteria));
    }

    // Test depMins lève une exception si absente
    @Test
    void depMinsThrowsForCriteriaWithoutDepMins() {
        long criteria = PackedCriteria.pack(1000, 2, 98765);
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.depMins(criteria));
    }

    // Test des méthodes d'accès
    @Test
    void accessorsReturnCorrectValues() {
        long criteria = PackedCriteria.pack(1350, 3, 42);
        assertEquals(1350, PackedCriteria.arrMins(criteria));
        assertEquals(3, PackedCriteria.changes(criteria));
        assertEquals(42, PackedCriteria.payload(criteria));
    }

    // Test de withoutDepMins
    @Test
    void withoutDepMinsRemovesDepMins() {
        long criteria = PackedCriteria.withDepMins(PackedCriteria.pack(500, 2, 99), 200);
        long newCriteria = PackedCriteria.withoutDepMins(criteria);
        assertFalse(PackedCriteria.hasDepMins(newCriteria));
    }

    // Test withDepMins ajoute correctement une heure de départ
    @Test
    void withDepMinsAddsDepMinsCorrectly() {
        long criteria = PackedCriteria.withDepMins(PackedCriteria.pack(700, 3, 55), 180);
        assertEquals(180, PackedCriteria.depMins(criteria));
    }

    // Test withDepMins lève une exception si l'heure de départ est invalide
    @Test
    void withDepMinsThrowsForInvalidDepMins() {
        long criteria = PackedCriteria.pack(900, 2, 77);
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.withDepMins(criteria, -241));  // Trop petit
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.withDepMins(criteria, 2880));  // Trop grand
    }

    // Test de withAdditionalChange
    @Test
    void withAdditionalChangeIncrementsChanges() {
        long criteria = PackedCriteria.pack(400, 2, 88);
        long newCriteria = PackedCriteria.withAdditionalChange(criteria);
        assertEquals(3, PackedCriteria.changes(newCriteria));
    }

    // Test de withAdditionalChange avec un dépassement
    @Test
    void withAdditionalChangeThrowsForOverflow() {
        long criteria = PackedCriteria.pack(400, 127, 88);
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.withAdditionalChange(criteria));
    }

    // Test de withPayload
    @Test
    void withPayloadUpdatesPayloadCorrectly() {
        long criteria = PackedCriteria.pack(600, 1, 33);
        long newCriteria = PackedCriteria.withPayload(criteria, 999);
        assertEquals(999, PackedCriteria.payload(newCriteria));
    }

    // Test de dominatesOrIsEqual (cas de dominance)
    @Test
    void dominatesOrIsEqualWorksForDominance() {
        long c1 = PackedCriteria.pack(500, 2, 0);
        long c2 = PackedCriteria.pack(600, 3, 0);
        assertTrue(PackedCriteria.dominatesOrIsEqual(c1, c2));  // c1 est meilleur
    }

    // Test de dominatesOrIsEqual (cas d'égalité)
    @Test
    void dominatesOrIsEqualWorksForEquality() {
        long c1 = PackedCriteria.pack(450, 1, 0);
        long c2 = PackedCriteria.pack(450, 1, 0);
        assertTrue(PackedCriteria.dominatesOrIsEqual(c1, c2));
    }

    // Test de dominatesOrIsEqual (cas non dominant)
    @Test
    void dominatesOrIsEqualReturnsFalseForNonDominance() {
        long c1 = PackedCriteria.pack(700, 4, 0);
        long c2 = PackedCriteria.pack(600, 3, 0);
        assertFalse(PackedCriteria.dominatesOrIsEqual(c1, c2));
    }

    // Test de dominatesOrIsEqual avec des critères incompatibles
    @Test
    void dominatesOrIsEqualThrowsForMismatchedDepMins() {
        long c1 = PackedCriteria.withDepMins(PackedCriteria.pack(800, 2, 0), 300);
        long c2 = PackedCriteria.pack(900, 3, 0);
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.dominatesOrIsEqual(c1, c2));
    }

}
