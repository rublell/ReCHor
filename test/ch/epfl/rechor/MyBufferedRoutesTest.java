package ch.epfl.rechor;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.mapped.BufferedRoutes;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class MyBufferedRoutesTest {
    @Test
    void testSizeReturnsCorrectNumber() {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.putShort((short) 0);
        buffer.put((byte) 0);
        buffer.putShort((short) 1);
        buffer.put((byte) 1);
        buffer.putShort((short) 2);
        buffer.put((byte) 2);
        buffer.flip();
        List<String> stringTable = Arrays.asList("Line A", "Line B", "Line C");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals(3, routes.size());
    }

    @Test
    void testNameReturnsCorrectString() {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.putShort((short) 1);
        buffer.put((byte) 3);
        buffer.flip();
        List<String> stringTable = Arrays.asList("Line A", "Line B", "Line C");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals("Line B", routes.name(0));
    }

    @Test
    void testVehicleReturnsCorrectVehicle() {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.putShort((short) 0);
        buffer.put((byte) 0);
        buffer.putShort((short) 1);
        buffer.put((byte) 1);
        buffer.flip();
        List<String> stringTable = Arrays.asList("Line A", "Line B");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals(Vehicle.ALL.get(0), routes.vehicle(0));
        assertEquals(Vehicle.ALL.get(1), routes.vehicle(1));
    }

    @Test
    void testNameThrowsForInvalidIndex23() {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.putShort((short) 0);
        buffer.put((byte) 0);
        buffer.flip();
        List<String> stringTable = Arrays.asList("Line A");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(1));
    }

    @Test
    void testVehicleThrowsForInvalidIndex() {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.putShort((short) 0);
        buffer.put((byte) 0);
        buffer.flip();
        List<String> stringTable = Arrays.asList("Line A");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(1));
    }

    @Test
    void testSizeReturnsCorrectValue() {
        // Prépare 3 enregistrements.
        ByteBuffer buffer = ByteBuffer.allocate(3 * 3);
        // Enregistrement 1 : name index = 0, kind = 0.
        buffer.putShort((short) 0);
        buffer.put((byte) 0);
        // Enregistrement 2 : name index = 1, kind = 1.
        buffer.putShort((short) 1);
        buffer.put((byte) 1);
        // Enregistrement 3 : name index = 2, kind = 2.
        buffer.putShort((short) 2);
        buffer.put((byte) 2);
        buffer.flip();

        List<String> stringTable = Arrays.asList("Route A", "Route B", "Route C");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals(3, routes.size());
    }

    @Test
    void testNameRetrieval() {
        // Un seul enregistrement.
        ByteBuffer buffer = ByteBuffer.allocate(3);
        // name index = 1, kind = 4.
        buffer.putShort((short) 1);
        buffer.put((byte) 4);
        buffer.flip();

        List<String> stringTable = Arrays.asList("Route A", "Route B", "Route C", "Route D", "Route E");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals("Route B", routes.name(0));
    }

    @Test
    void testVehicleRetrieval() {
        // Prépare 2 enregistrements.
        ByteBuffer buffer = ByteBuffer.allocate(3 * 2);
        // Enregistrement 1: name index = 0, kind = 0.
        buffer.putShort((short) 0);
        buffer.put((byte) 0);
        // Enregistrement 2: name index = 1, kind = 3.
        buffer.putShort((short) 1);
        buffer.put((byte) 3);
        buffer.flip();

        List<String> stringTable = Arrays.asList("Route A", "Route B");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        // On s'attend à ce que le véhicule retourné corresponde à l'élément d'indice kind.
        assertEquals(Vehicle.ALL.get(0), routes.vehicle(0));
        assertEquals(Vehicle.ALL.get(3), routes.vehicle(1));
    }

    @Test
    void testNameThrowsForInvalidInmldex23() {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.putShort((short) 0);
        buffer.put((byte) 2);
        buffer.flip();
        List<String> stringTable = Arrays.asList("Route A", "Route B", "Route C");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(1));
    }

    @Test
    void testVehicleThrowsForInvalidIndex23() {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.putShort((short) 0);
        buffer.put((byte) 2);
        buffer.flip();
        List<String> stringTable = Arrays.asList("Route A", "Route B", "Route C");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(1));
    }

    @Test
    void testMultipleRecords() {
        // Test avec plusieurs enregistrements et vérification de tous les champs.
        int n = 5;
        ByteBuffer buffer = ByteBuffer.allocate(3 * n);
        for (int i = 0; i < n; i++) {
            // Pour chaque enregistrement : name index = i, kind = i % 7.
            buffer.putShort((short) i);
            buffer.put((byte) (i % 7));
        }
        buffer.flip();
        List<String> stringTable = Arrays.asList("R0", "R1", "R2", "R3", "R4");
        BufferedRoutes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals(n, routes.size());
        for (int i = 0; i < n; i++) {
            assertEquals(stringTable.get(i), routes.name(i));
            assertEquals(Vehicle.ALL.get(i % 7), routes.vehicle(i));
        }
    }

}
