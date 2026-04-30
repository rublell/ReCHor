package ch.epfl.rechor;

import ch.epfl.rechor.timetable.Trips;
import ch.epfl.rechor.timetable.mapped.BufferedTrips;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MyBufferedTripsTest {
    @Test
    void routeIdReturnsCorrectValue() {
        List<String> stringTable = List.of("Lausanne", "Renens", "EPFL");
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putShort((short) 1); // ROUTE_ID for trip 0
        byteBuffer.putShort((short) 2); // DESTINATION_ID for trip 0
        byteBuffer.putShort((short) 0); // ROUTE_ID for trip 1
        byteBuffer.putShort((short) 1); // DESTINATION_ID for trip 1
        byteBuffer.flip();

        Trips trips = new BufferedTrips(stringTable, byteBuffer);
        assertEquals(1, trips.routeId(0));
        assertEquals(0, trips.routeId(1));
    }

    @Test
    void destinationReturnsCorrectValue() {
        List<String> stringTable = List.of("A", "B", "C");
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putShort((short) 0); // ROUTE_ID
        byteBuffer.putShort((short) 2); // DESTINATION_ID
        byteBuffer.flip();

        Trips trips = new BufferedTrips(stringTable, byteBuffer);
        assertEquals("C", trips.destination(0));
    }

    @Test
    void sizeReturnsCorrectValue() {
        List<String> stringTable = List.of("A", "B", "C");
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putShort((short) 0); // ROUTE_ID
        byteBuffer.putShort((short) 1); // DESTINATION_ID
        byteBuffer.flip();

        Trips trips = new BufferedTrips(stringTable, byteBuffer);
        assertEquals(1, trips.size());
    }

    @Test
    void methodsThrowExceptionForInvalidIndex() {
        List<String> stringTable = List.of("A", "B", "C");
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putShort((short) 0);
        byteBuffer.putShort((short) 1);
        byteBuffer.flip();

        Trips trips = new BufferedTrips(stringTable, byteBuffer);
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(1));
    }
}
