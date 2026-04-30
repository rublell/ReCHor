package ch.epfl.rechor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyPackedRangeTest {
    @Test
    void packWorksForValidRange() {
        int interval = PackedRange.pack(100, 120);
        assertEquals(100, PackedRange.startInclusive(interval));
        assertEquals(20, PackedRange.length(interval));
        assertEquals(120, PackedRange.endExclusive(interval));
    }

    // Cas limite : startInclusive au maximum (2^24 - 1)
    @Test
    void packThrowsForStartInclusiveOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(16777216, 16777217));
    }

    // Cas limite : longueur maximale de 255
    @Test
    void packWorksForMaxLength() {
        int interval = PackedRange.pack(100, 355);
        assertEquals(100, PackedRange.startInclusive(interval));
        assertEquals(255, PackedRange.length(interval));
        assertEquals(355, PackedRange.endExclusive(interval));
    }

    // Cas limite : longueur trop grande (> 255)
    @Test
    void packThrowsForTooLargeLength() {
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(100, 356));
    }

    // Vérifie que pack rejette un intervalle où endExclusive <= startInclusive


    // Vérifie que pack rejette les valeurs négatives
    @Test
    void packThrowsForNegativeStartInclusive() {
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(-1, 10));
    }

    // Vérifie que startInclusive fonctionne correctement
    @Test
    void startInclusiveReturnsCorrectValue() {
        int interval = PackedRange.pack(500, 700);
        assertEquals(500, PackedRange.startInclusive(interval));
    }

    // Vérifie que length fonctionne correctement
    @Test
    void lengthReturnsCorrectValue() {
        int interval = PackedRange.pack(500, 700);
        assertEquals(200, PackedRange.length(interval));
    }

    // Vérifie que endExclusive fonctionne correctement
    @Test
    void endExclusiveReturnsCorrectValue() {
        int interval = PackedRange.pack(500, 700);
        assertEquals(700, PackedRange.endExclusive(interval));
    }

    @Test
    void packAndUnpackWorkCorrectly() {
        int start = 1234;
        int end = 1278;
        int interval = PackedRange.pack(start, end);
        assertEquals(start, PackedRange.startInclusive(interval));
        assertEquals(end, PackedRange.endExclusive(interval));
        assertEquals(end - start, PackedRange.length(interval));
    }

    @Test
    void packThrowsWhenLengthTooLarge() {
        int start = 1000;
        int end = 1000 + 300; // 300 dépasse 255
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(start, end));
    }

    @Test
    void packThrowsWhenStartTooLarge() {
        int start = 1 << 24; // ne tient pas dans 24 bits
        int end = start + 10;
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(start, end));
    }
    @Test
    void packedRangePackWorksOnValidRanges() {
        assertDoesNotThrow(() -> PackedRange.pack(0, 1));
        assertDoesNotThrow(() -> PackedRange.pack(100, 200));
        assertDoesNotThrow(() -> PackedRange.pack((1 << 24) - 256, (1 << 24) - 1));  // Cas limite où length = 255
    }


    @Test
    void packedRangeStartInclusiveWorks() {
        int packed = PackedRange.pack(123456, 123500); // length = 44
        assertEquals(123456, PackedRange.startInclusive(packed));
    }



    @Test
    void packedRangeLengthWorks() {
        int packed = PackedRange.pack(50, 100); // length = 50
        assertEquals(50, PackedRange.length(packed));
    }
}
